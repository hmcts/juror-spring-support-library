package uk.gov.hmcts.juror.standard.api.model.error;

@SuppressWarnings({
    "PMD.TestClassWithoutTestCases" //False positive done via inheritance
})
class UnauthorisedErrorTest extends GenericErrorTest<UnauthorisedError> {
    @Override
    protected String getErrorCode() {
        return "UNAUTHORISED";
    }

    @Override
    protected String getDefaultMessage() {
        return "You are not authorised";
    }

    @Override
    protected UnauthorisedError createErrorObject() {
        return new UnauthorisedError();
    }
}
