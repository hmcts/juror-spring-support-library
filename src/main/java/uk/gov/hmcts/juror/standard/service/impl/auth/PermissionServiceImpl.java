package uk.gov.hmcts.juror.standard.service.impl.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Permission;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Role;
import uk.gov.hmcts.juror.standard.datastore.repository.PermissionRepository;
import uk.gov.hmcts.juror.standard.service.contracts.auth.PermissionService;
import uk.gov.hmcts.juror.standard.service.contracts.auth.RoleService;
import uk.gov.hmcts.juror.standard.service.exceptions.InvalidPermissionValueException;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
@ConditionalOnProperty(prefix = "uk.gov.hmcts.juror.security", name = "use-database", havingValue = "true")
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    private final RoleService roleService;

    public PermissionServiceImpl(PermissionRepository permissionRepository,
                                 RoleService roleService) {
        this.permissionRepository = permissionRepository;
        this.roleService = roleService;
    }

    @Override
    public Set<Permission> getPermissions(Set<String> permissionsString) {
        if (permissionsString == null || permissionsString.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Permission> permissions = permissionRepository.getPermissionByNameIn(permissionsString);
        if (permissions.size() != permissionsString.size()) {
            permissions.forEach(permission -> permissionsString.remove(permission.getName()));
            throw new InvalidPermissionValueException(
                    "One or more permissions could not be located: " + permissionsString);
        }
        return permissions;
    }

    @Override
    @Transactional
    public Permission getOrCreatePermission(String permission) {
        return getOrCreatePermission(permission, Collections.emptySet()
        );
    }

    @Override
    @Transactional
    public Permission getOrCreatePermission(String permissionName, Set<Role> roles) {
        Optional<Permission> permissionOptional = permissionRepository.findById(permissionName);
        Permission permission = permissionOptional.orElseGet(() -> new Permission(permissionName));
        permission = permissionRepository.save(permission);
        roleService.assignPermission(permission,roles);
        return permission;
    }
}
