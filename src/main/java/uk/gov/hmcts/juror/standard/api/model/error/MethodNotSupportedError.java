package uk.gov.hmcts.juror.standard.api.model.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MethodNotSupportedError extends GenericError {

    @JsonIgnore
    private static final String ERROR_CODE = "METHOD_NOT_SUPPORTED";
    @JsonIgnore
    private static final String ERROR_MESSAGE = "This method is not supported.";

    public MethodNotSupportedError() {
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
