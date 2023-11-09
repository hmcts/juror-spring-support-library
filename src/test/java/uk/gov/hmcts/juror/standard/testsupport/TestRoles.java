package uk.gov.hmcts.juror.standard.testsupport;

import uk.gov.hmcts.juror.standard.datastore.entity.auth.Role;

import java.util.Collections;
import java.util.Set;

@SuppressWarnings({
    "PMD.TestClassWithoutTestCases"
})
public final class TestRoles {
    public static final Role USER = new Role().setName("USER");
    public static final Role ADMIN = new Role().setName("ADMIN").setInheritedRoles(Set.of(USER));

    private TestRoles() {

    }

    public static void reset() {
        USER.setPermissions(Collections.emptySet());
        ADMIN.setPermissions(Collections.emptySet());
    }
}
