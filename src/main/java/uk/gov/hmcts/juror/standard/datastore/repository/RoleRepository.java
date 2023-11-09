package uk.gov.hmcts.juror.standard.datastore.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Role;

import java.util.Set;

@Repository
@ConditionalOnProperty(prefix = "uk.gov.hmcts.juror.security", name = "use-database", havingValue = "true")
public interface RoleRepository extends JpaRepository<Role, String> {

    Set<Role> getRolesByNameIsIn(Set<String> name);
}
