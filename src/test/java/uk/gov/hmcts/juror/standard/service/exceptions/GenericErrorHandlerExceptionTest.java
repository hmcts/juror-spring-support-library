package uk.gov.hmcts.juror.standard.service.exceptions;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.juror.standard.api.model.error.GenericError;
import uk.gov.hmcts.juror.standard.api.model.error.InternalServerError;
import uk.gov.hmcts.juror.standard.api.model.error.NotFoundError;
import uk.gov.hmcts.juror.standard.api.model.error.UnauthorisedError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings({
    "PMD.AvoidDuplicateLiterals"
})
class GenericErrorHandlerExceptionTest {

    @ParameterizedTest
    @EnumSource(APIHandleableException.Type.class)
    void positiveConstructorExceptionType(APIHandleableException.Type type) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        GenericError genericError = new NotFoundError();
        GenericErrorHandlerException exception = new GenericErrorHandlerException(type, genericError, status);
        assertEquals(status, exception.getStatusCode(), "Status code must match");
        assertEquals(genericError, exception.getErrorObject(), "Error object must match");
        assertEquals(type, exception.getExceptionType(), "Exception type must match");
        assertNull(exception.getCause(), "Cause must be null");
    }

    @ParameterizedTest
    @EnumSource(HttpStatus.class)
    void positiveConstructorStatusCode(HttpStatus status) {
        APIHandleableException.Type type = APIHandleableException.Type.INFORMATIONAL;
        GenericError genericError = new UnauthorisedError();
        GenericErrorHandlerException exception = new GenericErrorHandlerException(type, genericError, status);
        assertEquals(status, exception.getStatusCode(), "Status code must match");
        assertEquals(genericError, exception.getErrorObject(), "Error object must match");
        assertEquals(type, exception.getExceptionType(), "Exception type must match");
        assertNull(exception.getCause(), "Cause must be null");
    }

    @ParameterizedTest
    @EnumSource(APIHandleableException.Type.class)
    void positiveConstructorExceptionTypeWithMessage(APIHandleableException.Type type) {
        HttpStatus status = HttpStatus.CONFLICT;
        String message = "This is my exception message";
        GenericError genericError = new UnauthorisedError();
        GenericErrorHandlerException exception = new GenericErrorHandlerException(message, type, genericError, status);
        assertEquals(status, exception.getStatusCode(), "Status code must match");
        assertEquals(genericError, exception.getErrorObject(), "Error object must match");
        assertEquals(type, exception.getExceptionType(), "Exception type must match");
        assertEquals(message, exception.getMessage(), "Message must match");
        assertNull(exception.getCause(), "Cause must be null");
    }

    @ParameterizedTest
    @EnumSource(HttpStatus.class)
    void positiveConstructorStatusCodeWithMessage(HttpStatus status) {
        APIHandleableException.Type type = APIHandleableException.Type.USER_ERROR;
        GenericError genericError = new InternalServerError();
        String message = "This is my exception message";
        GenericErrorHandlerException exception = new GenericErrorHandlerException(message, type, genericError, status);
        assertEquals(status, exception.getStatusCode(), "Status code must match");
        assertEquals(genericError, exception.getErrorObject(), "Error object must match");
        assertEquals(type, exception.getExceptionType(), "Exception type must match");
        assertEquals(message, exception.getMessage(), "Message must match");
        assertNull(exception.getCause(), "Cause must be null");
    }
}
