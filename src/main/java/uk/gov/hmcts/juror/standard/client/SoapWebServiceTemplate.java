package uk.gov.hmcts.juror.standard.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import uk.gov.hmcts.juror.standard.config.SoapConfig;
import uk.gov.hmcts.juror.standard.config.WebConfig;
import uk.gov.hmcts.juror.standard.service.exceptions.RemoteGatewayException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
public record SoapWebServiceTemplate(
    SoapConfig config, WebServiceTemplate primary, List<WebServiceTemplate> failover) {

    public static SoapWebServiceTemplate create(SoapConfig config,
                                                Function<WebConfig, WebServiceTemplate> webServiceFunction) {
        return new SoapWebServiceTemplate(
            config,
            webServiceFunction.apply(config),
            createFailover(config, webServiceFunction)
        );
    }


    private static List<WebServiceTemplate> createFailover(SoapConfig config,
                                                           Function<WebConfig, WebServiceTemplate> webServiceFunction) {
        List<WebServiceTemplate> failoverList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(config.getFailoverConfig())) {
            for (WebConfig failoverConfig : config.getFailoverConfig()) {
                failoverList.add(webServiceFunction.apply(failoverConfig));
            }
        }
        return failoverList;
    }

    private <T, R> R callPrimary(T request) {
        return call(request, this.primary());
    }

    public <T, R> R call(T request) {
        Exception lastException;
        try {
            return callPrimary(request);
        } catch (Exception e) {
            log.error(
                "Unexpected error when trying to connect to primary service", e);
            lastException = e;
        }

        if (!CollectionUtils.isEmpty(this.failover())) {
            log.info("Attempting failover clients");
        }
        int count = 1;
        for (WebServiceTemplate webServiceTemplate : this.failover()) {
            try {
                return call(request, webServiceTemplate);
            } catch (Exception e) {
                log.error(
                    "Unexpected error when trying to connect to Failover client: attempting failover client " + count++,
                    e);
                lastException = e;
            }
        }
        throw new RemoteGatewayException("Failed to call: " + this.config().getRequestMethod(), lastException);
    }


    @SuppressWarnings("unchecked")
    private <T, R> R call(T request, WebServiceTemplate webServiceTemplate) {
        return (R) webServiceTemplate
            .marshalSendAndReceive(config.getUri(), request, message -> {
                SaajSoapMessage saajSoapMessage = (SaajSoapMessage) message;
                saajSoapMessage.setSoapAction(config.getSoapAction());
            });
    }
}
