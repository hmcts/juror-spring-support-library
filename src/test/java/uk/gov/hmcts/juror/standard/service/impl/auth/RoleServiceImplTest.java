package uk.gov.hmcts.juror.standard.service.impl.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Permission;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Role;
import uk.gov.hmcts.juror.standard.datastore.repository.RoleRepository;
import uk.gov.hmcts.juror.standard.service.exceptions.InvalidRoleValueException;
import uk.gov.hmcts.juror.standard.testsupport.TestPermissions;
import uk.gov.hmcts.juror.standard.testsupport.TestRoles;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RoleServiceImpl.class})
@SpringBootTest(properties = {"uk.gov.hmcts.juror.security.use-database=true"})
@SuppressWarnings({
    "PMD.AvoidDuplicateLiterals"
})
class RoleServiceImplTest {

    @Autowired
    private RoleServiceImpl roleService;

    @MockBean
    private RoleRepository roleRepository;


    @DisplayName("public Set<Role> getRoles(Set<String> roleNames")
    @Nested
    class GetRoles {

        @Test
        void negativeRoleNamesNull() {
            Set<Role> roles = roleService.getRoles(null);
            assertEquals(0, roles.size(), "Role size must be 0");
        }

        @Test
        void negativeRoleNamesEmpty() {
            Set<Role> roles = roleService.getRoles(new HashSet<>());
            assertEquals(0, roles.size(), "Role size must be 0");
        }

        @Test
        void negativeRoleNotFound() {
            Set<String> roleNames = new HashSet<>(Set.of("ADMIN", "INVALID"));
            when(roleRepository.getRolesByNameIsIn(roleNames)).thenReturn(Set.of(TestRoles.ADMIN));

            InvalidRoleValueException exception =
                assertThrows(InvalidRoleValueException.class, () -> roleService.getRoles(roleNames),
                    "Must throw exception");
            assertEquals("One or more roles could not be located: [INVALID]", exception.getMessage(),
                "Message must match");
        }

        @Test
        void positiveRolesFound() {
            Set<String> roleNames = new HashSet<>(Set.of("ADMIN"));
            Set<Role> roles = Set.of(TestRoles.ADMIN);
            when(roleRepository.getRolesByNameIsIn(roleNames)).thenReturn(roles);
            assertEquals(roles, roleService.getRoles(roleNames), "Roles must match");
        }
    }

    @DisplayName("public Role getOrCreate(String roleName)")
    @Nested
    class GetOrCreateRole {
        static final String ROLE_NAME = "roleName123";

        @BeforeEach
        void beforeEach() {
            when(roleRepository.save(any(Role.class))).thenAnswer(args -> args.getArguments()[0]);
        }

        @Test
        void positiveTypicalCreated() {
            when(roleRepository.findById(ROLE_NAME)).thenReturn(Optional.empty());

            Role role = roleService.getOrCreateRole(ROLE_NAME);
            assertEquals(ROLE_NAME, role.getName(), "Role name must match");
            assertNotNull(role.getInheritedRoles(), "Inherited roles must not bee null");
            assertEquals(0, role.getInheritedRoles().size(), "Inherited roles size must be 0");
        }

        @Test
        void positiveTypicalFound() {
            Role expectedRole = new Role(ROLE_NAME);

            when(roleRepository.findById(ROLE_NAME)).thenReturn(Optional.of(expectedRole));

            Role role = roleService.getOrCreateRole(ROLE_NAME);
            assertEquals(expectedRole, role, "Roles must match");
            assertEquals(ROLE_NAME, role.getName(), "Role name must match");
            assertNotNull(role.getInheritedRoles(), "Inherited roles must not bee null");
            assertEquals(0, role.getInheritedRoles().size(), "Inherited roles size must be 0");
        }

        @Test
        void positiveTypicalFoundExistingRolesCleared() {
            Role expectedRole = new Role(ROLE_NAME).setInheritedRoles(Set.of(TestRoles.USER));

            when(roleRepository.findById(ROLE_NAME)).thenReturn(Optional.of(expectedRole));

            Role role = roleService.getOrCreateRole(ROLE_NAME);
            assertEquals(expectedRole, role, "Roles must match");
            assertEquals(ROLE_NAME, role.getName(), "Role name must match");
            assertNotNull(role.getInheritedRoles(), "Inherited roles must not bee null");
            assertEquals(0, role.getInheritedRoles().size(), "Inherited roles size must be 0");

        }
    }

    @DisplayName("public Role getOrCreate(String roleName, Set<Role> inheritedRoles)")
    @Nested
    class GetOrCreateRoleWithInherited {
        static final String ROLE_NAME = "roleName123";

        @BeforeEach
        void beforeEach() {
            when(roleRepository.save(any(Role.class))).thenAnswer(args -> args.getArguments()[0]);
        }

        @Test
        void positiveTypicalCreated() {
            Role inheritedRole = new Role("newRoleName");
            when(roleRepository.findById(ROLE_NAME)).thenReturn(Optional.empty());

            Role role = roleService.getOrCreateRole(ROLE_NAME, Set.of(inheritedRole));
            assertEquals(ROLE_NAME, role.getName(), "Role name must match");
            assertNotNull(role.getInheritedRoles(), "Inherited roles must not bee null");
            assertEquals(1, role.getInheritedRoles().size(), "Inherited Role size must match");
            assertThat("Inherited roles must match", role.getInheritedRoles(), hasItem(inheritedRole));
        }

        @Test
        void positiveTypicalFound() {
            Role inheritedRole = new Role("newRoleName");
            Role expectedRole = new Role(ROLE_NAME);

            when(roleRepository.findById(ROLE_NAME)).thenReturn(Optional.of(expectedRole));

            Role role = roleService.getOrCreateRole(ROLE_NAME, Set.of(inheritedRole));
            assertEquals(ROLE_NAME, role.getName(), "Role name must match");
            assertNotNull(role.getInheritedRoles(), "Inherited roles must not bee null");
            assertEquals(1, role.getInheritedRoles().size(), "Inherited Role size must match");
            assertThat("Inherited roles must match", role.getInheritedRoles(), hasItem(inheritedRole));
        }

        @Test
        void positiveTypicalFoundExistingRolesCleared() {
            Role inheritedRole = new Role("newRoleName");
            Role expectedRole = new Role(ROLE_NAME).setInheritedRoles(Set.of(new Role("OldRole")));

            when(roleRepository.findById(ROLE_NAME)).thenReturn(Optional.of(expectedRole));

            Role role = roleService.getOrCreateRole(ROLE_NAME, Set.of(inheritedRole));
            assertEquals(ROLE_NAME, role.getName(), "Role name must match");
            assertNotNull(role.getInheritedRoles(), "Inherited roles must not bee null");
            assertEquals(1, role.getInheritedRoles().size(), "Inherited Role size must match");
            assertThat("Inherited roles must match", role.getInheritedRoles(), hasItem(inheritedRole));
        }
    }

    @DisplayName("public void assignPermission(Permission permission, Set<Role> roles)")
    @Nested
    class AssignPermissionsToRole {
        private final Permission permission = TestPermissions.USER_PERMISSIONS_ASSIGN;

        @Test
        void negativeNullRoles() {
            roleService.assignPermission(permission, null);
            verify(roleRepository, never()).saveAll(any());
            verifyNoMoreInteractions(roleRepository);
        }

        @Test
        void negativeEmptyRoles() {
            roleService.assignPermission(permission, Collections.emptySet());
            verify(roleRepository, never()).saveAll(any());
            verifyNoMoreInteractions(roleRepository);
        }

        @Test
        void positiveUpdated() {
            Set<Role> roles = Set.of(TestRoles.ADMIN, TestRoles.USER);
            roleService.assignPermission(permission, roles);
            verify(roleRepository, times(1)).saveAll(roles);
            verifyNoMoreInteractions(roleRepository);

            for (Role role : roles) {
                assertEquals(1, role.getPermissions().size(), "Permission size must match");
                assertThat("Permission must match", role.getPermissions(), hasItem(permission));
            }
        }
    }
}
