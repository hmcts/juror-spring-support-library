package uk.gov.hmcts.juror.standard.service.impl.auth;

import io.jsonwebtoken.lang.Collections;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.standard.api.model.auth.AssignPermissionsRequest;
import uk.gov.hmcts.juror.standard.api.model.auth.RegisterRequest;
import uk.gov.hmcts.juror.standard.api.model.error.bvr.CannotAssignPermissionsToSelfError;
import uk.gov.hmcts.juror.standard.api.model.error.bvr.CannotDeleteSelfError;
import uk.gov.hmcts.juror.standard.api.model.error.bvr.UserAlreadyRegisteredError;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Permission;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Role;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.User;
import uk.gov.hmcts.juror.standard.datastore.repository.UserRepository;
import uk.gov.hmcts.juror.standard.service.contracts.auth.JwtService;
import uk.gov.hmcts.juror.standard.service.contracts.auth.PermissionService;
import uk.gov.hmcts.juror.standard.service.contracts.auth.RoleService;
import uk.gov.hmcts.juror.standard.service.contracts.auth.UserService;
import uk.gov.hmcts.juror.standard.service.exceptions.BusinessRuleValidationException;
import uk.gov.hmcts.juror.standard.service.exceptions.InternalServerException;
import uk.gov.hmcts.juror.standard.service.exceptions.NotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Service
@ConditionalOnProperty(prefix = "uk.gov.hmcts.juror.security", name = "use-database", havingValue = "true")
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PermissionService permissionService;

    public UserServiceImpl(UserRepository userRepository,
                           RoleService roleService,
                           PermissionService permissionService,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }


    @Override
    public String authenticate(String email, String password) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );
        return jwtService.generateJwtToken(getUser(email));
    }

    @Override
    public String register(RegisterRequest request) {
        if (doesUserExist(request.getEmail())) {
            throw new BusinessRuleValidationException(new UserAlreadyRegisteredError());
        }
        Set<Role> roles = roleService.getRoles(request.getRoles());
        Set<Permission> permissions = permissionService.getPermissions(request.getPermissions());
        User user = User.builder()
            .firstname(request.getFirstname())
            .lastname(request.getLastname())
            .email(request.getEmail())
            .roles(roles)
            .permissions(permissions)
            .password(passwordEncoder.encode(request.getPassword()))
            .build();
        user = save(user);
        return jwtService.generateJwtToken(user);
    }

    @Override
    public User getUser(String email) {
        Optional<User> userOptional = userRepository.findUserByEmail(email);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        throw new NotFoundException("User with email: " + email + " not found");
    }

    private User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean doesUserExist(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @SuppressWarnings("PMD.UselessParentheses")
    public void updatePermissions(AssignPermissionsRequest request) {
        if (isUserSameAsAuthenticatedUser(request.getEmail())) {
            throw new BusinessRuleValidationException(new CannotAssignPermissionsToSelfError());
        }

        Predicate<AssignPermissionsRequest.RolePermissions> isEmpty =
            rolePermissions -> rolePermissions == null
                ||
                (Collections.isEmpty(rolePermissions.getPermissions())
                    && Collections.isEmpty(rolePermissions.getRoles()));


        if (isEmpty.test(request.getAdd()) && isEmpty.test(request.getRemove())) {
            return;
        }
        User user = getUser(request.getEmail());
        if (!isEmpty.test(request.getAdd())) {
            Set<Permission> permissions = permissionService.getPermissions(request.getAdd().getPermissions());
            Set<Role> roles = roleService.getRoles(request.getAdd().getRoles());

            user.addAllPermissions(permissions);
            user.addAllRoles(roles);
        }
        if (!isEmpty.test(request.getRemove())) {
            Set<Permission> permissions = permissionService.getPermissions(request.getRemove().getPermissions());
            Set<Role> roles = roleService.getRoles(request.getRemove().getRoles());

            user.removeAllPermissions(permissions);
            user.removeAllRoles(roles);
        }
        save(user);
    }

    @PreAuthorize("isFullyAuthenticated()")
    private boolean isUserSameAsAuthenticatedUser(String email) {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername().equalsIgnoreCase(email);
        }
        throw new InternalServerException("Unable to check if user is same as authenticated");
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        if (isUserSameAsAuthenticatedUser(email)) {
            throw new BusinessRuleValidationException(new CannotDeleteSelfError());
        }
        if (!doesUserExist(email)) {
            throw new NotFoundException("User with email: " + email + " not found");
        }
        userRepository.deleteByEmail(email);
    }

    @Override
    public void resetPassword(String email, String password) {
        User user = getUser(email);
        user.setPassword(passwordEncoder.encode(password));
        save(user);
    }
}
