package uk.gov.hmcts.juror.standard.api.model.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.standard.api.APIConstants;

@Getter
@Setter
public class LoginRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Length(min = 10, max = APIConstants.DEFAULT_MAX_LENGTH_LONG)
    private String password;

}
