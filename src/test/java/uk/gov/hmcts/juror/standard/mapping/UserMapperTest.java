package uk.gov.hmcts.juror.standard.mapping;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Permission;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings({
    "PMD.AvoidDuplicateLiterals"
})
class UserMapperTest {

    private final UserMapper userMapper;

    public UserMapperTest() {
        this.userMapper = new UserMapperImpl();
    }

    @Test
    void mapRoleToStringNull() {
        assertNull(userMapper.mapRoleToString(null),
            "Role must be null if null is given");
    }

    @Test
    void mapRoleToStringNotNull() {
        assertEquals("myRole", userMapper.mapRoleToString(new Role("myRole")),
            "Role name must be returned");
    }

    @Test
    void mapPermissionToStringNull() {
        assertNull(userMapper.mapPermissionToString(null),
            "Permission must be null if null is given");
    }

    @Test
    void mapPermissionToStringNotNull() {
        assertEquals("myRole", userMapper.mapPermissionToString(new Permission("myRole")),
            "Permission name must be returned");
    }

}
