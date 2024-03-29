package uk.gov.hmcts.juror.standard.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import uk.gov.hmcts.juror.standard.service.exceptions.InternalServerException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
@SuppressWarnings("PMD.ExcessiveImports")
public class WebConfig {
    @NotBlank
    private String scheme;
    @NotBlank
    private String host;
    @NotNull
    private Integer port;
    @NotBlank
    private String url;

    @Min(0)
    private Integer maxRetries;
    @Min(0)
    private long retryDelay;

    @NestedConfigurationProperty
    private Proxy proxy;

    private String username;
    private String password;

    @NestedConfigurationProperty
    private JwtSecurityConfig security;

    @NestedConfigurationProperty
    private SslConfig ssl;


    public String getUri() {
        return this.scheme + "://" + this.host + ":" + this.port + this.url;
    }

    @Data
    public static class Proxy {
        private String scheme;
        @NotBlank
        private String host;
        @NotNull
        private Integer port;


        private String username;
        private String password;
        private boolean enable;
    }

    @Data
    public static class SslConfig {
        private boolean isBase64Encoded;
        private File trustStoreLocation;
        private String trustStorePassword;

        private File keyStoreLocation;
        private String keyStorePassword;
        private String keyPassword;
        private boolean enable;

        @SuppressWarnings("AbbreviationAsWordInName")
        public SSLConnectionSocketFactory getSSLConnectionSocketFactory()
            throws CertificateException,
            NoSuchAlgorithmException,
            KeyStoreException, IOException,
            UnrecoverableKeyException, KeyManagementException {
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            if (this.getTrustStoreLocation() != null && StringUtils.isNotBlank(
                this.getTrustStorePassword())) {
                sslContextBuilder.loadTrustMaterial(
                    loadKeyStore(this.getTrustStoreLocation(),
                        this.getTrustStorePassword().toCharArray()),
                    null);
            }
            if (this.getKeyStoreLocation() != null && StringUtils.isNotBlank(
                this.getKeyStorePassword())) {
                sslContextBuilder.loadKeyMaterial(
                    loadKeyStore(this.getKeyStoreLocation(),
                        this.getKeyStorePassword().toCharArray()),
                    this.getKeyPassword().toCharArray());
            }
            return new SSLConnectionSocketFactory(sslContextBuilder.build());
        }

        private KeyStore loadKeyStore(final File file, final char... password)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

            byte[] fileContent = Files.readAllBytes(file.toPath());
            if (this.isBase64Encoded) {
                fileContent = Base64.decodeBase64(fileContent);
            }
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (InputStream inputStream = new ByteArrayInputStream(fileContent)) {
                keyStore.load(inputStream, password);
            }
            return keyStore;
        }
    }


    public ClientHttpRequestFactory getRequestFactory() {
        return getRequestFactory(this);
    }

    public static ClientHttpRequestFactory getRequestFactory(
        WebConfig webConfig
    ) {
        try {
            final RequestConfig config =
                RequestConfig.custom()
                    .build();

            final AuthInterceptor authInterceptor = new AuthInterceptor();

            final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
                .setDefaultRequestConfig(config);

            addProxy(httpClientBuilder, authInterceptor, webConfig);

            if (StringUtils.isNotBlank(webConfig.getUsername())) {
                authInterceptor.addBasic("Authorization", webConfig.getUsername(), webConfig.getPassword());
            }

            if (webConfig.getMaxRetries() != null) {
                httpClientBuilder.setRetryStrategy(
                    new DefaultHttpRequestRetryStrategy(webConfig.getMaxRetries(),
                        TimeValue.ofMicroseconds(webConfig.getRetryDelay()))
                );
            }

            if (webConfig.getSsl() != null && webConfig.getSsl().isEnable()) {
                httpClientBuilder.setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(webConfig.getSsl().getSSLConnectionSocketFactory())
                    .build()
                );
            }
            httpClientBuilder.addRequestInterceptorFirst(authInterceptor);
            return new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());
        } catch (Exception e) {
            log.error("Unexpected error when setting up request factory", e);
            throw new InternalServerException("Unexpected error when setting up request factory", e);
        }
    }

    private static void addProxy(HttpClientBuilder httpClientBuilder, AuthInterceptor authInterceptor,
                                 WebConfig webConfig) {
        if (webConfig.getProxy() != null && webConfig.getProxy().isEnable()) {
            Proxy proxy = webConfig.getProxy();
            HttpHost proxyHost = new HttpHost(proxy.getScheme(),
                proxy.getHost(),
                proxy.getPort());

            httpClientBuilder.setProxy(proxyHost);
            if (StringUtils.isNotBlank(proxy.getUsername())) {
                authInterceptor.addBasic("Proxy-Authorization", proxy.getUsername(), proxy.getPassword());
            }
        }
    }


    public static class AuthInterceptor implements HttpRequestInterceptor {

        private final Set<TriConsumer<HttpRequest, EntityDetails, HttpContext>> consumers;

        public AuthInterceptor() {
            this.consumers = new HashSet<>();
        }

        @Override
        public void process(HttpRequest request, EntityDetails entity, HttpContext context) {
            this.consumers.forEach(consumer -> consumer.accept(request, entity, context));
        }

        public void addBasic(final String type, final String username, final String password) {
            String basicAuthString = username + ":" + password;
            String encodedAuthentication = Base64.encodeBase64String(
                basicAuthString.getBytes()).trim();
            this.consumers.add((request, entity, context) ->
                request.addHeader(type, "Basic " + encodedAuthentication));
        }
    }
}
