package uk.gov.hmcts.juror.standard.api.model.error;

@SuppressWarnings({
    "PMD.TestClassWithoutTestCases" //False positive done via inheritance
})
class MethodNotSupportedErrorTest extends GenericErrorTest<MethodNotSupportedError> {
    @Override
    protected String getErrorCode() {
        return "METHOD_NOT_SUPPORTED";
    }

    @Override
    protected String getDefaultMessage() {
        return "This method is not supported.";
    }

    @Override
    protected MethodNotSupportedError createErrorObject() {
        return new MethodNotSupportedError();
    }
}
