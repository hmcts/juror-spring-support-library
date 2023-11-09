package uk.gov.hmcts.juror.standard.datastore.entity.auth;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.GrantedAuthority;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PermissionTest {

    @ParameterizedTest
    @ValueSource(strings = {"MyName1", "Permission 2", "Permission 3"})
    void positiveConstructorName(String name) {
        Permission permission = new Permission(name);
        assertEquals(name, permission.getName(),"Permission name must match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"MyName1", "Permission 2", "Permission 3"})
    void positiveSetName(String name) {
        Permission permission = new Permission();
        permission.setName(name);
        assertEquals(name, permission.getName(),"Permission name must match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"MyName1", "Permission 2", "Permission 3"})
    void positiveToGrantedAuthority(String name) {
        Permission permission = new Permission(name);
        GrantedAuthority grantedAuthority = permission.toGrantedAuthority();
        assertEquals(name, grantedAuthority.getAuthority(),"Authority must match");
    }
}
