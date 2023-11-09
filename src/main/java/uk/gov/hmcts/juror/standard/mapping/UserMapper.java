package uk.gov.hmcts.juror.standard.mapping;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.juror.standard.api.model.auth.UserResponse;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Permission;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Role;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.User;

import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class UserMapper {


    @Mapping(target = "combinedPermissions", expression = "java(mapPermissionsToString(user.getCombinedPermissions()))")
    public abstract UserResponse toUserResponse(User user);

    public abstract Set<String> mapRolesToString(Set<Role> value);

    public abstract Set<String> mapPermissionsToString(Set<Permission> value);

    public String mapRoleToString(Role value) {
        return value == null ? null : value.getName();
    }

    public String mapPermissionToString(Permission value) {
        return value == null ? null : value.getName();
    }
}