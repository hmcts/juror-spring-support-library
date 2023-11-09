package uk.gov.hmcts.juror.standard.api.model.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Valid
@NotNull
public class UserEmailRequest {

    @Email
    @NotBlank
    private String email;
}
