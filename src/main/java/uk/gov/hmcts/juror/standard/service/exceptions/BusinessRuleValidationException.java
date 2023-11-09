package uk.gov.hmcts.juror.standard.service.exceptions;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.juror.standard.api.model.error.bvr.BusinessRuleError;

import java.io.Serial;

public class BusinessRuleValidationException extends GenericErrorHandlerException {

    @Serial
    private static final long serialVersionUID = -3767041549258254732L;

    public BusinessRuleValidationException(BusinessRuleError businessRuleError) {
        super(Type.INFORMATIONAL, businessRuleError, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
