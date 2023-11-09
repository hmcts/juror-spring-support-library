package uk.gov.hmcts.juror.standard.api.model.error;

@SuppressWarnings({
    "PMD.TestClassWithoutTestCases" //False positive done via inheritance
})
class NotFoundErrorTest extends GenericErrorTest<NotFoundError> {
    @Override
    protected String getErrorCode() {
        return "NOT_FOUND";
    }

    @Override
    protected String getDefaultMessage() {
        return "The requested resource could not be located.";
    }

    @Override
    protected NotFoundError createErrorObject() {
        return new NotFoundError();
    }
}
