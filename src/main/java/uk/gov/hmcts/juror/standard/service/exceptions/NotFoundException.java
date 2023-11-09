package uk.gov.hmcts.juror.standard.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import uk.gov.hmcts.juror.standard.api.model.error.GenericError;
import uk.gov.hmcts.juror.standard.api.model.error.NotFoundError;

import java.io.Serial;

public class NotFoundException extends APIHandleableException {
    @Serial
    private static final long serialVersionUID = -8667501481067373585L;

    public NotFoundException(String message) {
        this(message, null);
    }

    public NotFoundException(String message, Throwable throwable) {
        super(Type.INFORMATIONAL, message, throwable);
    }

    @Override
    public GenericError getErrorObject() {
        return new NotFoundError();
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return HttpStatus.NOT_FOUND;
    }
}
