package uk.gov.hmcts.juror.standard.datastore.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.User;

import java.util.Optional;

@Repository
@ConditionalOnProperty(prefix = "uk.gov.hmcts.juror.security", name = "use-database", havingValue = "true")
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmail(String email);

    boolean existsByEmail(String email);

    void deleteByEmail(String email);
}
