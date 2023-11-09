package uk.gov.hmcts.juror.standard.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.Clock;

@Configuration
@Slf4j
@Order(1)
public class ApplicationBeans {

    @Bean
    @ConditionalOnProperty(prefix = "uk.gov.hmcts.juror.security", name = "use-database", havingValue = "false")
    public AuthenticationProvider authenticationProviderJwt() {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) {
                return authentication;
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return AuthenticationProvider.class.isAssignableFrom(authentication);
            }
        };
    }

    @Bean
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10, new SecureRandom());
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
