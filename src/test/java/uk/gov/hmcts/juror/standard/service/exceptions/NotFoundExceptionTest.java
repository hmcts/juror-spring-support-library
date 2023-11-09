package uk.gov.hmcts.juror.standard.service.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.juror.standard.api.model.error.NotFoundError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class NotFoundExceptionTest {

    @Test
    void positiveConstructorSingle() {
        final String message = "This is my exception message";
        NotFoundException exception = new NotFoundException(message);
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode(), "Status code must match");
        assertNotNull(exception.getErrorObject(), "Error object must not be null");
        assertEquals(NotFoundError.class, exception.getErrorObject().getClass(), "Error object class must match");
        assertEquals(APIHandleableException.Type.INFORMATIONAL, exception.getExceptionType(),
            "Exception type must match");
        assertEquals(message, exception.getMessage(), "Message must match");
        assertNull(exception.getCause(), "Cause must be null");
    }

    @Test
    void positiveConstructorWithCause() {
        final String message = "This is my exception message";
        final Throwable cause = new RuntimeException("This is the cause of the exception");
        NotFoundException exception = new NotFoundException(message, cause);

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode(), "Status code must match");
        assertNotNull(exception.getErrorObject(), "Error object must not be null");
        assertEquals(NotFoundError.class, exception.getErrorObject().getClass(), "Error object class must match");
        assertEquals(APIHandleableException.Type.INFORMATIONAL, exception.getExceptionType(),
            "Exception type must match");
        assertEquals(message, exception.getMessage(), "Message must match");
        assertEquals(cause, exception.getCause(), "Cause must match");
    }
}
