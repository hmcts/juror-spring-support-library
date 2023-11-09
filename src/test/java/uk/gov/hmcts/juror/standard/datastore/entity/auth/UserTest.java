package uk.gov.hmcts.juror.standard.datastore.entity.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.standard.testsupport.TestPermissions;
import uk.gov.hmcts.juror.standard.testsupport.TestRoles;
import uk.gov.hmcts.juror.standard.testsupport.TestUtil;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@Nested
@DisplayName("User Entity Tests")
@SuppressWarnings({
    "unchecked",
    "PMD.LawOfDemeter",
    "PMD.AvoidDuplicateLiterals"
})
class UserTest {
    @AfterEach
    void afterEach() {
        TestRoles.reset();
    }

    @Nested
    @DisplayName("public Collection<? extends GrantedAuthority> getAuthorities()")
    class GetAuthoritiesTest {

        @Test
        void positiveConvertPermissionEmpty() {
            Set<Permission> permissionSet = Set.of();
            User user = new User();
            user.setPermissions(permissionSet);

            assertNotNull(user.getAuthorities(), "Authorities must not be null");
            assertEquals(0, user.getAuthorities().size(), "Authorities size must be 0");
        }

        @Test
        void positiveConvertPermissionSingle() {
            Set<Permission> permissionSet = Set.of(TestPermissions.USER_CREATE);
            User user = new User();
            user.setPermissions(permissionSet);

            assertNotNull(user.getAuthorities(), "Authorities must not be null");
            assertEquals(1, user.getAuthorities().size(), "Authorities size must be 1");
            assertThat("Authorities must contain USER_CREATE", user.getAuthorities(),
                hasItem(equalTo(TestPermissions.USER_CREATE.toGrantedAuthority())));
        }

        @Test
        void positiveConvertPermissionMultiple() {
            Set<Permission> permissionSet = Set.of(TestPermissions.USER_CREATE, TestPermissions.USER_DELETE);
            User user = new User();
            user.setPermissions(permissionSet);

            assertNotNull(user.getAuthorities(), "Authorities must not be null");
            assertEquals(2, user.getAuthorities().size(), "Authorities size must be 2");
            assertThat("Authorities must contain USER_CREATE & USER_DELETE", user.getAuthorities(),
                hasItems(equalTo(TestPermissions.USER_CREATE.toGrantedAuthority()),
                    equalTo(TestPermissions.USER_DELETE.toGrantedAuthority())));
        }

        @Test
        void positiveConvertPermissionNull() {
            User user = new User();
            user.setPermissions(null);
            assertNotNull(user.getAuthorities(), "Authorities must not be null");
            assertEquals(0, user.getAuthorities().size(), "Authorities size must be 0");
        }

        @Test
        void positiveConvertPermissionRoleInherited() {
            Set<Permission> permissionSet = Set.of(TestPermissions.USER_CREATE, TestPermissions.USER_DELETE);
            TestRoles.ADMIN.addPermission(TestPermissions.JOB_RUN);
            TestRoles.ADMIN.addPermission(TestPermissions.USER_PERMISSIONS_ASSIGN);
            User user = new User();
            user.setPermissions(permissionSet);
            user.setRoles(Set.of(TestRoles.ADMIN));

            assertNotNull(user.getAuthorities(), "Authorities must not be null");
            assertEquals(4, user.getAuthorities().size(), "Authorities size must be 4");
            assertThat(
                "Authorities must contain USER_CREATE & USER_DELETE & JOB_RUN & USER_PERMISSIONS_ASSIGN",
                user.getAuthorities(), hasItems(
                    equalTo(TestPermissions.USER_CREATE.toGrantedAuthority()),
                    equalTo(TestPermissions.USER_DELETE.toGrantedAuthority()),
                    equalTo(TestPermissions.JOB_RUN.toGrantedAuthority()),
                    equalTo(TestPermissions.USER_PERMISSIONS_ASSIGN.toGrantedAuthority())
                ));
        }

    }

    @Nested
    @DisplayName("public Set<Permission> getCombinedPermissions()")
    class GetCombinedPermissionsTest {
        @Test
        void positiveNoRolesNoPermissions() {
            User user = new User();
            assertNotNull(user.getCombinedPermissions(), "Combined permissions must not be null");
            assertEquals(0, user.getCombinedPermissions().size(), "Combined permissions size must be 0");
        }

        @Test
        void positiveNullRolesNullPermissions() {
            User user = new User();
            user.setPermissions(null);
            user.setRoles(null);

            assertNotNull(user.getCombinedPermissions(), "Combined permissions must not be null");
            assertEquals(0, user.getCombinedPermissions().size(), "Combined permissions size must be 0");
        }

        @Test
        void positiveRolesOnly() {
            TestRoles.ADMIN.addPermission(TestPermissions.JOB_RUN);
            TestRoles.ADMIN.addPermission(TestPermissions.USER_PERMISSIONS_ASSIGN);
            User user = new User();
            user.setRoles(Set.of(TestRoles.ADMIN));

            assertNotNull(user.getCombinedPermissions(), "Combined permissions must not be null");
            assertEquals(2, user.getCombinedPermissions().size(), "Combined permissions size must be 2");
            assertThat(
                "Combined permissions must contain JOB_RUN & USER_PERMISSIONS_ASSIGN",
                user.getCombinedPermissions(), hasItems(
                    TestPermissions.JOB_RUN,
                    TestPermissions.USER_PERMISSIONS_ASSIGN
                ));

        }

        @Test
        void positivePermissionsOnly() {
            Set<Permission> permissionSet = Set.of(TestPermissions.USER_CREATE, TestPermissions.USER_DELETE);
            User user = new User();
            user.setPermissions(permissionSet);

            assertNotNull(user.getCombinedPermissions(), "Combined permissions must not be null");
            assertEquals(2, user.getCombinedPermissions().size(), "Combined permissions size must be 2");
            assertThat(
                "Combined permissions must contain USER_CREATE & USER_DELETE",
                user.getCombinedPermissions(), hasItems(
                    TestPermissions.USER_CREATE,
                    TestPermissions.USER_DELETE
                ));
        }

        @Test
        void positiveRolesAndPermissions() {
            Set<Permission> permissionSet = Set.of(TestPermissions.USER_CREATE, TestPermissions.USER_DELETE);
            TestRoles.ADMIN.addPermission(TestPermissions.JOB_RUN);
            TestRoles.ADMIN.addPermission(TestPermissions.USER_PERMISSIONS_ASSIGN);
            User user = new User();
            user.setPermissions(permissionSet);
            user.setRoles(Set.of(TestRoles.ADMIN));

            assertNotNull(user.getCombinedPermissions(), "Combined permissions must not be null");
            assertEquals(4, user.getCombinedPermissions().size(), "Combined permissions size must be 4");
            assertThat(
                "Combined permissions must contain USER_CREATE & USER_DELETE & JOB_RUN & USER_PERMISSIONS_ASSIGN",
                user.getCombinedPermissions(), hasItems(
                    TestPermissions.USER_CREATE,
                    TestPermissions.USER_DELETE,
                    TestPermissions.JOB_RUN,
                    TestPermissions.USER_PERMISSIONS_ASSIGN
                ));
        }
    }

    @Nested
    @DisplayName("public Set<Permission> getPermissions()")
    class GetPermissionsTest {

        @Test
        void positiveSingleValue() {
            Set<Permission> permissionSet = Set.of(TestPermissions.USER_CREATE);
            User user = new User();
            user.setPermissions(permissionSet);

            assertNotNull(user.getPermissions(), "Permissions must not be null");
            assertEquals(1, user.getPermissions().size(), "Permissions size must be 1");
            assertThat(
                "Permissions must contain USER_CREATE",
                user.getPermissions(), hasItems(
                    TestPermissions.USER_CREATE
                ));
            TestUtil.isUnmodifiable(user.getPermissions());
        }

        @Test
        void positiveMultipleValues() {
            Set<Permission> permissionSet = Set.of(TestPermissions.USER_CREATE, TestPermissions.USER_DELETE);
            User user = new User();
            user.setPermissions(permissionSet);

            assertNotNull(user.getPermissions(), "Permissions must not be null");
            assertEquals(2, user.getPermissions().size(), "Permissions size must be 2");
            assertThat(
                "Permissions must contain USER_CREATE & USER_DELETE",
                user.getPermissions(), hasItems(
                    TestPermissions.USER_CREATE,
                    TestPermissions.USER_DELETE
                ));
            TestUtil.isUnmodifiable(user.getPermissions());
        }

        @Test
        void positiveNullPermissions() {
            User user = new User();
            user.setPermissions(null);
            assertNotNull(user.getPermissions(), "Permissions must not be null");
            assertEquals(0, user.getPermissions().size(), "Permission size must be 0");
            TestUtil.isUnmodifiable(user.getPermissions());
        }
    }

    @Nested
    @DisplayName("public Set<Role> getRoles()")
    class GetRolesTest {
        @Test
        void positiveSingleValue() {
            User user = new User();
            user.setRoles(Set.of(TestRoles.ADMIN));

            assertNotNull(user.getRoles(), "Roles must not be null");
            assertEquals(1, user.getRoles().size(), "Role size must be 1");
            assertThat(
                "Roles must contain ADMIN",
                user.getRoles(), hasItems(
                    TestRoles.ADMIN
                ));
            TestUtil.isUnmodifiable(user.getRoles());
        }

        @Test
        void positiveMultipleValues() {
            User user = new User();
            user.setRoles(Set.of(TestRoles.ADMIN, TestRoles.USER));

            assertNotNull(user.getRoles(), "Roles must not be null");
            assertEquals(2, user.getRoles().size(), "Role size must be 2");
            assertThat(
                "Roles must contain ADMIN & USER",
                user.getRoles(), hasItems(
                    TestRoles.ADMIN, TestRoles.USER
                ));
            TestUtil.isUnmodifiable(user.getRoles());
        }

        @Test
        void positiveNullRoles() {
            User user = new User();
            user.setRoles(null);
            assertNotNull(user.getRoles(), "Roles must not be null");
            assertEquals(0, user.getRoles().size(), "Role size must be 0");
            TestUtil.isUnmodifiable(user.getRoles());
        }
    }

    @Nested
    @DisplayName("public void removeAllPermissions(Collection<Permission> permissions)")
    class RemoveAllPermissionsTest {
        private Set<Permission> permissionSet;

        @BeforeEach
        public void beforeEach() {
            this.permissionSet = Set.of(mock(Permission.class));
        }


        @Test
        void negativeNullProvidedPermissions() {
            User user = new User();
            user.setPermissions(permissionSet);
            user.removeAllPermissions(null);
            assertEquals(permissionSet.size(), user.getPermissions().size(), "Permissions size must match");
        }

        @Test
        void negativeEmptyProvidedPermissions() {
            User user = new User();
            user.setPermissions(permissionSet);
            Set<Permission> permissions = Set.of();
            user.removeAllPermissions(permissions);
            assertEquals(permissionSet.size(), user.getPermissions().size(), "Permissions size must match");
        }

        @Test
        void negativeNullPermissions() {
            User user = new User();
            user.setPermissions(null);
            Set<Permission> permissions = Set.of(TestPermissions.USER_CREATE, TestPermissions.USER_PERMISSIONS_ASSIGN);
            assertDoesNotThrow(() -> user.removeAllPermissions(permissions),
                "Remove permissions must not throw any exceptions");
        }

        @Test
        void positiveRemoveAll() {
            User user = new User();
            user.setPermissions(Set.of(
                TestPermissions.USER_CREATE,
                TestPermissions.USER_PERMISSIONS_ASSIGN,
                TestPermissions.USER_DELETE));
            Set<Permission> permissions = Set.of(TestPermissions.USER_CREATE, TestPermissions.USER_PERMISSIONS_ASSIGN);
            user.removeAllPermissions(permissions);
            assertEquals(1, user.getPermissions().size(), "Permission should be removed");
            assertTrue(user.getPermissions().contains(TestPermissions.USER_DELETE),
                "Only USER_DELETE Permission should remain");
        }
    }

    @Nested
    @DisplayName("public void removeAllRoles(Collection<Role> roles)")
    class RemoveAllRolesTest {
        private Set<Role> roleSet;

        @BeforeEach
        public void beforeEach() {
            this.roleSet = Set.of(mock(Role.class));
        }


        @Test
        void negativeNullProvidedRoles() {
            User user = new User();
            user.setRoles(roleSet);
            user.removeAllRoles(null);
            assertEquals(roleSet.size(), user.getRoles().size(), "Role size must match");
        }

        @Test
        void negativeEmptyProvidedRoles() {
            User user = new User();
            user.setRoles(roleSet);
            Set<Role> roles = Set.of();
            user.removeAllRoles(roles);
            assertEquals(roleSet.size(), user.getRoles().size(), "Role size must match");
        }

        @Test
        void negativeNullRoles() {
            User user = new User();
            user.setRoles(null);
            Set<Role> roles = Set.of(TestRoles.ADMIN);
            assertDoesNotThrow(() -> user.removeAllRoles(roles),
                "Remove all roles must not throw exceptions");
        }

        @Test
        void positiveRemoveAll() {
            User user = new User();
            user.setRoles(Set.of(TestRoles.ADMIN, TestRoles.USER));
            Set<Role> roles = Set.of(TestRoles.ADMIN);
            user.removeAllRoles(roles);
            assertEquals(1, user.getRoles().size(), "Role should be removed");
            assertTrue(user.getRoles().contains(TestRoles.USER), "Only user role should remain");
        }
    }

    @Nested
    @DisplayName("public void addAllPermissions(Collection<Permission> permissions)")
    class AddAllPermissionsTest {

        @Test
        void positiveTypical() {
            User user = new User();
            user.setPermissions(new HashSet<>());
            Set<Permission> permissions = Set.of(TestPermissions.USER_CREATE, TestPermissions.USER_PERMISSIONS_ASSIGN);
            user.addAllPermissions(permissions);
            assertEquals(2, user.getPermissions().size(), "Permissions size must be 2");
            assertThat("Permissions must match", user.getPermissions(),
                hasItems(permissions.toArray(new Permission[0])));
        }

        @Test
        void positiveAddition() {
            User user = new User();
            user.setPermissions(Set.of(TestPermissions.JOB_RUN, TestPermissions.USER_DELETE));
            Set<Permission> permissions = Set.of(TestPermissions.USER_CREATE, TestPermissions.USER_PERMISSIONS_ASSIGN);
            user.addAllPermissions(permissions);
            assertEquals(4, user.getPermissions().size(), "Permissions size must be 4");
            assertThat("Permissions must match",
                user.getPermissions(), hasItems(permissions.toArray(new Permission[0])));
            assertThat("Permissions must contain JOB_RUN & USER_DELETE",
                user.getPermissions(), hasItems(TestPermissions.JOB_RUN, TestPermissions.USER_DELETE));
        }

        @Test
        void negativeNullProvidedPermissions() {
            User user = new User();
            user.setPermissions(new HashSet<>());
            user.addAllPermissions(null);
            assertEquals(0, user.getPermissions().size(), "Permission size must be 0");
        }

        @Test
        void negativeEmptyProvidedPermissions() {
            User user = new User();
            user.setPermissions(new HashSet<>());
            Set<Permission> permissions = Set.of();
            user.addAllPermissions(permissions);
            assertEquals(0, user.getPermissions().size(), "Permission size must be 0");
        }

        @Test
        void negativeNullPermissions() {
            User user = new User();
            user.setPermissions(null);
            Set<Permission> permissions = Set.of(TestPermissions.USER_CREATE, TestPermissions.USER_PERMISSIONS_ASSIGN);
            user.addAllPermissions(permissions);
            assertEquals(2, user.getPermissions().size(), "Permissions size must be 2");
            assertThat("Permissions must match",
                user.getPermissions(), hasItems(permissions.toArray(new Permission[0])));
        }
    }

    @Nested
    @DisplayName("public void addAllRoles(Collection<Role> roles)")
    class AddAllRolesTest {
        @Test
        void positiveTypical() {
            User user = new User();
            user.setRoles(new HashSet<>());
            Set<Role> roles = Set.of(TestRoles.ADMIN, TestRoles.USER);
            user.addAllRoles(roles);
            assertEquals(2, user.getRoles().size(), "Role size must be 2");
            assertThat("Roles must match", user.getRoles(), hasItems(roles.toArray(new Role[0])));
        }

        @Test
        void positiveAddition() {
            User user = new User();
            user.setRoles(Set.of(TestRoles.ADMIN));
            Set<Role> roles = Set.of(TestRoles.USER);
            user.addAllRoles(roles);
            assertEquals(2, user.getRoles().size(), "Role size must be 2");
            assertThat("Roles must match", user.getRoles(), hasItems(roles.toArray(new Role[0])));
            assertThat("Roles must contain ADMIN", user.getRoles(), hasItems(TestRoles.ADMIN));
        }

        @Test
        void negativeNullProvidedRoles() {
            User user = new User();
            user.setRoles(new HashSet<>());
            user.addAllRoles(null);
            assertEquals(0, user.getRoles().size(), "Role size must be 0");
        }

        @Test
        void negativeEmptyProvidedRoles() {
            User user = new User();
            user.setRoles(new HashSet<>());
            Set<Role> roles = Set.of();
            user.addAllRoles(roles);
            assertEquals(0, user.getRoles().size(), "Role size must be 0");
        }

        @Test
        void negativeNullRoles() {
            User user = new User();
            user.setRoles(null);
            Set<Role> roles = Set.of(TestRoles.ADMIN, TestRoles.USER);
            user.addAllRoles(roles);
            assertEquals(2, user.getRoles().size(), "Role size must be 2");
            assertThat("Roles must match", user.getRoles(), hasItems(roles.toArray(new Role[0])));
        }
    }

    @Nested
    @DisplayName("public String getUsername()")
    class GetUsernameTest {
        @Test
        void positiveReturnEmail() {
            String email = "schedular@cgi.com";
            User user = new User();
            user.setEmail(email);
            assertEquals(email, user.getUsername(), "User name must match");
            assertEquals(email, user.getEmail(), "Email must match");
        }
    }
}
