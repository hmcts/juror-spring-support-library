package uk.gov.hmcts.juror.standard.api.model.error.bvr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CannotAssignPermissionsToSelfError extends BusinessRuleError {
    @JsonIgnore
    private static final String ERROR_CODE = "CAN_NOT_ASSIGN_PERMISSIONS_TO_SELF";
    @JsonIgnore
    private static final String ERROR_MESSAGE = "You can not update your own permissions";


    public CannotAssignPermissionsToSelfError() {
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
