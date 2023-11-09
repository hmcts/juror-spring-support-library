package uk.gov.hmcts.juror.standard.service.contracts.auth;

import uk.gov.hmcts.juror.standard.datastore.entity.auth.Permission;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Role;

import java.util.Set;

public interface PermissionService {
    Set<Permission> getPermissions(Set<String> permissions);

    Permission getOrCreatePermission(String permission);

    Permission getOrCreatePermission(String permission, Set<Role> roles);
}
