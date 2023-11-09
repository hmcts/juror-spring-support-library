package uk.gov.hmcts.juror.standard.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import uk.gov.hmcts.juror.standard.api.model.error.GenericError;
import uk.gov.hmcts.juror.standard.api.model.error.RemoteGatewayError;

import java.io.Serial;


public class RemoteGatewayException extends APIHandleableException {
    @Serial
    private static final long serialVersionUID = 6942539834559147122L;

    public RemoteGatewayException(String message) {
        this(message, null);
    }

    public RemoteGatewayException(String message, Throwable throwable) {
        super(Type.SYSTEM_ERROR, message, throwable);
    }

    @Override
    public GenericError getErrorObject() {
        return new RemoteGatewayError();
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }
}
