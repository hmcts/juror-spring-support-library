package uk.gov.hmcts.juror.standard.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuppressWarnings("PMD.TooManyFields")
public class SoapConfig extends WebConfig {

    @NotBlank
    private String requestLocation;
    @NotBlank
    private String namespace;
    @NotBlank
    private String requestMethod;
    @NotBlank
    private String responseMethod;

    private List<WebConfig> failoverConfig;
}
