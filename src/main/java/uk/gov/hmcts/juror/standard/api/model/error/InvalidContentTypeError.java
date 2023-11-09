package uk.gov.hmcts.juror.standard.api.model.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;

import java.util.List;

@Getter
@Setter
public class InvalidContentTypeError extends GenericError {

    @JsonIgnore
    private static final String ERROR_CODE = "INVALID_CONTENT_TYPE";
    @JsonIgnore
    private static final String ERROR_MESSAGE = "Content Type must be " + MediaType.APPLICATION_JSON_VALUE;

    public InvalidContentTypeError() {
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
