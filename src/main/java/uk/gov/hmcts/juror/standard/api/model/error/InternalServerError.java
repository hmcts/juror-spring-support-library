package uk.gov.hmcts.juror.standard.api.model.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InternalServerError extends GenericError {

    @JsonIgnore
    private static final String ERROR_CODE = "INTERNAL_SERVER_ERROR";
    @JsonIgnore
    private static final String ERROR_MESSAGE = "An internal server error has occurred. "
        + "Please contact system administrators if this problem persists.";

    public InternalServerError() {
        super(ERROR_CODE);
        addMessage(ERROR_MESSAGE);
    }

    @Schema(allowableValues = ERROR_CODE)
    @Override
    public String getCode() {
        return super.getCode();
    }

    @Schema(allowableValues = ERROR_MESSAGE)
    @Override
    public List<String> getMessages() {
        return super.getMessages();
    }
}
