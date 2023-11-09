package uk.gov.hmcts.juror.standard.api.model.error.bvr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserAlreadyRegisteredError extends BusinessRuleError {
    @JsonIgnore
    private static final String ERROR_CODE = "USER_ALREADY_REGISTERED";
    @JsonIgnore
    private static final String ERROR_MESSAGE = "A user with this email is already registered.";


    public UserAlreadyRegisteredError() {
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
