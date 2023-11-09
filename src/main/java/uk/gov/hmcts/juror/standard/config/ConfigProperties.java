package uk.gov.hmcts.juror.standard.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "uk.gov.hmcts.juror")
@Getter
@Setter
public class ConfigProperties {

    @NotNull
    @NestedConfigurationProperty
    private SecurityConfigProperties security;

    @Getter
    @Setter
    public static class SecurityConfigProperties {

        @NotNull
        private boolean useDatabase;

        @NotNull
        private long tokenValidity;

        @NotBlank
        private String secret;

        @NotNull
        @NestedConfigurationProperty
        private AdminAccountConfigProperties adminUser;

        @NotNull
        @NestedConfigurationProperty
        private JurorApiServiceAuthentication jurorApiServiceAuthentication;

        @Getter
        @Setter
        public static class AdminAccountConfigProperties {
            @NotNull
            @Email
            private String email;
            @NotNull
            private String password;
            @NotNull
            private String firstname;
            @NotNull
            private String lastname;
        }

        @Getter
        @Setter
        public static class JurorApiServiceAuthentication {
            @NotNull
            private String id;
            @NotNull
            private String issuer;
            @NotNull
            private String subject;
            @NotNull
            private Long tokenValidity;
            @NotNull
            private String secret;
        }
    }
}
