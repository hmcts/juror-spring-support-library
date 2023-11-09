package uk.gov.hmcts.juror.standard.api.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.Set;

@Getter
@Builder
public class UserResponse {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String firstname;

    @NotBlank
    private String lastname;


    @UniqueElements
    private Set<String> roles;

    @UniqueElements
    private Set<String> permissions;


    @UniqueElements
    @JsonProperty("combined_permissions")
    private Set<String> combinedPermissions;
}
