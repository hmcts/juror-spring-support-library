package uk.gov.hmcts.juror.standard.mapping;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.standard.api.model.auth.UserResponse;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Permission;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Role;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.User;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-17T19:23:02+0100",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.42.50.v20250729-0351, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl extends UserMapper {

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.email( user.getEmail() );
        userResponse.firstname( user.getFirstname() );
        userResponse.lastname( user.getLastname() );
        userResponse.permissions( mapPermissionsToString( user.getPermissions() ) );
        userResponse.roles( mapRolesToString( user.getRoles() ) );

        userResponse.combinedPermissions( mapPermissionsToString(user.getCombinedPermissions()) );

        return userResponse.build();
    }

    @Override
    public Set<String> mapRolesToString(Set<Role> value) {
        if ( value == null ) {
            return null;
        }

        Set<String> set = new LinkedHashSet<String>( Math.max( (int) ( value.size() / .75f ) + 1, 16 ) );
        for ( Role role : value ) {
            set.add( mapRoleToString( role ) );
        }

        return set;
    }

    @Override
    public Set<String> mapPermissionsToString(Set<Permission> value) {
        if ( value == null ) {
            return null;
        }

        Set<String> set = new LinkedHashSet<String>( Math.max( (int) ( value.size() / .75f ) + 1, 16 ) );
        for ( Permission permission : value ) {
            set.add( mapPermissionToString( permission ) );
        }

        return set;
    }
}
