package uk.gov.hmcts.juror.standard.api.controllers.auth;


import io.jsonwebtoken.lang.Collections;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.juror.standard.api.ExceptionHandling;
import uk.gov.hmcts.juror.standard.api.model.auth.AssignPermissionsRequest;
import uk.gov.hmcts.juror.standard.api.model.auth.RegisterRequest;
import uk.gov.hmcts.juror.standard.api.model.auth.UserResponse;
import uk.gov.hmcts.juror.standard.api.model.error.bvr.CannotAssignPermissionsToSelfError;
import uk.gov.hmcts.juror.standard.api.model.error.bvr.CannotDeleteSelfError;
import uk.gov.hmcts.juror.standard.api.model.error.bvr.UserAlreadyRegisteredError;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.User;
import uk.gov.hmcts.juror.standard.mapping.UserMapper;
import uk.gov.hmcts.juror.standard.service.contracts.auth.UserService;
import uk.gov.hmcts.juror.standard.service.exceptions.BusinessRuleValidationException;
import uk.gov.hmcts.juror.standard.service.exceptions.InvalidPermissionValueException;
import uk.gov.hmcts.juror.standard.service.exceptions.InvalidRoleValueException;
import uk.gov.hmcts.juror.standard.service.exceptions.NotFoundException;
import uk.gov.hmcts.juror.standard.testsupport.TestConstants;
import uk.gov.hmcts.juror.standard.testsupport.TestPermissions;
import uk.gov.hmcts.juror.standard.testsupport.TestRoles;
import uk.gov.hmcts.juror.standard.testsupport.TestUtil;
import uk.gov.hmcts.juror.standard.testsupport.controller.ControllerWithPayloadTest;
import uk.gov.hmcts.juror.standard.testsupport.controller.ErrorRequestArgument;
import uk.gov.hmcts.juror.standard.testsupport.controller.InvalidPayloadArgument;
import uk.gov.hmcts.juror.standard.testsupport.controller.NotFoundPayloadArgument;
import uk.gov.hmcts.juror.standard.testsupport.controller.SuccessRequestArgument;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.standard.api.controllers.auth.AuthenticationControllerTest.DeleteUser.DELETE_USER_URL;
import static uk.gov.hmcts.juror.standard.api.controllers.auth.AuthenticationControllerTest.GetUserDetails.GET_USER_DETAILS_URL;
import static uk.gov.hmcts.juror.standard.api.controllers.auth.AuthenticationControllerTest.Login.POST_LOGIN_URL;
import static uk.gov.hmcts.juror.standard.api.controllers.auth.AuthenticationControllerTest.Register.POST_REGISTER_URL;
import static uk.gov.hmcts.juror.standard.api.controllers.auth.AuthenticationControllerTest.ResetPassword.PUT_RESET_PASSWORD_URL;
import static uk.gov.hmcts.juror.standard.api.controllers.auth.AuthenticationControllerTest.UpdatePermissions.PUT_UPDATE_PERMISSIONS_URL;
import static uk.gov.hmcts.juror.standard.testsupport.TestUtil.addJsonPath;
import static uk.gov.hmcts.juror.standard.testsupport.TestUtil.deleteJsonPath;
import static uk.gov.hmcts.juror.standard.testsupport.TestUtil.readResource;
import static uk.gov.hmcts.juror.standard.testsupport.TestUtil.replaceJsonPath;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthenticationController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    properties = {"uk.gov.hmcts.juror.security.use-database=true"})
@ContextConfiguration(
    classes = {
        AuthenticationController.class,
        ExceptionHandling.class
    }
)
@DisplayName("Controller: /auth")
@SuppressWarnings({
    "unchecked",
    "PMD.LawOfDemeter",
    "PMD.ExcessiveImports",
    "PMD.AvoidDuplicateLiterals",
    "PMD.JUnitTestsShouldIncludeAssert"
})
class AuthenticationControllerTest {
    private static final String CONTROLLER_BASEURL = "/auth";
    private static final String RESOURCE_PREFIX = "/testData/authenticationController";

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    protected Stream<InvalidPayloadArgument> getInvalidPayloadUserEmailRequestArgumentSource(
        String payload,
        Consumer<ResultActions> postActions) {
        return Stream.of(
            new InvalidPayloadArgument(replaceJsonPath(payload, "$.email", ""),
                "email: must not be blank").setPostActions(postActions),
            new InvalidPayloadArgument(replaceJsonPath(payload, "$.email", "invalidEmail.com"),
                "email: must be a well-formed email address").setPostActions(postActions),
            new InvalidPayloadArgument(deleteJsonPath(payload, "$.email"),
                "email: must not be blank").setPostActions(postActions)
        );
    }

    protected Stream<InvalidPayloadArgument> getInvalidPayloadPasswordArgumentSource(
        String payload,
        Consumer<ResultActions> postActions) {
        return Stream.of(
            new InvalidPayloadArgument(replaceJsonPath(payload, "$.password", ""), "password: must not be blank",
                "password: length must be between 10 and 2500").setPostActions(postActions),
            new InvalidPayloadArgument(replaceJsonPath(payload, "$.password", "123456789"),
                "password: length must be between 10 and 2500").setPostActions(postActions),
            new InvalidPayloadArgument(
                replaceJsonPath(payload, "$.password", RandomStringUtils.randomAlphanumeric(2501)),
                "password: length must be between 10 and 2500").setPostActions(postActions),
            new InvalidPayloadArgument(deleteJsonPath(payload, "$.password"),
                "password: must not be blank").setPostActions(postActions)
        );
    }

    protected Stream<InvalidPayloadArgument> getInvalidPayloadFirstnameArgumentSource(
        String payload,
        Consumer<ResultActions> postActions) {
        return Stream.of(
            new InvalidPayloadArgument(replaceJsonPath(payload, "$.firstname", ""),
                "firstname: must not be blank").setPostActions(postActions),
            new InvalidPayloadArgument(deleteJsonPath(payload, "$.firstname"),
                "firstname: must not be blank").setPostActions(postActions)
        );
    }

    protected Stream<InvalidPayloadArgument> getInvalidPayloadLastnameArgumentSource(
        String payload,
        Consumer<ResultActions> postActions) {
        return Stream.of(new InvalidPayloadArgument(replaceJsonPath(payload, "$.lastname", ""),
                "lastname: must not be blank").setPostActions(postActions),
            new InvalidPayloadArgument(deleteJsonPath(payload, "$.lastname"),
                "lastname: must not be blank").setPostActions(postActions)
        );
    }

    protected Stream<InvalidPayloadArgument> getInvalidPayloadRolesArgumentSource(String prefixParam, String payload,
                                                                                  Consumer<ResultActions> postActions) {
        String prefix = prefixParam + ".";
        return Stream.of(new InvalidPayloadArgument(replaceJsonPath(payload, prefix + "roles", new HashSet<>()),
            prefix.replace("$.", "") + "roles: size must be between 1 and 2500").setPostActions(postActions)
        );
    }

    protected Stream<InvalidPayloadArgument> getInvalidPayloadPermissionsArgumentSource(
        String prefixParam, String payload,
        Consumer<ResultActions> postActions) {
        String prefix = prefixParam + ".";
        return Stream.of(new InvalidPayloadArgument(replaceJsonPath(payload, prefix + "permissions", new HashSet<>()),
            prefix.replace("$.", "") + "permissions: size must be between 1 and 2500").setPostActions(postActions)
        );
    }


    @Nested
    @DisplayName("POST " + POST_LOGIN_URL)
    class Login extends ControllerWithPayloadTest {
        static final String POST_LOGIN_URL = CONTROLLER_BASEURL + "/login";

        static final String RESPONSE_TOKEN = "testingJwtTokenThatIsStubbedInTheTestSoItIsNotActuallyAWorkingJWT";

        public Login() {
            super(HttpMethod.POST, POST_LOGIN_URL, HttpStatus.OK);
        }

        @Override
        protected String getTypicalPayload() {
            return readResource("postLoginValid.json", RESOURCE_PREFIX);
        }


        private SuccessRequestArgument createSuccessRequestArgument(String name, String email, String password,
                                                                    String payload, String expectedResponse) {
            return new SuccessRequestArgument(name,
                builder -> when(userService.authenticate(email, password)).thenReturn(RESPONSE_TOKEN),
                resultActions -> verify(userService, times(1)).authenticate(email, password),
                payload, expectedResponse);
        }

        @Override
        protected Stream<SuccessRequestArgument> getSuccessRequestArgument() {
            String typicalPayload = getTypicalPayload();
            String expectedResponse = readResource("postLoginValidResponse.json", RESOURCE_PREFIX);

            String maxPassword = RandomStringUtils.random(2500);
            return Stream.of(
                createSuccessRequestArgument("typical", "admin@scheduler.cgi.com",
                    "kj3TXdvYqmFTXXTq!9nA7ZUmDgiQ&W7Z&v7mnFyp2bvM&BZ#nPosFfL8zNvw", typicalPayload,
                    expectedResponse),
                createSuccessRequestArgument("min", "admin@scheduler.cgi.com", "0123456789",
                    replaceJsonPath(typicalPayload, "$.password", "0123456789"), expectedResponse),
                createSuccessRequestArgument("max", "admin@scheduler.cgi.com", maxPassword,
                    replaceJsonPath(typicalPayload, "$.password", maxPassword), expectedResponse)
            );
        }

        @Override
        protected Stream<InvalidPayloadArgument> getInvalidPayloadArgumentSource() {
            String postPayload = getTypicalPayload();
            Consumer<ResultActions> postActions =
                resultActions -> verify(userService, never()).authenticate(any(), any());

            return Stream.concat(
                getInvalidPayloadUserEmailRequestArgumentSource(postPayload, postActions),
                getInvalidPayloadPasswordArgumentSource(postPayload, postActions)
            );
        }

    }

    @Nested
    @DisplayName("POST " + POST_REGISTER_URL)
    class Register extends ControllerWithPayloadTest {
        static final String POST_REGISTER_URL = CONTROLLER_BASEURL + "/register";

        public Register() {
            super(HttpMethod.POST, POST_REGISTER_URL, HttpStatus.CREATED);
        }

        @Override
        protected String getTypicalPayload() {
            return readResource("postRegisterValid.json", RESOURCE_PREFIX);
        }

        private SuccessRequestArgument createSuccessRequestArgument(String name, String email, String password,
                                                                    String firstname, String lastname,
                                                                    Set<String> roleSet, Set<String> permissionSet,
                                                                    String payload) {
            return new SuccessRequestArgument(name,
                null,
                resultActions -> {
                    ArgumentCaptor<RegisterRequest> registerRequestArgumentCaptor =
                        ArgumentCaptor.forClass(RegisterRequest.class);
                    verify(userService, times(1)).register(registerRequestArgumentCaptor.capture());
                    RegisterRequest request = registerRequestArgumentCaptor.getValue();
                    assertEquals(email, request.getEmail(), "Email must match");
                    assertEquals(password, request.getPassword(), "Password must match");
                    assertEquals(firstname, request.getFirstname(), "First name emust match");
                    assertEquals(lastname, request.getLastname(), "Last name must match");
                    if (Collections.isEmpty(roleSet)) {
                        assertNull(request.getRoles(), "Roles must be null");
                    } else {
                        assertEquals(roleSet.size(), request.getRoles().size(), "Role size must match");
                        assertThat("Roles must match", request.getRoles(), hasItems(roleSet.toArray(new String[0])));
                    }
                    if (Collections.isEmpty(permissionSet)) {
                        assertNull(request.getPermissions(), "Permissions must be null");
                    } else {
                        assertEquals(permissionSet.size(), request.getPermissions().size(),
                            "Permissions size must match");
                        assertThat("Permissions must match", request.getPermissions(),
                            hasItems(permissionSet.toArray(new String[0])));
                    }
                },
                payload);
        }

        @Test
        void negativeUserAlreadyRegistered() throws Exception {
            String typicalPayload = getTypicalPayload();
            callAndExpectErrorResponse(new ErrorRequestArgument(HttpStatus.UNPROCESSABLE_ENTITY,
                typicalPayload, "USER_ALREADY_REGISTERED", "A user with this email is already registered.")
                .setPreActions(builder -> when(userService.register(any())).thenThrow(
                    new BusinessRuleValidationException(new UserAlreadyRegisteredError()))));
        }

        @Override
        protected Stream<SuccessRequestArgument> getSuccessRequestArgument() {
            String typicalPayload = getTypicalPayload();
            return Stream.of(
                createSuccessRequestArgument("Typical", "user@cgi.com", "password404", "Ben", "Edwards",
                    Set.of(TestRoles.USER.getName()), Set.of(TestPermissions.USER_CREATE.getName()),
                    typicalPayload),
                createSuccessRequestArgument("No Role/Permissions", "user@cgi.com", "password404", "Ben", "Edwards",
                    Set.of(), Set.of(),
                    deleteJsonPath(deleteJsonPath(typicalPayload, "$.roles"), "$.permissions")),
                createSuccessRequestArgument("With Permission", "user@cgi.com", "password404", "Ben", "Edwards",
                    Set.of(), Set.of(TestPermissions.USER_CREATE.getName()),
                    deleteJsonPath(typicalPayload, "$.roles")),
                createSuccessRequestArgument("With Role", "user@cgi.com", "password404", "Ben", "Edwards",
                    Set.of(TestRoles.USER.getName()), Set.of(),
                    deleteJsonPath(typicalPayload, "$.permissions")),

                createSuccessRequestArgument("Duplicate Role & Permission", "user@cgi.com", "password404", "Ben",
                    "Edwards",
                    Set.of(TestRoles.USER.getName(), TestRoles.ADMIN.getName()),
                    Set.of(TestPermissions.USER_CREATE.getName(),
                        TestPermissions.USER_PERMISSIONS_ASSIGN.getName(),
                        TestPermissions.USER_DELETE.getName()),
                    replaceJsonPath(
                        replaceJsonPath(typicalPayload, "$.permissions",
                            new String[]{TestPermissions.USER_CREATE.getName(),
                                TestPermissions.USER_PERMISSIONS_ASSIGN.getName(),
                                TestPermissions.USER_CREATE.getName(),
                                TestPermissions.USER_DELETE.getName()}),
                        "$.roles", new String[]{TestRoles.USER.getName(), TestRoles.ADMIN.getName(),
                            TestRoles.USER.getName(), TestRoles.ADMIN.getName()}))
            );
        }

        @Override
        protected Stream<InvalidPayloadArgument> getInvalidPayloadArgumentSource() {
            String postPayload = getTypicalPayload();
            Consumer<ResultActions> postActions = resultActions -> verify(userService, never()).register(any());
            return TestUtil.concat(
                getInvalidPayloadUserEmailRequestArgumentSource(postPayload, postActions),
                getInvalidPayloadPasswordArgumentSource(postPayload, postActions),
                getInvalidPayloadFirstnameArgumentSource(postPayload, postActions),
                getInvalidPayloadLastnameArgumentSource(postPayload, postActions),
                Stream.concat(getInvalidPayloadRolesArgumentSource("$", postPayload, postActions),
                    getInvalidPayloadPermissionsArgumentSource("$", postPayload, postActions))
            );
        }

        @Test
        void negativeInvalidPermission() throws Exception {
            String typicalPayload = getTypicalPayload();
            callAndExpectErrorResponse(new ErrorRequestArgument(HttpStatus.BAD_REQUEST,
                typicalPayload, "INVALID_PAYLOAD", "One or more permissions could not be located: [INVALID]")
                .setPreActions(builder -> doThrow(
                    new InvalidPermissionValueException("One or more permissions could not be located: "
                        + "[INVALID]"))
                    .when(userService).register(any())));
        }

        @Test
        void negativeInvalidRole() throws Exception {
            String typicalPayload = getTypicalPayload();
            callAndExpectErrorResponse(new ErrorRequestArgument(HttpStatus.BAD_REQUEST,
                typicalPayload, "INVALID_PAYLOAD", "One or more roles could not be located: [INVALID]")
                .setPreActions(builder -> doThrow(
                    new InvalidRoleValueException("One or more roles could not be located: [INVALID]"))
                    .when(userService).register(any())));
        }

    }

    @Nested
    @DisplayName("DELETE " + DELETE_USER_URL)
    class DeleteUser extends ControllerWithPayloadTest {
        static final String DELETE_USER_URL = CONTROLLER_BASEURL + "/user";

        public DeleteUser() {
            super(HttpMethod.DELETE, DELETE_USER_URL, HttpStatus.ACCEPTED);
        }

        @Override
        protected String getTypicalPayload() {
            return readResource("postUserEmailRequestValid.json", RESOURCE_PREFIX);

        }

        @Override
        protected Stream<SuccessRequestArgument> getSuccessRequestArgument() {
            return Stream.of(new SuccessRequestArgument("Typical", null, resultActions -> {
                verify(userService, times(1)).deleteUser(TestConstants.EMAIL);
            }, getTypicalPayload()));
        }


        @Override
        protected Stream<InvalidPayloadArgument> getInvalidPayloadArgumentSource() {
            Consumer<ResultActions> postActions = resultActions -> verify(userService, never()).deleteUser(any());
            return getInvalidPayloadUserEmailRequestArgumentSource(getTypicalPayload(), postActions);
        }

        @Test
        void negativeUserNotFound() throws Exception {
            callAndValidate(new NotFoundPayloadArgument(getTypicalPayload())
                .setPostActions(resultActions -> verify(userService, times(1)).deleteUser(any()))
                .setPreActions(
                    builder -> doThrow(new NotFoundException("User with email: " + TestConstants.EMAIL + " not found"))
                        .when(userService).deleteUser(TestConstants.EMAIL)));
        }

        @Test
        void negativeCanNotDeleteSelf() throws Exception {
            String typicalPayload = getTypicalPayload();
            callAndExpectErrorResponse(new ErrorRequestArgument(HttpStatus.UNPROCESSABLE_ENTITY,
                typicalPayload, "CAN_NOT_DELETE_SELF", "You can not delete yourself")
                .setPreActions(builder -> doThrow(new BusinessRuleValidationException(new CannotDeleteSelfError()))
                    .when(userService).deleteUser(TestConstants.EMAIL)));
        }
    }

    @Nested
    @DisplayName("PUT " + PUT_RESET_PASSWORD_URL)
    class ResetPassword extends ControllerWithPayloadTest {
        static final String PUT_RESET_PASSWORD_URL = CONTROLLER_BASEURL + "/user/reset_password";

        private static final String PASSWORD = "myNewPassword";

        public ResetPassword() {
            super(HttpMethod.PUT, PUT_RESET_PASSWORD_URL, HttpStatus.ACCEPTED);
        }

        @Override
        protected String getTypicalPayload() {
            return addJsonPath(readResource("postUserEmailRequestValid.json", RESOURCE_PREFIX), "$", "password",
                PASSWORD);

        }

        @Override
        protected Stream<SuccessRequestArgument> getSuccessRequestArgument() {
            return Stream.of(new SuccessRequestArgument("Typical", null,
                resultActions -> verify(userService, times(1)).resetPassword(TestConstants.EMAIL, PASSWORD),
                getTypicalPayload()));
        }


        @Override
        protected Stream<InvalidPayloadArgument> getInvalidPayloadArgumentSource() {
            String payload = getTypicalPayload();
            Consumer<ResultActions> postActions =
                resultActions -> verify(userService, never()).resetPassword(any(), any());

            return Stream.concat(
                getInvalidPayloadUserEmailRequestArgumentSource(payload, postActions),
                getInvalidPayloadPasswordArgumentSource(payload, postActions)
            );
        }

        @Test
        void negativeUserNotFound() throws Exception {
            callAndValidate(new NotFoundPayloadArgument(getTypicalPayload())
                .setPostActions(resultActions -> verify(userService, times(1)).resetPassword(any(), any()))
                .setPreActions(
                    builder -> doThrow(new NotFoundException("User with email: " + TestConstants.EMAIL + " not found"))
                        .when(userService).resetPassword(TestConstants.EMAIL, PASSWORD)));
        }
    }

    @Nested
    @DisplayName("PUT " + PUT_UPDATE_PERMISSIONS_URL)
    class UpdatePermissions extends ControllerWithPayloadTest {
        static final String PUT_UPDATE_PERMISSIONS_URL = CONTROLLER_BASEURL + "/user/permissions";

        public UpdatePermissions() {
            super(HttpMethod.PUT, PUT_UPDATE_PERMISSIONS_URL, HttpStatus.ACCEPTED);
        }

        @Override
        protected String getTypicalPayload() {
            return readResource("postUpdatePermissionsValid.json", RESOURCE_PREFIX);


        }

        @SuppressWarnings("PMD.CognitiveComplexity")
        private SuccessRequestArgument createSuccessRequestArgument(String name, String email,
                                                                    Set<String> addRoleSet,
                                                                    Set<String> addPermissionSet,
                                                                    Set<String> removeRoleSet,
                                                                    Set<String> removePermissionSet,
                                                                    String payload) {
            return new SuccessRequestArgument(name, null, resultActions -> {
                final ArgumentCaptor<AssignPermissionsRequest> captor =
                    ArgumentCaptor.forClass(AssignPermissionsRequest.class);
                verify(userService, times(1)).updatePermissions(captor.capture());
                AssignPermissionsRequest permissionsRequest = captor.getValue();
                assertEquals(email, permissionsRequest.getEmail(), "Email must match");

                if (Collections.isEmpty(addRoleSet) && Collections.isEmpty(addPermissionSet)) {
                    assertNull(permissionsRequest.getAdd(), "Add must be null");
                } else {
                    assertNotNull(permissionsRequest.getAdd(), "Add must not be null");
                    if (Collections.isEmpty(addRoleSet)) {
                        assertNull(permissionsRequest.getAdd().getRoles(), "Add roles must be null");
                    } else {
                        assertEquals(addRoleSet.size(), permissionsRequest.getAdd().getRoles().size(),
                            "Add role size must match");
                        assertThat("Add roles must match",
                            permissionsRequest.getAdd().getRoles(), hasItems(addRoleSet.toArray(new String[0])));
                    }

                    if (Collections.isEmpty(addPermissionSet)) {
                        assertNull(permissionsRequest.getAdd().getPermissions(), "Permissions must be null");
                    } else {
                        assertEquals(addPermissionSet.size(), permissionsRequest.getAdd().getPermissions().size(),
                            "Add Permissions size must match");
                        assertThat("Add permissions must match", permissionsRequest.getAdd().getPermissions(),
                            hasItems(addPermissionSet.toArray(new String[0])));
                    }
                }

                if (Collections.isEmpty(removeRoleSet) && Collections.isEmpty(removePermissionSet)) {
                    assertNull(permissionsRequest.getRemove(), "Remove must be null");
                } else {
                    assertNotNull(permissionsRequest.getRemove(), "Remove must not be null");
                    if (Collections.isEmpty(removeRoleSet)) {
                        assertNull(permissionsRequest.getRemove().getRoles(), "Remove roles must be null");
                    } else {
                        assertEquals(removeRoleSet.size(), permissionsRequest.getRemove().getRoles().size(),
                            "Remove role size must match");
                        assertThat(
                            "Remove roles must match",
                            permissionsRequest.getRemove().getRoles(), hasItems(removeRoleSet.toArray(new String[0])));
                    }

                    if (Collections.isEmpty(removePermissionSet)) {
                        assertNull(permissionsRequest.getRemove().getPermissions(), "Remove permissions must be null");
                    } else {
                        assertEquals(removePermissionSet.size(), permissionsRequest.getRemove().getPermissions().size(),
                            "Remove permission size must match");
                        assertThat("Remove permission must match",
                            permissionsRequest.getRemove().getPermissions(),
                            hasItems(removePermissionSet.toArray(new String[0])));
                    }
                }
            }, payload);
        }

        @Override
        protected Stream<SuccessRequestArgument> getSuccessRequestArgument() {
            String payload = getTypicalPayload();
            return Stream.of(
                createSuccessRequestArgument("Typical", TestConstants.EMAIL, Set.of(TestRoles.ADMIN.getName()),
                    Set.of(TestPermissions.USER_CREATE.getName()), Set.of(TestRoles.USER.getName()),
                    Set.of(TestPermissions.USER_DELETE.getName()), payload),
                createSuccessRequestArgument("Add Only", TestConstants.EMAIL, Set.of(TestRoles.ADMIN.getName()),
                    Set.of(TestPermissions.USER_CREATE.getName()), Set.of(), Set.of(),
                    deleteJsonPath(payload, "$.remove")),
                createSuccessRequestArgument("Remove Only", TestConstants.EMAIL, Set.of(), Set.of(),
                    Set.of(TestRoles.USER.getName()), Set.of(TestPermissions.USER_DELETE.getName()),
                    deleteJsonPath(payload, "$.add")),
                createSuccessRequestArgument("Add Only Permission", TestConstants.EMAIL, Set.of(),
                    Set.of(TestPermissions.USER_CREATE.getName()), Set.of(), Set.of(),
                    deleteJsonPath(deleteJsonPath(payload, "$.remove"), "$.add.roles")),
                createSuccessRequestArgument("Add Only Role", TestConstants.EMAIL, Set.of(TestRoles.ADMIN.getName()),
                    Set.of(),
                    Set.of(), Set.of(),
                    deleteJsonPath(deleteJsonPath(payload, "$.remove"), "$.add.permissions")),

                createSuccessRequestArgument("Remove Only Permission", TestConstants.EMAIL, Set.of(), Set.of(),
                    Set.of(),
                    Set.of(TestPermissions.USER_DELETE.getName()),
                    deleteJsonPath(deleteJsonPath(payload, "$.add"),
                        "$.remove.roles")),
                createSuccessRequestArgument("Remove Only Role", TestConstants.EMAIL, Set.of(), Set.of(),
                    Set.of(TestRoles.USER.getName()), Set.of(),
                    deleteJsonPath(deleteJsonPath(payload, "$.add"), "$.remove.permissions"))
            );
        }


        @Override
        protected Stream<InvalidPayloadArgument> getInvalidPayloadArgumentSource() {
            Consumer<ResultActions> postActions = resultActions -> verify(userService, never()).deleteUser(any());
            String payload = getTypicalPayload();
            return TestUtil.concat(
                getInvalidPayloadUserEmailRequestArgumentSource(payload, postActions),
                getInvalidPayloadRolesArgumentSource("$.add", payload, postActions),
                getInvalidPayloadRolesArgumentSource("$.remove", payload, postActions),
                getInvalidPayloadPermissionsArgumentSource("$.add", payload, postActions),
                getInvalidPayloadPermissionsArgumentSource("$.remove", payload, postActions)
            );
        }

        @Test
        void negativeInvalidPermission() throws Exception {
            String typicalPayload = getTypicalPayload();
            callAndExpectErrorResponse(new ErrorRequestArgument(HttpStatus.BAD_REQUEST,
                typicalPayload, "INVALID_PAYLOAD", "One or more permissions could not be located: [INVALID]")
                .setPreActions(builder -> doThrow(
                    new InvalidPermissionValueException("One or more permissions could not be located: "
                        + "[INVALID]"))
                    .when(userService).updatePermissions(any())));
        }

        @Test
        void negativeInvalidRole() throws Exception {
            String typicalPayload = getTypicalPayload();
            callAndExpectErrorResponse(new ErrorRequestArgument(HttpStatus.BAD_REQUEST,
                typicalPayload, "INVALID_PAYLOAD", "One or more roles could not be located: [INVALID]")
                .setPreActions(builder -> doThrow(
                    new InvalidRoleValueException("One or more roles could not be located: [INVALID]"))
                    .when(userService).updatePermissions(any())));
        }

        @Test
        void negativeUserNotFound() throws Exception {
            callAndValidate(new NotFoundPayloadArgument(getTypicalPayload())
                .setPostActions(resultActions -> verify(userService, times(1)).updatePermissions(any()))
                .setPreActions(
                    builder -> doThrow(new NotFoundException("User with email: " + TestConstants.EMAIL + " not found"))
                        .when(userService).updatePermissions(any())));
        }

        @Test
        void negativeCanNotAssignPermissionsToSelf() throws Exception {
            String typicalPayload = getTypicalPayload();
            callAndExpectErrorResponse(new ErrorRequestArgument(HttpStatus.UNPROCESSABLE_ENTITY,
                typicalPayload, "CAN_NOT_ASSIGN_PERMISSIONS_TO_SELF", "You can not update your own permissions")
                .setPreActions(builder -> doThrow(
                    new BusinessRuleValidationException(new CannotAssignPermissionsToSelfError()))
                    .when(userService).updatePermissions(any())));
        }
    }

    @Nested
    @DisplayName("POST " + GET_USER_DETAILS_URL)
    class GetUserDetails extends ControllerWithPayloadTest {
        static final String GET_USER_DETAILS_URL = CONTROLLER_BASEURL + "/user";

        public GetUserDetails() {
            super(HttpMethod.POST, GET_USER_DETAILS_URL, HttpStatus.OK);
        }

        @Override
        protected String getTypicalPayload() {
            return readResource("postUserEmailRequestValid.json", RESOURCE_PREFIX);

        }

        public SuccessRequestArgument createSuccessRequestArgument(String name, UserResponse userResponse,
                                                                   String responsePayload) {
            User user = User.builder().build();

            return new SuccessRequestArgument(name, builder -> {
                when(userService.getUser(TestConstants.EMAIL)).thenReturn(user);
                when(userMapper.toUserResponse(user)).thenReturn(userResponse);
            }, resultActions -> {
                verify(userService, times(1)).getUser(TestConstants.EMAIL);
                verify(userMapper, times(1)).toUserResponse(any());

            }, getTypicalPayload(), responsePayload);
        }

        @Override
        protected Stream<SuccessRequestArgument> getSuccessRequestArgument() {
            String expectedResponse = readResource("getUserDetails.json", RESOURCE_PREFIX);
            return Stream.of(
                createSuccessRequestArgument("Typical",
                    UserResponse.builder()
                        .email("admin@scheduler.cgi.com")
                        .firstname("Ben")
                        .lastname("Edwards")
                        .roles(Set.of(TestRoles.ADMIN.getName()))
                        .permissions(Set.of(TestPermissions.USER_CREATE.getName()))
                        .combinedPermissions(Set.of(TestPermissions.USER_CREATE.getName(),
                            TestPermissions.USER_DELETE.getName()))
                        .build(),
                    expectedResponse
                )
            );
        }


        @Override
        protected Stream<InvalidPayloadArgument> getInvalidPayloadArgumentSource() {
            Consumer<ResultActions> postActions = resultActions -> verify(userService, never()).getUser(any());
            return getInvalidPayloadUserEmailRequestArgumentSource(getTypicalPayload(), postActions);
        }

        @Test
        void negativeUserNotFound() throws Exception {
            callAndValidate(new NotFoundPayloadArgument(getTypicalPayload())
                .setPostActions(resultActions -> verify(userService, times(1)).getUser(any()))
                .setPreActions(
                    builder -> doThrow(new NotFoundException("User with email: " + TestConstants.EMAIL + " not found"))
                        .when(userService).getUser(TestConstants.EMAIL)));
        }
    }
}
