package uk.gov.hmcts.juror.standard.datastore.entity.auth;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Accessors(chain = true)
@SuppressWarnings({
    "PMD.ShortClassName",
    "PMD.LinguisticNaming"
})
public class Role {

    @Id
    @Getter
    @Setter
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Permission> permissions;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> inheritedRoles;


    public Role(String name) {
        setName(name);
    }

    public Role setPermissions(Collection<Permission> permissions) {
        this.getPermissionsInternal().clear();
        if (permissions != null) {
            this.getPermissionsInternal().addAll(permissions);
        }
        return this;
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(this.getPermissionsInternal());
    }

    public Set<Permission> getCombinedPermissions() {
        Set<Permission> permissionSet = new HashSet<>(this.getPermissions());
        for (Role inhertiedRole : this.getInheritedRoles()) {
            permissionSet.addAll(inhertiedRole.getCombinedPermissions());
        }
        return Collections.unmodifiableSet(permissionSet);
    }

    public Set<Role> getInheritedRoles() {
        return Collections.unmodifiableSet(this.getInheritedRolesInternal());
    }

    public Role setInheritedRoles(Collection<Role> inheritedRoles) {
        this.getInheritedRolesInternal().clear();

        if (inheritedRoles != null) {
            this.getInheritedRolesInternal().addAll(inheritedRoles);
        }
        return this;
    }

    public void addPermission(Permission permission) {
        this.getPermissionsInternal().add(permission);
    }


    private Set<Permission> getPermissionsInternal() {
        if (this.permissions == null) {
            this.permissions = new HashSet<>();
        }
        return this.permissions;
    }

    private Set<Role> getInheritedRolesInternal() {
        if (this.inheritedRoles == null) {
            this.inheritedRoles = new HashSet<>();
        }
        return this.inheritedRoles;
    }
}
