package uk.gov.hmcts.juror.standard.testsupport.controller;

import org.springframework.http.HttpStatus;

public class InvalidPayloadArgument extends ErrorRequestArgument {
    public InvalidPayloadArgument(String requestPayload, String... expectedErrorMessages) {
        super(HttpStatus.BAD_REQUEST, requestPayload, "INVALID_PAYLOAD", expectedErrorMessages);
    }
}
