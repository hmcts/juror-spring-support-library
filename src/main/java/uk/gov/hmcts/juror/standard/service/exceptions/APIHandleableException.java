package uk.gov.hmcts.juror.standard.service.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import uk.gov.hmcts.juror.standard.api.model.error.GenericError;

import java.io.Serial;

@Getter
public abstract class APIHandleableException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -3368871385680566274L;
    private final Type exceptionType;

    protected APIHandleableException(Type exceptionType, String message) {
        this(exceptionType, message, null);
    }

    protected APIHandleableException(Type exceptionType, String message, Throwable throwable) {
        super(message, throwable);
        this.exceptionType = exceptionType;
    }

    public abstract GenericError getErrorObject();

    public abstract HttpStatusCode getStatusCode();

    public enum Type {
        SYSTEM_ERROR,
        USER_ERROR,
        INFORMATIONAL
    }
}
