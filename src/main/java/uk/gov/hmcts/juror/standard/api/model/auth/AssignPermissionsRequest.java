package uk.gov.hmcts.juror.standard.api.model.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.juror.standard.api.APIConstants;

import java.util.Set;

@Getter
@Builder
public class AssignPermissionsRequest {

    @Email
    @NotBlank
    private String email;

    @Valid
    private RolePermissions add;

    @Valid
    private RolePermissions remove;

    @Getter
    @Validated
    @Builder
    public static class RolePermissions {

        @Size(min = 1, max = APIConstants.DEFAULT_MAX_LENGTH_LONG)
        Set<@NotNull String> roles;

        @Size(min = 1, max = APIConstants.DEFAULT_MAX_LENGTH_LONG)
        Set<@NotNull String> permissions;
    }
}
