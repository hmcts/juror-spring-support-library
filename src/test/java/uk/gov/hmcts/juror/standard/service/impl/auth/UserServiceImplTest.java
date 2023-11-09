package uk.gov.hmcts.juror.standard.service.impl.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.standard.api.model.auth.AssignPermissionsRequest;
import uk.gov.hmcts.juror.standard.api.model.auth.RegisterRequest;
import uk.gov.hmcts.juror.standard.api.model.error.bvr.CannotAssignPermissionsToSelfError;
import uk.gov.hmcts.juror.standard.api.model.error.bvr.CannotDeleteSelfError;
import uk.gov.hmcts.juror.standard.api.model.error.bvr.UserAlreadyRegisteredError;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Permission;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Role;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.User;
import uk.gov.hmcts.juror.standard.datastore.repository.UserRepository;
import uk.gov.hmcts.juror.standard.service.contracts.auth.JwtService;
import uk.gov.hmcts.juror.standard.service.contracts.auth.PermissionService;
import uk.gov.hmcts.juror.standard.service.contracts.auth.RoleService;
import uk.gov.hmcts.juror.standard.service.exceptions.BusinessRuleValidationException;
import uk.gov.hmcts.juror.standard.service.exceptions.InternalServerException;
import uk.gov.hmcts.juror.standard.service.exceptions.NotFoundException;
import uk.gov.hmcts.juror.standard.testsupport.TestConstants;
import uk.gov.hmcts.juror.standard.testsupport.TestPermissions;
import uk.gov.hmcts.juror.standard.testsupport.TestRoles;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
        UserServiceImpl.class
    }
)
@SpringBootTest(properties = {
    "uk.gov.hmcts.juror.security.use-database=true"
})
@DisplayName("UserServiceImpl")
@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.AvoidDuplicateLiterals",
    "PMD.LawOfDemeter",
    "PMD.ExcessiveImports"
})
class UserServiceImplTest {

    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    @MockBean
    private RoleService roleService;
    @MockBean
    private PermissionService permissionService;

    @AfterEach
    void afterEach() {
        TestRoles.reset();
    }

    @DisplayName("public String authenticate(String email, String password)")
    @Nested
    class Authenticate {
        @Test
        void positiveAuthenticated() {
            String password = "password";
            User user = mock(User.class);
            when(jwtService.generateJwtToken(user)).thenReturn(TestConstants.JWT);
            when(userRepository.findUserByEmail(TestConstants.EMAIL)).thenReturn(Optional.of(user));

            userService.authenticate(TestConstants.EMAIL, password);

            ArgumentCaptor<UsernamePasswordAuthenticationToken> argumentCaptor = ArgumentCaptor.forClass(
                UsernamePasswordAuthenticationToken.class);
            verify(authenticationManager, times(1)).authenticate(argumentCaptor.capture());
            assertEquals(TestConstants.EMAIL, argumentCaptor.getValue().getPrincipal(), "Principal must match");
            assertEquals(password, argumentCaptor.getValue().getCredentials(), "Credentials must match");
            verify(jwtService, times(1)).generateJwtToken(user);
        }

        @Test
        void negativeBadCredentials() {
            String email = "schedular@cgi.com";
            String password = "password";
            BadCredentialsException badCredentialsException = mock(BadCredentialsException.class);
            doThrow(badCredentialsException).when(authenticationManager).authenticate(any());
            BadCredentialsException actualException = assertThrows(
                BadCredentialsException.class,
                () -> userService.authenticate(email, password)
            );
            assertEquals(badCredentialsException, actualException, "Exception must match");
        }
    }

    @DisplayName("public String register(RegisterRequest request)")
    @Nested
    class Register {

        @Test
        void positiveUserCreated() {
            String encodedPassword = "encodedPassword_password404";
            RegisterRequest registerRequest = RegisterRequest
                .builder()
                .firstname("FirstName")
                .lastname("LastName")
                .email("schedular@cgi.com")
                .roles(Set.of(TestRoles.USER.getName()))
                .permissions(Set.of(TestPermissions.USER_CREATE.getName()))
                .password("password404")
                .build();

            when(userService.doesUserExist(registerRequest.getEmail())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User user = (User) i.getArguments()[0];
                user.setId(5L);
                return user;
            });
            when(jwtService.generateJwtToken(any())).thenReturn(TestConstants.JWT);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(encodedPassword);

            Set<Role> roles = Set.of(TestRoles.USER);
            Set<Permission> permissions = Set.of(TestPermissions.USER_CREATE);


            when(roleService.getRoles(registerRequest.getRoles())).thenReturn(roles);
            when(permissionService.getPermissions(registerRequest.getPermissions())).thenReturn(permissions);

            assertEquals(TestConstants.JWT, userService.register(registerRequest),"Jwt must match");

            ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

            verify(userRepository, times(1)).save(userArgumentCaptor.capture());

            User user = userArgumentCaptor.getValue();
            assertEquals(5L, user.getId(), "Id must match");
            assertEquals(registerRequest.getFirstname(), user.getFirstname(), "First name must match");
            assertEquals(registerRequest.getLastname(), user.getLastname(), "Last name must match");
            assertEquals(registerRequest.getEmail(), user.getEmail(), "Email must match");
            assertEquals(roles, user.getRoles(), "Roles must match");
            assertEquals(permissions, user.getPermissions(), "Permissions must match");
            assertEquals(encodedPassword, user.getPassword(), "Password must match");

            verify(jwtService, times(1)).generateJwtToken(user);
        }

        @Test
        void negativeUserAlreadyExists() {
            RegisterRequest registerRequest = RegisterRequest
                .builder()
                .firstname("FirstName")
                .lastname("LastName")
                .email(TestConstants.EMAIL)
                .roles(Set.of(TestRoles.USER.getName()))
                .permissions(Set.of(TestPermissions.USER_CREATE.getName()))
                .password("password404")
                .build();

            when(userService.doesUserExist(registerRequest.getEmail())).thenReturn(true);

            BusinessRuleValidationException businessRuleValidationException = assertThrows(
                BusinessRuleValidationException.class,
                () -> userService.register(registerRequest)
            );
            assertEquals(UserAlreadyRegisteredError.class,
                businessRuleValidationException.getErrorObject().getClass(), "Error object class must match");
        }
    }

    @DisplayName("public User getUser(String TestConstants.EMAIL)")
    @Nested
    class GetUser {
        @Test
        void positiveUserFound() {
            User user = mock(User.class);
            when(userRepository.findUserByEmail(TestConstants.EMAIL)).thenReturn(Optional.of(user));
            assertEquals(user, userService.getUser(TestConstants.EMAIL), "Must return correct user");
        }

        @Test
        void negativeUserNotFound() {
            when(userRepository.findUserByEmail(TestConstants.EMAIL)).thenReturn(Optional.empty());
            NotFoundException notFoundException = assertThrows(
                NotFoundException.class,
                () -> userService.getUser(TestConstants.EMAIL),
                "Must throw not found exception"
            );
            assertEquals("User with email: " + TestConstants.EMAIL + " not found", notFoundException.getMessage(),
                "Message must match");
        }
    }

    @DisplayName("public boolean doesUserExist(String TestConstants.EMAIL)")
    @Nested
    class DoesUserExist {
        @Test
        void positiveUserFound() {
            when(userRepository.existsByEmail(TestConstants.EMAIL)).thenReturn(true);
            assertTrue(userService.doesUserExist(TestConstants.EMAIL), "User should exist");
        }

        @Test
        void negativeUserNotFound() {
            when(userRepository.existsByEmail(TestConstants.EMAIL)).thenReturn(false);
            assertFalse(userService.doesUserExist(TestConstants.EMAIL), "User should not exist");
        }
    }

    @DisplayName("public void updatePermissions(AssignPermissionsRequest request)")
    @Nested
    class UpdatePermissions {

        private void setupPositive() {
            Authentication authentication = mock(Authentication.class);
            User user = mock(User.class);
            when(user.getUsername()).thenReturn("another." + TestConstants.EMAIL);
            when(authentication.getPrincipal()).thenReturn(user);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        void positiveNullRoleAndPermissions() {
            setupPositive();
            AssignPermissionsRequest assignPermissionsRequest =
                AssignPermissionsRequest.builder().email(TestConstants.EMAIL).build();
            userService.updatePermissions(assignPermissionsRequest);
            verify(userRepository, never()).save(any());
        }

        @Test
        void positiveEmptyRoleAndPermissions() {
            setupPositive();
            AssignPermissionsRequest assignPermissionsRequest = AssignPermissionsRequest.builder()
                .email(TestConstants.EMAIL)
                .add(getEmptyRolePermissions())
                .remove(getEmptyRolePermissions())
                .build();
            userService.updatePermissions(assignPermissionsRequest);
            verify(userRepository, never()).save(any());
        }

        private AssignPermissionsRequest.RolePermissions getEmptyRolePermissions() {
            return AssignPermissionsRequest.RolePermissions.builder().build();
        }

        @Test
        void positiveOnlyAdd() {
            setupPositive();
            User user = mock(User.class);
            when(userRepository.findUserByEmail(TestConstants.EMAIL)).thenReturn(Optional.of(user));

            AssignPermissionsRequest.RolePermissions add = AssignPermissionsRequest.RolePermissions.builder()
                .roles(Set.of(TestRoles.USER.getName()))
                .permissions(Set.of(TestPermissions.USER_CREATE.getName())
                ).build();

            AssignPermissionsRequest assignPermissionsRequest = AssignPermissionsRequest.builder()
                .email(TestConstants.EMAIL)
                .add(add)
                .build();

            Set<Role> roles = Set.of(TestRoles.USER);
            when(roleService.getRoles(add.getRoles())).thenReturn(roles);

            Set<Permission> permissions = Set.of(TestPermissions.USER_CREATE);
            when(permissionService.getPermissions(add.getPermissions())).thenReturn(permissions);

            userService.updatePermissions(assignPermissionsRequest);
            verify(userRepository, times(1)).save(user);
            verify(user, times(1)).addAllPermissions(permissions);
            verify(user, times(1)).addAllRoles(roles);
            verifyNoMoreInteractions(user);
        }

        @Test
        void positiveOnlyRemove() {
            setupPositive();
            User user = mock(User.class);
            when(userRepository.findUserByEmail(TestConstants.EMAIL)).thenReturn(Optional.of(user));

            AssignPermissionsRequest.RolePermissions remove = AssignPermissionsRequest.RolePermissions.builder()
                .roles(Set.of(TestRoles.USER.getName()))
                .permissions(Set.of(TestPermissions.USER_CREATE.getName())
                ).build();

            AssignPermissionsRequest assignPermissionsRequest = AssignPermissionsRequest.builder()
                .email(TestConstants.EMAIL)
                .remove(remove)
                .build();

            Set<Role> roles = Set.of(TestRoles.USER);
            when(roleService.getRoles(remove.getRoles())).thenReturn(roles);

            Set<Permission> permissions = Set.of(TestPermissions.USER_CREATE);
            when(permissionService.getPermissions(remove.getPermissions())).thenReturn(permissions);

            userService.updatePermissions(assignPermissionsRequest);
            verify(userRepository, times(1)).save(user);
            verify(user, times(1)).removeAllPermissions(permissions);
            verify(user, times(1)).removeAllRoles(roles);
            verifyNoMoreInteractions(user);
        }

        @Test
        void negativeSameAsAuthenticatedUser() {
            Authentication authentication = mock(Authentication.class);
            User user = mock(User.class);
            when(user.getUsername()).thenReturn(TestConstants.EMAIL);
            when(authentication.getPrincipal()).thenReturn(user);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            AssignPermissionsRequest assignPermissionsRequest =
                AssignPermissionsRequest.builder().email(TestConstants.EMAIL).build();
            BusinessRuleValidationException businessRuleValidationException = assertThrows(
                BusinessRuleValidationException.class,
                () -> userService.updatePermissions(assignPermissionsRequest));
            assertEquals(
                CannotAssignPermissionsToSelfError.class,
                businessRuleValidationException.getErrorObject().getClass(),
                "Error object class must match"
            );
        }

        @Test
        void negativeFailedToCheckAuthenticatedUser() {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn("Principal");
            SecurityContextHolder.getContext().setAuthentication(authentication);
            AssignPermissionsRequest assignPermissionsRequest =
                AssignPermissionsRequest.builder().email(TestConstants.EMAIL).build();
            InternalServerException internalServerException = assertThrows(InternalServerException.class,
                () -> userService.updatePermissions(
                    assignPermissionsRequest));

            assertEquals("Unable to check if user is same as authenticated", internalServerException.getMessage(),
                "Message must match");

        }
    }

    @DisplayName("public void deleteUser(String TestConstants.EMAIL)")
    @Nested
    class DeleteUser {


        private void setupPermissions() {
            Authentication authentication = mock(Authentication.class);
            User user = mock(User.class);
            when(user.getUsername()).thenReturn("another." + TestConstants.EMAIL);
            when(authentication.getPrincipal()).thenReturn(user);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        void positiveUserDeleted() {
            setupPermissions();
            when(userRepository.existsByEmail(TestConstants.EMAIL)).thenReturn(true);
            userService.deleteUser(TestConstants.EMAIL);
            verify(userRepository, times(1)).deleteByEmail(TestConstants.EMAIL);
        }

        @Test
        void negativeSameAsAuthenticatedUser() {
            Authentication authentication = mock(Authentication.class);
            User user = mock(User.class);
            when(user.getUsername()).thenReturn(TestConstants.EMAIL);
            when(authentication.getPrincipal()).thenReturn(user);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            BusinessRuleValidationException businessRuleValidationException =
                assertThrows(BusinessRuleValidationException.class, () -> userService.deleteUser(TestConstants.EMAIL));
            assertEquals(CannotDeleteSelfError.class, businessRuleValidationException.getErrorObject().getClass(),
                "Error object class must match");
        }

        @Test
        void negativeUserDoesNotExist() {
            setupPermissions();
            when(userRepository.existsByEmail(TestConstants.EMAIL)).thenReturn(false);
            NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
                userService.deleteUser(TestConstants.EMAIL));
            assertEquals("User with email: " + TestConstants.EMAIL + " not found", notFoundException.getMessage(),
                "Message must match");
            verify(userRepository, never()).deleteByEmail(any());
        }

    }

    @DisplayName(" public void resetPassword(String email, String password)")
    @Nested
    class ResetPassword {
        @Test
        void positivePasswordReset() {
            User user = mock(User.class);
            String password = "newPassword";
            String encodedPassword = "newEncodedPassword";

            when(userRepository.findUserByEmail(TestConstants.EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

            userService.resetPassword(TestConstants.EMAIL, password);

            verify(user, times(1)).setPassword(encodedPassword);
            verify(userRepository, times(1)).save(user);
            verify(userRepository, times(1)).findUserByEmail(TestConstants.EMAIL);
            verifyNoMoreInteractions(user);
            verifyNoMoreInteractions(userRepository);

        }

        @Test
        void negativeUserNotFound() {
            when(userRepository.findUserByEmail(TestConstants.EMAIL)).thenReturn(Optional.empty());

            NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
                userService.resetPassword(TestConstants.EMAIL, "password"));

            assertEquals("User with email: " + TestConstants.EMAIL + " not found", notFoundException.getMessage(),
                "Message must match");
        }

    }
}
