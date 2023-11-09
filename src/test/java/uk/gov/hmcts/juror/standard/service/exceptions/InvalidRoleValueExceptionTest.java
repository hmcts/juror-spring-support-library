package uk.gov.hmcts.juror.standard.service.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.juror.standard.api.model.error.InvalidPayloadError;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class InvalidRoleValueExceptionTest {

    @Test
    void positiveConstructorSingle() {
        final String message = "This is my exception message";
        InvalidRoleValueException exception = new InvalidRoleValueException(message);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode(), "Status code must match");
        assertNotNull(exception.getErrorObject(), "Error object must not be null");
        assertEquals(InvalidPayloadError.class, exception.getErrorObject().getClass(), "Error object class must match");
        assertEquals(1, exception.getErrorObject().getMessages().size(), "Message size must match");
        assertThat("Messages must match", exception.getErrorObject().getMessages(), hasItem(message));
        assertEquals(APIHandleableException.Type.INFORMATIONAL, exception.getExceptionType(),
            "Exception type must match");
        assertEquals(message, exception.getMessage(), "Message must match");
        assertNull(exception.getCause(), "Cause must be null");
    }
}
