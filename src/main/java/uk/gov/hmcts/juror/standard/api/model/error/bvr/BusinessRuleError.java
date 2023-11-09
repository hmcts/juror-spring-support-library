package uk.gov.hmcts.juror.standard.api.model.error.bvr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.juror.standard.api.model.error.GenericError;

@Getter
@Setter
public class BusinessRuleError extends GenericError {

    @JsonIgnore
    private static final String ERROR_CODE = "BUSINESS_RULE_VALIDATION_ERROR";

    public BusinessRuleError() {
        this(ERROR_CODE);
    }

    public BusinessRuleError(String errorCode) {
        super(errorCode);
    }

    @Schema(allowableValues = ERROR_CODE)
    @Override
    public String getCode() {
        return super.getCode();
    }
}
