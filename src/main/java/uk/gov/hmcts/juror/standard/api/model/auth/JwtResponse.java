package uk.gov.hmcts.juror.standard.api.model.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtResponse {
    @NotBlank
    private String jwt;
}
