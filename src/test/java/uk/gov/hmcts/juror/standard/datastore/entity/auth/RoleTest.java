package uk.gov.hmcts.juror.standard.datastore.entity.auth;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.juror.standard.testsupport.TestPermissions;
import uk.gov.hmcts.juror.standard.testsupport.TestRoles;
import uk.gov.hmcts.juror.standard.testsupport.TestUtil;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({
    "PMD.AvoidDuplicateLiterals",
    "PMD.TooManyMethods"
})
class RoleTest {


    @AfterEach
    void afterEach() {
        TestRoles.reset();
    }

    @ParameterizedTest
    @ValueSource(strings = {"MyName1", "Role 2", "Role 3"})
    void positiveConstructorName(String name) {
        Role role = new Role(name);
        assertEquals(name, role.getName(),"Name must match");
    }

    @Test
    void positiveGetPermissionsNull() {
        Role role = new Role();
        Set<Permission> permissionSet = role.getPermissions();
        assertEquals(0, permissionSet.size(), "Size must be 0");
        TestUtil.isUnmodifiable(permissionSet);
    }

    @Test
    void positiveGetPermissions() {
        Role role = new Role();
        role.addPermission(TestPermissions.JOB_RUN);
        Set<Permission> permissionSet = role.getPermissions();
        assertEquals(1, permissionSet.size(), "Size must be 1");
        assertThat("Permission set must contain JOB_RUN", permissionSet, hasItem(TestPermissions.JOB_RUN));
        TestUtil.isUnmodifiable(permissionSet);
        role.addPermission(TestPermissions.USER_PERMISSIONS_ASSIGN);
        permissionSet = role.getPermissions();
        assertEquals(2, permissionSet.size(), "Size must be 2");
        assertThat("Permission set must contain JOB_RUN", permissionSet, hasItem(TestPermissions.JOB_RUN));
        assertThat("Permission set must contain USER_PERMISSIONS_ASSIGN", permissionSet,
            hasItem(TestPermissions.USER_PERMISSIONS_ASSIGN));
    }

    @Test
    void positiveSetPermissionsEmpty() {
        Role role = new Role();
        role.addPermission(TestPermissions.JOB_RUN);
        role.addPermission(TestPermissions.USER_PERMISSIONS_ASSIGN);

        Set<Permission> permissionSet = role.getPermissions();
        assertEquals(2, permissionSet.size(), "Size must be 2");
        assertThat("Permission set must contain JOB_RUN & USER_PERMISSIONS_ASSIGN",
            permissionSet, hasItems(TestPermissions.JOB_RUN, TestPermissions.USER_PERMISSIONS_ASSIGN));
        TestUtil.isUnmodifiable(permissionSet);

        role.setPermissions(Collections.emptySet());
        permissionSet = role.getPermissions();
        assertEquals(0, permissionSet.size(), "Size must be 0");
        TestUtil.isUnmodifiable(permissionSet);
    }

    @Test
    void positiveSetPermissionsNull() {
        Role role = new Role();
        role.addPermission(TestPermissions.JOB_RUN);
        role.addPermission(TestPermissions.USER_PERMISSIONS_ASSIGN);

        Set<Permission> permissionSet = role.getPermissions();
        assertEquals(2, permissionSet.size(), "Size must be 2");
        assertThat("Permission set must contain JOB_RUN & USER_PERMISSIONS_ASSIGN",
            permissionSet, hasItems(TestPermissions.JOB_RUN, TestPermissions.USER_PERMISSIONS_ASSIGN));
        TestUtil.isUnmodifiable(permissionSet);

        role.setPermissions(null);
        permissionSet = role.getPermissions();
        assertEquals(0, permissionSet.size(), "Size must be 0");
        TestUtil.isUnmodifiable(permissionSet);
    }

    @Test
    void positiveSetPermissionsTypical() {
        Role role = new Role();
        role.addPermission(TestPermissions.JOB_RUN);
        role.addPermission(TestPermissions.USER_PERMISSIONS_ASSIGN);

        Set<Permission> permissionSet = role.getPermissions();
        assertEquals(2, permissionSet.size(), "Size must be 2");
        assertThat("Permission set must contain JOB_RUN & USER_PERMISSIONS_ASSIGN",
            permissionSet, hasItems(TestPermissions.JOB_RUN, TestPermissions.USER_PERMISSIONS_ASSIGN));
        TestUtil.isUnmodifiable(permissionSet);

        Set<Permission> newPermissions = Set.of(TestPermissions.USER_CREATE, TestPermissions.USER_DELETE);

        role.setPermissions(newPermissions);
        permissionSet = role.getPermissions();
        assertEquals(newPermissions.size(), permissionSet.size(), "Permission size must match");
        assertThat("Permissions must match", permissionSet, hasItems(newPermissions.toArray(new Permission[0])));
        TestUtil.isUnmodifiable(permissionSet);
    }


    @Test
    void positiveGetInheritedRolesNull() {
        Role role = new Role();
        Set<Role> roleSet = role.getInheritedRoles();
        assertEquals(0, roleSet.size(), "Role size must match");
        TestUtil.isUnmodifiable(roleSet);
    }

    @Test
    void positiveGetInheritedRoles() {
        Set<Role> inheritedRoles = Set.of(TestRoles.USER);
        Role role = new Role();
        role.setInheritedRoles(inheritedRoles);

        Set<Role> roleSet = role.getInheritedRoles();
        assertEquals(1, roleSet.size(), "Role size must match");
        assertThat("Role set must contain USER", roleSet, hasItem(TestRoles.USER));
        TestUtil.isUnmodifiable(roleSet);
    }

    @Test
    void positiveSetInheritedRolesEmpty() {
        Role role = new Role();
        role.setInheritedRoles(Set.of(TestRoles.USER));

        role.setInheritedRoles(Collections.emptySet());
        Set<Role> inheritedRoles = role.getInheritedRoles();
        assertEquals(0, inheritedRoles.size(), "Inherited roles size must be 0");
        TestUtil.isUnmodifiable(inheritedRoles);
    }

    @Test
    void positiveSetInheritedRolesNull() {
        Role role = new Role();
        role.setInheritedRoles(Set.of(TestRoles.USER));

        role.setInheritedRoles(null);
        Set<Role> inheritedRoles = role.getInheritedRoles();
        assertEquals(0, inheritedRoles.size(), "Inherited roles size must be 0");
        TestUtil.isUnmodifiable(inheritedRoles);
    }

    @Test
    void positiveGetCombinedPermissionsNoRolesAndNoPermissions() {
        Role role = TestRoles.USER;
        assertEquals(0, role.getInheritedRoles().size(), "Inherited roles size must be 0");
        assertEquals(0, role.getPermissions().size(), "Permission size must be 0");
        assertEquals(0, role.getCombinedPermissions().size(), "Combined roles size must be 0");
    }

    @Test
    void positiveGetCombinedPermissionsNoRolesButHasPermissions() {
        Role role = TestRoles.USER;
        role.addPermission(TestPermissions.JOB_RUN);
        role.addPermission(TestPermissions.USER_PERMISSIONS_ASSIGN);
        assertEquals(0, role.getInheritedRoles().size(), "Inherited roles size must be 0");
        assertEquals(2, role.getPermissions().size(), "Permission size must be 0");
        Set<Permission> permissions = role.getCombinedPermissions();
        assertEquals(2, permissions.size(), "Combined permission size must be 0");
        assertThat("Permissions must match",
            permissions, hasItems(TestPermissions.JOB_RUN, TestPermissions.USER_PERMISSIONS_ASSIGN));
    }

    @Test
    void positiveGetCombinedPermissionsRolesButNoPermissions() {
        Role userRole = TestRoles.USER;
        userRole.addPermission(TestPermissions.JOB_RUN);
        userRole.addPermission(TestPermissions.USER_PERMISSIONS_ASSIGN);

        assertEquals(0, userRole.getInheritedRoles().size(),"Inherited Roles must match");
        assertEquals(2, userRole.getPermissions().size(),"Permissions must match");

        Role adminRole = TestRoles.ADMIN;
        assertEquals(1, adminRole.getInheritedRoles().size(), "Inherited roles size must be 1");
        assertEquals(0, adminRole.getPermissions().size(), "Permissions size must be 0");

        Set<Permission> permissions = adminRole.getCombinedPermissions();
        assertEquals(2, permissions.size(), "Permission size must be 2");
        assertThat("Permissions must contain JOB_RUN & USER_PERMISSIONS_ASSIGN",
            permissions, hasItems(TestPermissions.JOB_RUN, TestPermissions.USER_PERMISSIONS_ASSIGN));
    }

    @Test
    void positiveGetCombinedPermissionsRolesAndPermissions() {
        Role userRole = TestRoles.USER;
        userRole.addPermission(TestPermissions.JOB_RUN);
        userRole.addPermission(TestPermissions.USER_PERMISSIONS_ASSIGN);

        assertEquals(0, userRole.getInheritedRoles().size(), "Inherited roles size must be 0");
        assertEquals(2, userRole.getPermissions().size(), "Permissions size must be 2");

        Role adminRole = TestRoles.ADMIN;
        adminRole.addPermission(TestPermissions.USER_CREATE);
        adminRole.addPermission(TestPermissions.USER_DELETE);
        assertEquals(1, adminRole.getInheritedRoles().size(), "Inherited roles size must be 1");
        assertEquals(2, adminRole.getPermissions().size(), "Permissions size must be 2");

        Set<Permission> permissions = adminRole.getCombinedPermissions();
        assertEquals(4, permissions.size(), "Combined Permissions size must be 4");
        assertThat(
            "Permissions must contain JOB_RUN & USER_PERMISSIONS_ASSIGN & USER_DELETE & USER_CREATE",
            permissions, hasItems(TestPermissions.JOB_RUN,
                TestPermissions.USER_PERMISSIONS_ASSIGN,
                TestPermissions.USER_DELETE,
                TestPermissions.USER_CREATE));
    }
}
