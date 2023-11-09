package uk.gov.hmcts.juror.standard.testsupport;

import uk.gov.hmcts.juror.standard.datastore.entity.auth.Permission;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.UserPermissionConstants;

@SuppressWarnings({
    "PMD.TestClassWithoutTestCases"
})
public final class TestPermissions {
    public static final Permission USER_CREATE = new Permission(UserPermissionConstants.CREATE);
    public static final Permission USER_DELETE = new Permission(UserPermissionConstants.DELETE);
    public static final Permission USER_PERMISSIONS_ASSIGN = new Permission(UserPermissionConstants.PERMISSIONS_ASSIGN);
    public static final Permission JOB_RUN = new Permission(UserPermissionConstants.PASSWORD_RESET_ALL);

    private TestPermissions() {

    }
}
