package uk.gov.hmcts.juror.standard.api.model.error.bvr;

import uk.gov.hmcts.juror.standard.api.model.error.GenericErrorTest;

@SuppressWarnings({
    "PMD.TestClassWithoutTestCases" //False positive done via inheritance
})
class BusinessRuleErrorTest extends GenericErrorTest<BusinessRuleError> {
    @Override
    protected String getErrorCode() {
        return "BUSINESS_RULE_VALIDATION_ERROR";
    }

    @Override
    protected String getDefaultMessage() {
        return null;
    }

    @Override
    protected BusinessRuleError createErrorObject() {
        return new BusinessRuleError();
    }
}
