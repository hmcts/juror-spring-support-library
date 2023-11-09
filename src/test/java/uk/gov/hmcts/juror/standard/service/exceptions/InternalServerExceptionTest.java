package uk.gov.hmcts.juror.standard.service.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.juror.standard.api.model.error.InternalServerError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class InternalServerExceptionTest {

    @Test
    void positiveConstructorSingle() {
        final String message = "This is my exception message";
        InternalServerException exception = new InternalServerException(message);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode(), "Status code must match");
        assertNotNull(exception.getErrorObject(), "Error object must not be null");
        assertEquals(InternalServerError.class, exception.getErrorObject().getClass(), "Error object class must match");
        assertEquals(APIHandleableException.Type.SYSTEM_ERROR, exception.getExceptionType(),
            "Exception type must match");
        assertEquals(message, exception.getMessage(), "Message must match");
        assertNull(exception.getCause(), "Cause must be null");
    }

    @Test
    void positiveConstructorWithCause() {
        final String message = "This is my exception message";
        final Throwable cause = new RuntimeException("This is the cause of the exception");
        InternalServerException exception = new InternalServerException(message, cause);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode(), "Status code must match");
        assertNotNull(exception.getErrorObject(), "Error object must not be null");
        assertEquals(InternalServerError.class, exception.getErrorObject().getClass(), "Error object class must match");
        assertEquals(APIHandleableException.Type.SYSTEM_ERROR, exception.getExceptionType(),
            "Exception type must match");
        assertEquals(message, exception.getMessage(), "Message must match");
        assertEquals(cause, exception.getCause(), "Cause must match");
    }
}
