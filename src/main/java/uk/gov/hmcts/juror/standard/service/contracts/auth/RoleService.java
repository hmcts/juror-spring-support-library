package uk.gov.hmcts.juror.standard.service.contracts.auth;

import uk.gov.hmcts.juror.standard.datastore.entity.auth.Permission;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Role;

import java.util.Set;

public interface RoleService {
    Set<Role> getRoles(Set<String> roles);

    Role getOrCreateRole(String roleName);

    Role getOrCreateRole(String roleName, Set<Role> inheritedRoles);

    void assignPermission(Permission permission, Set<Role> roles);
}
