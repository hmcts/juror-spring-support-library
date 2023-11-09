package uk.gov.hmcts.juror.standard.datastore.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Permission;

import java.util.Set;

@Repository
@ConditionalOnProperty(prefix = "uk.gov.hmcts.juror.security", name = "use-database", havingValue = "true")
public interface PermissionRepository extends JpaRepository<Permission, String> {

    Set<Permission> getPermissionByNameIn(Set<String> permissions);
}
