package uk.gov.hmcts.juror.standard.testsupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.gov.hmcts.juror.standard.api.model.auth.AssignPermissionsRequest;
import uk.gov.hmcts.juror.standard.api.model.auth.LoginRequest;
import uk.gov.hmcts.juror.standard.api.model.auth.RegisterRequest;
import uk.gov.hmcts.juror.standard.api.model.auth.ResetPasswordRequest;
import uk.gov.hmcts.juror.standard.api.model.auth.UserEmailRequest;

import java.util.Collections;
import java.util.Set;

import static uk.gov.hmcts.juror.standard.testsupport.ConvertUtilIT.asJsonString;

@SuppressWarnings({
    "PMD.LawOfDemeter"
})
public final class DataUtilIT {
    private DataUtilIT() {
    }

    public static String loginRequest(String email, String password) throws JsonProcessingException {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        return asJsonString(loginRequest);
    }

    public static String registerRequest(String email, String password) throws JsonProcessingException {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email(email)
            .password(password)
            .firstname("Myfirstname")
            .lastname("Mylastname")
            .build();

        return asJsonString(registerRequest);
    }

    public static String userEmailRequest(String email) throws JsonProcessingException {
        UserEmailRequest userEmailRequest = new UserEmailRequest();
        userEmailRequest.setEmail(email);

        return asJsonString(userEmailRequest);
    }

    public static String resetPasswordRequest(String email, String password) throws JsonProcessingException {
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setEmail(email);
        resetPasswordRequest.setPassword(password);

        return asJsonString(resetPasswordRequest);
    }

    public static String assignPermissionsRequest(Set<String> permissions,
                                                  String role,
                                                  String email) throws JsonProcessingException {
        AssignPermissionsRequest.RolePermissions rolePermissions;

        if (role.isEmpty()) {
            rolePermissions = AssignPermissionsRequest.RolePermissions.builder()
                .permissions(permissions)
                .build();
        } else {
            rolePermissions = AssignPermissionsRequest.RolePermissions.builder()
                .permissions(permissions)
                .roles(Collections.singleton(role))
                .build();
        }

        return asJsonString(AssignPermissionsRequest.builder()
            .add(rolePermissions)
            .email(email)
            .build());
    }
}
