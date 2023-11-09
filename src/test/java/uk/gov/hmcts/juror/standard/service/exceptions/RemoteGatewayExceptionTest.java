package uk.gov.hmcts.juror.standard.service.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.juror.standard.api.model.error.RemoteGatewayError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RemoteGatewayExceptionTest {

    @Test
    void positiveConstructorSingle() {
        final String message = "This is my exception message";
        RemoteGatewayException exception = new RemoteGatewayException(message);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode(), "Status code must match");
        assertNotNull(exception.getErrorObject(), "Error object must not be null");
        assertEquals(RemoteGatewayError.class, exception.getErrorObject().getClass(), "Error object class must match");
        assertEquals(APIHandleableException.Type.SYSTEM_ERROR, exception.getExceptionType(),
            "Exception type must match");
        assertEquals(message, exception.getMessage(), "Message must match");
        assertNull(exception.getCause(), "Cause must be null");
    }

    @Test
    void positiveConstructorWithCause() {
        final String message = "This is my exception message";
        final Throwable cause = new RuntimeException("This is the cause of the exception");
        RemoteGatewayException exception = new RemoteGatewayException(message, cause);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode(), "Status code must match");
        assertNotNull(exception.getErrorObject(), "Error object must not be null");
        assertEquals(RemoteGatewayError.class, exception.getErrorObject().getClass(), "Error object class must match");
        assertEquals(APIHandleableException.Type.SYSTEM_ERROR, exception.getExceptionType(),
            "Exception type must match");
        assertEquals(message, exception.getMessage(), "Message must match");
        assertEquals(cause, exception.getCause(), "Cause must match");
    }
}
