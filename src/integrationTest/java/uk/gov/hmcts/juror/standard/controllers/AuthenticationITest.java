package uk.gov.hmcts.juror.standard.controllers;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.juror.standard.datastore.entity.auth.UserPermissionConstants.PASSWORD_RESET_SELF;
import static uk.gov.hmcts.juror.standard.datastore.entity.auth.UserPermissionConstants.VIEW_SELF;
import static uk.gov.hmcts.juror.standard.testsupport.DataUtilIT.assignPermissionsRequest;
import static uk.gov.hmcts.juror.standard.testsupport.DataUtilIT.loginRequest;
import static uk.gov.hmcts.juror.standard.testsupport.DataUtilIT.registerRequest;
import static uk.gov.hmcts.juror.standard.testsupport.DataUtilIT.resetPasswordRequest;
import static uk.gov.hmcts.juror.standard.testsupport.DataUtilIT.userEmailRequest;
import static uk.gov.hmcts.juror.standard.testsupport.ITestUtil.dynamicEmailGenerator;
import static uk.gov.hmcts.juror.standard.testsupport.ITestUtil.getNextUniqueIndex;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@SuppressWarnings({
    "PMD.AvoidDuplicateLiterals",
    "PMD.TooManyMethods",
    "PMD.JUnitTestsShouldIncludeAssert"
})
class AuthenticationITest extends AbstractITest {

    private static final String URL_AUTH_LOGIN = "/auth/login";
    private static final String URL_AUTH_REGISTER = "/auth/register";
    private static final String URL_AUTH_USER = "/auth/user";
    private static final String URL_AUTH_USER_RESET_PASSWORD = "/auth/user/reset_password";
    private static final String URL_AUTH_USER_PERMISSIONS = "/auth/user/permissions";

    private static final String RESET_PASSWORD = "testPassword123";

    @Autowired
    protected AuthenticationITest(MockMvc mockMvc) {
        super(mockMvc);
    }

    @DisplayName("Login: as an admin user - is okay")
    @Test
    void loginAsAdmin() throws Exception {
        mockMvcPerform(URL_AUTH_LOGIN, "", POST, loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.jwt").isNotEmpty());
    }

    @DisplayName("Login: Attempt to login as an admin user with the standard user's email - is unauthorised")
    @Test
    void loginAsAdminWithStandardUsername() throws Exception {
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_LOGIN, "", POST, loginRequest(userEmail, ADMIN_PASSWORD_ENCRYPTED))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.code").value("UNAUTHORISED"))
            .andExpect(jsonPath("$.messages")
                .value("You are not authorised"));
    }

    @DisplayName("Login: Attempt to login as an admin user with the standard user's password - is unauthorised")
    @Test
    void loginAsAdminWithStandardUserPassword() throws Exception {
        mockMvcPerform(URL_AUTH_LOGIN, "", POST, loginRequest(ADMIN_EMAIL, USER_PASSWORD))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.code").value("UNAUTHORISED"))
            .andExpect(jsonPath("$.messages")
                .value("You are not authorised"));
    }

    @DisplayName("Login: Standard user has not been registered and user attempts to login without jwt - not authorised")
    @Test
    void loginAsStandardUserButUserNotRegistered() throws Exception {
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_LOGIN, "", POST, loginRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.code").value("UNAUTHORISED"))
            .andExpect(jsonPath("$.messages")
                .value("You are not authorised"));
    }

    @DisplayName("Register: Admin user registers a new standard (non admin) user - is okay")
    @Test
    void adminUserRegistersNewUser() throws Exception {
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));
    }

    @DisplayName("Register: Standard user attempts to register another standard user - not authorised")
    @Test
    void standardUserAttemptsToRegisterAnotherStandardUser() throws Exception {
        //Admin user creates the standard user
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));

        //Standard user logins
        MvcResult mvcResult = mockMvcPerform(URL_AUTH_LOGIN, "", POST, loginRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isOk())
            .andReturn();

        String jwtUser = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.jwt");

        //Standard user now attempts to register another user - not authorised
        String anotherUserEmail = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_REGISTER, jwtUser, POST, registerRequest(anotherUserEmail, USER_PASSWORD))
            .andExpect(status().isUnauthorized());

        //Verify user has not been registered (created) - not found
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(anotherUserEmail))
            .andExpect(status().isNotFound());
    }

    @DisplayName("Register: Admin user attempts to register an email that already exists - is un-processable")
    @Test
    void adminUserAttemptsToRegisterAnExistingUser() throws Exception {
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));

        //Register the same user again - un-processable
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.code").value("USER_ALREADY_REGISTERED"))
            .andExpect(jsonPath("$.messages")
                .value("A user with this email is already registered."));
    }

    @DisplayName("Register: Admin user registers with invalid email - bad request")
    @Test
    void adminUserRegistersNewUserWithInvalidEmail() throws Exception {
        String userEmail = "userEmailIsInvalid.com";
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PAYLOAD"))
            .andExpect(jsonPath("$.messages")
                .value("email: must be a well-formed email address"));
    }

    @DisplayName("Delete: Admin user deletes a user - is accepted")
    @Test
    void adminUserDeleteUser() throws Exception {
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));

        //Admin user now deletes the user
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, DELETE, userEmailRequest(userEmail))
            .andExpect(status().isAccepted());

        //Verify user has been deleted - not found
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isNotFound());
    }

    @DisplayName("Delete: Standard user attempts to deletes a user - is un-authorised")
    @Test
    void standardUserAttemptsToDeleteUser() throws Exception {
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));

        //Register user 1
        String userEmail1 = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail1, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user 1 has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail1));

        //Register user 2
        String userEmail2 = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail2, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user 2 has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail2))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail2));

        //Standard user logins
        MvcResult mvcResult = mockMvcPerform(URL_AUTH_LOGIN, "", POST, loginRequest(userEmail1, USER_PASSWORD))
            .andExpect(status().isOk())
            .andReturn();

        //Standard user 1 now attempts to delete standard user 2 - unauthorised
        String jwtUser = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.jwt");
        mockMvcPerform(URL_AUTH_USER, jwtUser, DELETE, userEmailRequest(userEmail2))
            .andExpect(status().isUnauthorized());

        //Verify user has not been deleted
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail2))
            .andExpect(status().isOk());
    }

    @DisplayName("Delete: Admin user attempts to delete a user that does not exist - not found")
    @Test
    void adminUserDeleteNonExistingUser() throws Exception {
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));

        mockMvcPerform(URL_AUTH_USER, jwtAdmin, DELETE, userEmailRequest("nonExistingUser@domain.com"))
            .andExpect(status().isNotFound());
    }

    @DisplayName("Delete: Admin user attempts to delete a user with an incorrect email address - is bad request")
    @Test
    void adminUserDeleteUserWithBadEmail() throws Exception {
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));

        mockMvcPerform(URL_AUTH_USER, jwtAdmin, DELETE, userEmailRequest("badEmail.com"))
            .andExpect(status().isBadRequest());
    }

    @DisplayName("Reset Password: Admin user is able to reset a user's password - is accepted")
    @Test
    void adminUserResetStandardUserPassword() throws Exception {
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));

        //Register user
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));

        //Admin user resets new user's password
        mockMvcPerform(URL_AUTH_USER_RESET_PASSWORD, jwtAdmin, PUT, resetPasswordRequest(userEmail, RESET_PASSWORD))
            .andExpect(status().isAccepted());
    }

    @DisplayName("Reset Password: Standard user is not able to reset own password without permission - is "
        + "un-authorised")
    @Test
    void userNotAbleToResetPasswordWithoutPermission() throws Exception {
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));

        //Register user
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));

        //User logins
        MvcResult mvcResult = mockMvcPerform(URL_AUTH_LOGIN, "", POST, loginRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isOk())
            .andReturn();

        //User attempts to reset own password
        String jwtUser = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.jwt");
        mockMvcPerform(URL_AUTH_USER_RESET_PASSWORD, jwtUser, PUT, resetPasswordRequest(userEmail, RESET_PASSWORD))
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("Reset Password: Standard user has permission to reset own password - is accepted")
    @Test
    void userHasPermissionToResetOwnPassword() throws Exception {
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));

        //Register user
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));

        //Grant user permission
        Set<String> permissionSet = new HashSet<>();
        permissionSet.add(PASSWORD_RESET_SELF);
        mockMvcPerform(URL_AUTH_USER_PERMISSIONS, jwtAdmin, PUT,
            assignPermissionsRequest(permissionSet, "", userEmail))
            .andExpect(status().isAccepted());

        //User logins
        MvcResult mvcResult = mockMvcPerform(URL_AUTH_LOGIN, "", POST, loginRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isOk())
            .andReturn();

        //User attempts to reset own password
        String jwtUser = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.jwt");
        mockMvcPerform(URL_AUTH_USER_RESET_PASSWORD, jwtUser, PUT, resetPasswordRequest(userEmail, RESET_PASSWORD))
            .andExpect(status().isAccepted());
    }

    @DisplayName("Reset Password: Standard user does not have permission to reset another user's password - "
        + "un-authorised")
    @Test
    void userDoesNotHavePermissionToResetAnotherUserPassword() throws Exception {
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));

        //Register user 1
        String userEmail1 = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail1, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user 1 has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail1));

        //Register user 2
        String userEmail2 = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail2, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user 2 has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail2))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail2));

        //Grant user 1 permission to reset own password
        Set<String> permissionSet = new HashSet<>();
        permissionSet.add(PASSWORD_RESET_SELF);
        mockMvcPerform(URL_AUTH_USER_PERMISSIONS, jwtAdmin, PUT,
            assignPermissionsRequest(permissionSet, "", userEmail1))
            .andExpect(status().isAccepted());

        //User 1 logins
        MvcResult mvcResult = mockMvcPerform(URL_AUTH_LOGIN, "", POST, loginRequest(userEmail1, USER_PASSWORD))
            .andExpect(status().isOk())
            .andReturn();

        //User 1 attempts to reset another users (user 2) password
        String jwtUser = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.jwt");
        mockMvcPerform(URL_AUTH_USER_RESET_PASSWORD, jwtUser, PUT, resetPasswordRequest(userEmail2, RESET_PASSWORD))
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("Permissions: Admin user can assign permissions - is accepted")
    @Test
    void adminUserCanAssignPermissions() throws Exception {
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));

        //Register user
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));

        //Grant user permission to reset own password
        Set<String> permissionSet = new HashSet<>();
        permissionSet.add(PASSWORD_RESET_SELF);
        mockMvcPerform(URL_AUTH_USER_PERMISSIONS, jwtAdmin, PUT,
            assignPermissionsRequest(permissionSet, "", userEmail))
            .andExpect(status().isAccepted());
    }

    @DisplayName("Permissions: Standard user is not authorised to assign permissions - is un-authorised")
    @Test
    void standardUserNotAuthorisedToAssignPermissions() throws Exception {
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));

        //Register user
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));

        //User logins
        MvcResult mvcResult = mockMvcPerform(URL_AUTH_LOGIN, "", POST, loginRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isOk())
            .andReturn();

        //User attempts to assign permission
        String jwtUser = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.jwt");
        Set<String> permissionSet = new HashSet<>();
        permissionSet.add(PASSWORD_RESET_SELF);
        mockMvcPerform(URL_AUTH_USER_PERMISSIONS, jwtUser, PUT,
            assignPermissionsRequest(permissionSet, "", userEmail))
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("User: Admin can view user details")
    @Test
    void adminCanViewUserDetails() throws Exception {
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));

        //Register user
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));

        //View user details
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));
    }

    @DisplayName("User: Standard user cannot view their details without permission")
    @Test
    void standardUserCannotViewTheirDetailsWithoutPermission() throws Exception {
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));

        //Register user
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));

        //User logins
        MvcResult mvcResult = mockMvcPerform(URL_AUTH_LOGIN, "", POST, loginRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isOk())
            .andReturn();

        //View user details
        String jwtUser = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.jwt");
        mockMvcPerform(URL_AUTH_USER, jwtUser, POST, userEmailRequest(userEmail))
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("User: Standard user can view their details")
    @Test
    void standardUserCanViewTheirDetails() throws Exception {
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));

        //Register user
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));

        //Grant user permission to view own details
        Set<String> permissionSet = new HashSet<>();
        permissionSet.add(VIEW_SELF);
        mockMvcPerform(URL_AUTH_USER_PERMISSIONS, jwtAdmin, PUT,
            assignPermissionsRequest(permissionSet, "", userEmail))
            .andExpect(status().isAccepted());

        //User logins
        MvcResult mvcResult = mockMvcPerform(URL_AUTH_LOGIN, "", POST, loginRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isOk())
            .andReturn();

        //View user details
        String jwtUser = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.jwt");
        mockMvcPerform(URL_AUTH_USER, jwtUser, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));
    }

    @DisplayName("User: Standard user cannot view other user details")
    @Test
    void standardUserCanViewOtherUserDetails() throws Exception {
        String jwtAdmin = generateJwt(loginRequest(ADMIN_EMAIL, ADMIN_PASSWORD_ENCRYPTED));

        //Register user
        String userEmail = dynamicEmailGenerator(getNextUniqueIndex());
        mockMvcPerform(URL_AUTH_REGISTER, jwtAdmin, POST, registerRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isCreated());

        //Verify user has been registered (created)
        mockMvcPerform(URL_AUTH_USER, jwtAdmin, POST, userEmailRequest(userEmail))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.email").value(userEmail));

        //Grant user permission to view own details
        Set<String> permissionSet = new HashSet<>();
        permissionSet.add(VIEW_SELF);
        mockMvcPerform(URL_AUTH_USER_PERMISSIONS, jwtAdmin, PUT,
            assignPermissionsRequest(permissionSet, "", userEmail))
            .andExpect(status().isAccepted());

        //User logins
        MvcResult mvcResult = mockMvcPerform(URL_AUTH_LOGIN, "", POST, loginRequest(userEmail, USER_PASSWORD))
            .andExpect(status().isOk())
            .andReturn();

        //User attempts to view details of administrator
        String jwtUser = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.jwt");
        mockMvcPerform(URL_AUTH_USER, jwtUser, POST, userEmailRequest(ADMIN_EMAIL))
            .andExpect(status().isUnauthorized());
    }
}
