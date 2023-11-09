package uk.gov.hmcts.juror.standard.datastore.entity.auth;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
@Setter
@Entity
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    @Id
    private String name;

    public GrantedAuthority toGrantedAuthority() {
        return new SimpleGrantedAuthority(name);
    }
}
