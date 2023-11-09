package uk.gov.hmcts.juror.standard.datastore.entity.auth;

public final class UserPermissionConstants {
    public static final String CREATE = "user::create";
    public static final String VIEW_SELF = "user::view::self";
    public static final String VIEW_ALL = "user::view::all";
    public static final String PERMISSIONS_ASSIGN = "user::permissions::assign";
    public static final String DELETE = "user::delete";
    public static final String PASSWORD_RESET_SELF = "user::password::reset::self";
    public static final String PASSWORD_RESET_ALL = "user::password::reset::all";

    private UserPermissionConstants() {

    }

}
