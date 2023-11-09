package uk.gov.hmcts.juror.standard.client;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@SuppressWarnings({
    "PMD.AbstractClassWithoutAbstractMethod"
})
public abstract class AbstractSoapClient {

    private final SoapWebServiceTemplate soapWebServiceTemplate;

    protected AbstractSoapClient(SoapWebServiceTemplate soapWebServiceTemplate) {
        this.soapWebServiceTemplate = soapWebServiceTemplate;
    }
}
