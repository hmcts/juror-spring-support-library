package uk.gov.hmcts.juror.standard.service.contracts.auth;

import uk.gov.hmcts.juror.standard.api.model.auth.AssignPermissionsRequest;
import uk.gov.hmcts.juror.standard.api.model.auth.RegisterRequest;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.User;

public interface UserService {
    String authenticate(String email, String password);

    String register(RegisterRequest request);

    User getUser(String email);

    void deleteUser(String email);

    void resetPassword(String email, String password);

    boolean doesUserExist(String email);

    void updatePermissions(AssignPermissionsRequest request);
}
