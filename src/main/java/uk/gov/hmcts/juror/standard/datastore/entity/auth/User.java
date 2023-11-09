package uk.gov.hmcts.juror.standard.datastore.entity.auth;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users") //Required as Postgres does not like the table being called user
@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.ShortClassName"
})
public class User implements UserDetails {

    @Serial
    private static final long serialVersionUID = -788975566569897618L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @Length(min = 5)
    @NotBlank
    private String password;

    @NotBlank
    private String firstname;

    @NotBlank
    private String lastname;

    @ManyToMany(fetch = FetchType.EAGER)
    @Setter(AccessLevel.NONE)
    private Set<Role> roles;

    @ManyToMany(fetch = FetchType.EAGER)
    @Setter(AccessLevel.NONE)
    private Set<Permission> permissions;

    @Builder.Default
    private boolean accountNonExpired = true;
    @Builder.Default
    private boolean accountNonLocked = true;
    @Builder.Default
    private boolean credentialsNonExpired = true;
    @Builder.Default
    private boolean enabled = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        this.getCombinedPermissions().forEach(permission -> grantedAuthorities.add(permission.toGrantedAuthority()));
        return grantedAuthorities;
    }

    public void setRoles(Set<Role> roles) {
        this.getRolesInternal().clear();
        this.addAllRoles(roles);
    }

    public void setPermissions(Set<Permission> permissions) {
        this.getPermissionsInternal().clear();
        this.addAllPermissions(permissions);
    }

    public Set<Permission> getCombinedPermissions() {
        Set<Permission> combinedPermissions = new HashSet<>(this.getPermissions());
        this.getRoles().forEach(role -> combinedPermissions.addAll(role.getCombinedPermissions()));
        return combinedPermissions;
    }

    private Set<Permission> getPermissionsInternal() {
        if (this.permissions == null) {
            this.permissions = new HashSet<>();
        }
        return this.permissions;
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(this.getPermissionsInternal());
    }

    private Set<Role> getRolesInternal() {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        return this.roles;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(this.getRolesInternal());
    }

    public void removeAllPermissions(Collection<Permission> permissions) {
        if (CollectionUtils.isEmpty(permissions)) {
            return;
        }
        this.getPermissionsInternal().removeAll(permissions);
    }

    public void removeAllRoles(Collection<Role> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return;
        }
        this.getRolesInternal().removeAll(roles);
    }

    public void addAllPermissions(Collection<Permission> permissions) {
        if (CollectionUtils.isEmpty(permissions)) {
            return;
        }
        this.getPermissionsInternal().addAll(permissions);
    }

    public void addAllRoles(Collection<Role> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return;
        }
        this.getRolesInternal().addAll(roles);
    }

    @Override
    public String getUsername() {
        return this.getEmail();
    }
}
