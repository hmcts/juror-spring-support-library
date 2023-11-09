package uk.gov.hmcts.juror.standard.api.model.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.standard.api.APIConstants;

import java.util.Set;

@Getter
@Builder
public class RegisterRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Length(min = 10, max = APIConstants.DEFAULT_MAX_LENGTH_LONG)
    private String password;

    @NotBlank
    private String firstname;

    @NotBlank
    private String lastname;


    @Size(min = 1, max = APIConstants.DEFAULT_MAX_LENGTH_LONG)
    private Set<@NotNull String> roles;

    @Size(min = 1, max = APIConstants.DEFAULT_MAX_LENGTH_LONG)
    private Set<@NotNull String> permissions;
}
