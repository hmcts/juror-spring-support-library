package uk.gov.hmcts.juror.standard.api.model.error;

@SuppressWarnings({
    "PMD.TestClassWithoutTestCases" //False positive done via inheritance
})
class InvalidContentTypeErrorTest extends GenericErrorTest<InvalidContentTypeError> {
    @Override
    protected String getErrorCode() {
        return "INVALID_CONTENT_TYPE";
    }

    @Override
    protected String getDefaultMessage() {
        return "Content Type must be application/json";
    }

    @Override
    protected InvalidContentTypeError createErrorObject() {
        return new InvalidContentTypeError();
    }
}
