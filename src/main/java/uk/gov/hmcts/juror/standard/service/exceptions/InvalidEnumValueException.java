package uk.gov.hmcts.juror.standard.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import uk.gov.hmcts.juror.standard.api.model.error.GenericError;
import uk.gov.hmcts.juror.standard.api.model.error.InvalidPayloadError;

import java.io.Serial;

public class InvalidEnumValueException extends APIHandleableException {

    @Serial
    private static final long serialVersionUID = -8573605186571014569L;

    public InvalidEnumValueException(String message) {
        super(Type.INFORMATIONAL, message);
    }

    @Override
    public GenericError getErrorObject() {
        return new InvalidPayloadError()
            .addMessage(getMessage());
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return HttpStatus.BAD_REQUEST;
    }
}
