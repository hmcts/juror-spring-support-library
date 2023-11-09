package uk.gov.hmcts.juror.standard.service.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import uk.gov.hmcts.juror.standard.api.model.error.GenericError;
import uk.gov.hmcts.juror.standard.api.model.error.InternalServerError;

import java.io.Serial;

@Slf4j
public class InternalServerException extends APIHandleableException {

    @Serial
    private static final long serialVersionUID = -1427766114695642730L;

    public InternalServerException(String message) {
        this(message, null);
    }

    public InternalServerException(String message, Throwable throwable) {
        super(Type.SYSTEM_ERROR, message, throwable);
        log.error(message,throwable);
    }

    @Override
    public GenericError getErrorObject() {
        return new InternalServerError();
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
