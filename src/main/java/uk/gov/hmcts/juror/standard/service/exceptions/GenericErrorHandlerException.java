package uk.gov.hmcts.juror.standard.service.exceptions;

import org.springframework.http.HttpStatusCode;
import uk.gov.hmcts.juror.standard.api.model.error.GenericError;

import java.io.Serial;

public class GenericErrorHandlerException extends APIHandleableException {
    @Serial
    private static final long serialVersionUID = 7758153292866254033L;
    private final GenericError errorObject;
    private final HttpStatusCode statusCode;

    public GenericErrorHandlerException(Type exceptionType, GenericError genericError, HttpStatusCode statusCode) {
        this(genericError.getCode() + " " + (genericError.getMessages()), exceptionType, genericError, statusCode);
    }

    public GenericErrorHandlerException(String message, Type exceptionType, GenericError genericError,
                                        HttpStatusCode statusCode) {
        super(exceptionType, message);
        this.errorObject = genericError;
        this.statusCode = statusCode;
    }

    @Override
    public GenericError getErrorObject() {
        return this.errorObject;
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return this.statusCode;
    }
}
