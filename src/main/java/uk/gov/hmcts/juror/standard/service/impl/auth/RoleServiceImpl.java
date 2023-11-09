package uk.gov.hmcts.juror.standard.service.impl.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Permission;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Role;
import uk.gov.hmcts.juror.standard.datastore.repository.RoleRepository;
import uk.gov.hmcts.juror.standard.service.contracts.auth.RoleService;
import uk.gov.hmcts.juror.standard.service.exceptions.InvalidRoleValueException;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
@ConditionalOnProperty(prefix = "uk.gov.hmcts.juror.security", name = "use-database", havingValue = "true")
public class RoleServiceImpl implements RoleService {


    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Set<Role> getRoles(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Role> roles = roleRepository.getRolesByNameIsIn(roleNames);
        if (roles.size() != roleNames.size()) {
            roles.forEach(role -> roleNames.remove(role.getName()));
            throw new InvalidRoleValueException("One or more roles could not be located: " + roleNames);
        }
        return roles;
    }

    @Override
    @Transactional
    public Role getOrCreateRole(String roleName) {
        return getOrCreateRole(roleName, Collections.emptySet());
    }

    @Override
    @Transactional
    public Role getOrCreateRole(String roleName, Set<Role> inheritedRoles) {
        Optional<Role> roleOptional = roleRepository.findById(roleName);
        Role role = roleOptional.orElseGet(() -> new Role(roleName));
        role.setInheritedRoles(inheritedRoles);
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public void assignPermission(Permission permission, Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return;
        }
        roles.forEach(role -> role.addPermission(permission));
        roleRepository.saveAll(roles);
    }
}
