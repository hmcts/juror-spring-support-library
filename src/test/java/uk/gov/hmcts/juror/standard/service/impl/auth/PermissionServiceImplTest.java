package uk.gov.hmcts.juror.standard.service.impl.auth;

import org.junit.jupiter.api.AfterEach;
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
import uk.gov.hmcts.juror.standard.datastore.repository.PermissionRepository;
import uk.gov.hmcts.juror.standard.service.contracts.auth.RoleService;
import uk.gov.hmcts.juror.standard.service.exceptions.InvalidPermissionValueException;
import uk.gov.hmcts.juror.standard.testsupport.TestPermissions;
import uk.gov.hmcts.juror.standard.testsupport.TestRoles;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
        PermissionServiceImpl.class
    }
)
@SpringBootTest(properties = {
    "uk.gov.hmcts.juror.security.use-database=true"
})
@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.AvoidDuplicateLiterals"
})
public class PermissionServiceImplTest {
    @Autowired
    private PermissionServiceImpl permissionService;

    @MockBean
    private PermissionRepository permissionRepository;

    @MockBean
    private RoleService roleService;

    @AfterEach
    void afterEach() {
        TestRoles.reset();
    }

    @DisplayName("public Set<Permission> getPermissions(Set<String> permissionsString)")
    @Nested
    class GetPermissions {

        @Test
        void negativePermissionNamesNull() {
            Set<Permission> permissions = permissionService.getPermissions(null);
            assertEquals(0, permissions.size(), "Permission size must match");
        }

        @Test
        void negativePermissionNamesEmpty() {
            Set<Permission> permissions = permissionService.getPermissions(new HashSet<>());
            assertEquals(0, permissions.size(), "Permission size must match");
        }

        @Test
        void negativePermissionNotFound() {
            Set<String> permissionNames = new HashSet<>(Set.of(TestPermissions.USER_CREATE.getName(), "INVALID"));
            when(permissionRepository.getPermissionByNameIn(permissionNames)).thenReturn(
                Set.of(TestPermissions.USER_CREATE));

            InvalidPermissionValueException exception = assertThrows(InvalidPermissionValueException.class,
                () -> permissionService.getPermissions(permissionNames));

            assertEquals("One or more permissions could not be located: [INVALID]", exception.getMessage(),
                "Message must match");
        }

        @Test
        void positivePermissionsFound() {
            Set<String> permissionNames = new HashSet<>(Set.of(TestPermissions.USER_CREATE.getName(),
                TestPermissions.USER_DELETE.getName()));
            Set<Permission> permissions = Set.of(TestPermissions.USER_CREATE, TestPermissions.USER_DELETE);
            when(permissionRepository.getPermissionByNameIn(permissionNames)).thenReturn(permissions);
            assertEquals(permissions, permissionService.getPermissions(permissionNames), "Permissions must match");
        }

    }

    @DisplayName("public Permission createPermission(String permission)")
    @Nested
    class GetOrCreatePermission {

        static final String PERMISSION_NAME = "permissionName123";

        @BeforeEach
        void beforeEach() {
            when(permissionRepository.save(any(Permission.class))).thenAnswer(args -> args.getArguments()[0]);
        }

        @Test
        void positiveTypicalCreated() {
            when(permissionRepository.findById(PERMISSION_NAME)).thenReturn(Optional.empty());

            Permission permission = permissionService.getOrCreatePermission(PERMISSION_NAME);
            assertEquals(PERMISSION_NAME, permission.getName(), "Permission name must match");
            verify(roleService, times(1)).assignPermission(permission, Collections.emptySet());
        }

        @Test
        void positiveTypicalFound() {
            Permission expectedPermission = new Permission().setName(PERMISSION_NAME);

            when(permissionRepository.findById(PERMISSION_NAME)).thenReturn(Optional.of(expectedPermission));

            Permission permission = permissionService.getOrCreatePermission(PERMISSION_NAME);
            assertEquals(expectedPermission, permission, "Permission must match");
            assertEquals(PERMISSION_NAME, permission.getName(), "Permission name must match");
            verify(roleService, times(1)).assignPermission(permission, Collections.emptySet());
        }
    }

    @DisplayName("public Permission getOrCreatePermission(String permissionName, Set<Role> roles)")
    @Nested
    class GetOrCreatePermissionWithRoles {
        static final String PERMISSION_NAME = "permissionName123";

        @BeforeEach
        void beforeEach() {
            when(permissionRepository.save(any(Permission.class))).thenAnswer(args -> args.getArguments()[0]);
        }

        @Test
        void positiveTypicalCreated() {
            Set<Role> roles = Set.of(TestRoles.ADMIN, TestRoles.USER);

            when(permissionRepository.findById(PERMISSION_NAME)).thenReturn(Optional.empty());

            Permission permission = permissionService.getOrCreatePermission(PERMISSION_NAME, roles);
            assertEquals(PERMISSION_NAME, permission.getName(), "Permission name must match");

            verify(roleService, times(1)).assignPermission(permission, roles);
        }

        @Test
        void positiveTypicalFound() {
            Set<Role> roles = Set.of(TestRoles.ADMIN, TestRoles.USER);

            Permission expectedPermission = new Permission().setName(PERMISSION_NAME);

            when(permissionRepository.findById(PERMISSION_NAME)).thenReturn(Optional.of(expectedPermission));

            Permission permission = permissionService.getOrCreatePermission(PERMISSION_NAME, roles);
            assertEquals(expectedPermission, permission,"Permission must match");
            assertEquals(PERMISSION_NAME, permission.getName(),"Permission name must match");

            verify(roleService, times(1)).assignPermission(permission, roles);
        }
    }
}
