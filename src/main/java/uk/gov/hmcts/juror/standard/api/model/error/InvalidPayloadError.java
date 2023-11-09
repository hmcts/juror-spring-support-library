package uk.gov.hmcts.juror.standard.api.model.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidPayloadError extends GenericErrorWithEditableMessages {

    @JsonIgnore
    private static final String ERROR_CODE = "INVALID_PAYLOAD";

    public InvalidPayloadError() {
        super(ERROR_CODE);
    }

    @Schema(allowableValues = ERROR_CODE)
    @Override
    public String getCode() {
        return super.getCode();
    }

}
