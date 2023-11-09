package uk.gov.hmcts.juror.standard.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import uk.gov.hmcts.juror.standard.api.model.error.GenericError;
import uk.gov.hmcts.juror.standard.api.model.error.UnauthorisedError;

import java.io.Serial;

public class UnauthorisedException extends APIHandleableException {
    @Serial
    private static final long serialVersionUID = 6568103624008682343L;

    public UnauthorisedException(String message) {
        this(message, null);
    }

    public UnauthorisedException(String message, Throwable throwable) {
        super(Type.USER_ERROR, message, throwable);
    }

    @Override
    public GenericError getErrorObject() {
        return new UnauthorisedError();
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return HttpStatus.UNAUTHORIZED;
    }
}
