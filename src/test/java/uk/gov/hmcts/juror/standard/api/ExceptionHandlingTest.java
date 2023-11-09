package uk.gov.hmcts.juror.standard.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import uk.gov.hmcts.juror.standard.api.model.error.GenericError;
import uk.gov.hmcts.juror.standard.api.model.error.InternalServerError;
import uk.gov.hmcts.juror.standard.api.model.error.InvalidContentTypeError;
import uk.gov.hmcts.juror.standard.api.model.error.InvalidPayloadError;
import uk.gov.hmcts.juror.standard.api.model.error.MethodNotSupportedError;
import uk.gov.hmcts.juror.standard.api.model.error.UnauthorisedError;
import uk.gov.hmcts.juror.standard.service.exceptions.APIHandleableException;
import uk.gov.hmcts.juror.standard.service.exceptions.InternalServerException;
import uk.gov.hmcts.juror.standard.service.exceptions.InvalidEnumValueException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ExceptionHandling.class})
@SuppressWarnings({
    "PMD.JUnitTestsShouldIncludeAssert",
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods",
    "PMD.AvoidDuplicateLiterals"
})
class ExceptionHandlingTest {

    @MockBean
    private Logger logger;

    @Autowired
    @InjectMocks
    private ExceptionHandling exceptionHandling;

    @Test
    void positiveHandleApiHandleException() {
        GenericError payload = new InvalidPayloadError();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        APIHandleableException.Type exceptionType = APIHandleableException.Type.INFORMATIONAL;

        APIHandleableException apiHandleableException = mock(APIHandleableException.class);
        when(apiHandleableException.getExceptionType()).thenReturn(exceptionType);
        when(apiHandleableException.getStatusCode()).thenReturn(status);
        when(apiHandleableException.getErrorObject()).thenReturn(payload);

        ResponseEntity<GenericError> responseEntity =
            exceptionHandling.handleAPIHandleException(apiHandleableException);
        runStandardVerifications(responseEntity, status, payload);
    }

    @Test
    void positiveHandleApiHandleExceptionInternalServerError() {
        GenericError payload = new InvalidPayloadError();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        APIHandleableException.Type exceptionType = APIHandleableException.Type.SYSTEM_ERROR;

        InternalServerException apiHandleableException = mock(InternalServerException.class);
        when(apiHandleableException.getExceptionType()).thenReturn(exceptionType);
        when(apiHandleableException.getStatusCode()).thenReturn(status);
        when(apiHandleableException.getErrorObject()).thenReturn(payload);

        ResponseEntity<GenericError> responseEntity =
            exceptionHandling.handleAPIHandleException(apiHandleableException);
        runStandardVerifications(responseEntity, status, payload);
    }

    @Test
    void positiveHandleConstraintViolationExceptionSingle() {
        InvalidPayloadError expectedPayload = new InvalidPayloadError();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ConstraintViolationException exception = mock(ConstraintViolationException.class);


        Set<ConstraintViolation<?>> constraintViolations = new HashSet<>();
        constraintViolations.add(
            createConstraintViolationMockAndUpdateExpectedResponse(expectedPayload, "field", "message"));
        when(exception.getConstraintViolations()).thenReturn(constraintViolations);

        ResponseEntity<GenericError> responseEntity = exceptionHandling.handleConstraintViolationException(exception);
        runStandardVerifications(responseEntity, status, expectedPayload);
    }

    @Test
    void positiveHandleConstraintViolationExceptionMultiple() {
        InvalidPayloadError expectedPayload = new InvalidPayloadError();


        Set<ConstraintViolation<?>> constraintViolations = new HashSet<>();
        constraintViolations.add(
            createConstraintViolationMockAndUpdateExpectedResponse(expectedPayload, "field", "message"));
        constraintViolations.add(
            createConstraintViolationMockAndUpdateExpectedResponse(expectedPayload, "field1", "message1"));
        constraintViolations.add(
            createConstraintViolationMockAndUpdateExpectedResponse(expectedPayload, "field2", "message2"));

        ConstraintViolationException exception = mock(ConstraintViolationException.class);
        when(exception.getConstraintViolations()).thenReturn(constraintViolations);


        ResponseEntity<GenericError> responseEntity = exceptionHandling.handleConstraintViolationException(exception);

        runStandardVerifications(responseEntity, HttpStatus.BAD_REQUEST, expectedPayload);
    }

    @Test
    void positiveHandleMethodArgumentNotValidExceptionSingle() {
        InvalidPayloadError expectedPayload = new InvalidPayloadError();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);


        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(createFieldErrorMockAndUpdateExpectedResponse(expectedPayload, "field", "message"));
        when(exception.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<GenericError> responseEntity =
            exceptionHandling.handleMethodArgumentNotValidException(exception);
        runStandardVerifications(responseEntity, status, expectedPayload);
    }

    @Test
    void positiveHandleMethodArgumentNotValidExceptionMultiple() {
        InvalidPayloadError expectedPayload = new InvalidPayloadError();


        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(createFieldErrorMockAndUpdateExpectedResponse(expectedPayload, "field", "message"));
        fieldErrors.add(createFieldErrorMockAndUpdateExpectedResponse(expectedPayload, "field1", "message1"));
        fieldErrors.add(createFieldErrorMockAndUpdateExpectedResponse(expectedPayload, "field2", "message2"));


        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<GenericError> responseEntity =
            exceptionHandling.handleMethodArgumentNotValidException(exception);
        runStandardVerifications(responseEntity, HttpStatus.BAD_REQUEST, expectedPayload);
    }

    @Test
    void positiveHandleHttpMessageNotReadableExceptionNoRootCause() {
        InvalidPayloadError expectedPayload = new InvalidPayloadError();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        when(exception.getRootCause()).thenReturn(null);
        expectedPayload.addMessage("Unable to read payload content");

        ResponseEntity<GenericError> responseEntity =
            exceptionHandling.handleHttpMessageNotReadableException(exception);
        runStandardVerifications(responseEntity, status, expectedPayload);
    }

    @Test
    void positiveHandleHttpMessageNotReadableExceptionRootCauseNotEnumValueException() {

        InvalidPayloadError expectedPayload = new InvalidPayloadError();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        when(exception.getRootCause()).thenReturn(new RuntimeException());
        expectedPayload.addMessage("Unable to read payload content");

        ResponseEntity<GenericError> responseEntity =
            exceptionHandling.handleHttpMessageNotReadableException(exception);
        runStandardVerifications(responseEntity, status, expectedPayload);
    }

    @Test
    void positiveHandleHttpMessageNotReadableExceptionRootCauseIsEnumValueException() {
        InvalidPayloadError expectedPayload = new InvalidPayloadError();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        InvalidEnumValueException invalidEnumValueException = new InvalidEnumValueException("This is my error message");
        when(exception.getRootCause()).thenReturn(invalidEnumValueException);
        expectedPayload.addMessage("This is my error message");

        ResponseEntity<GenericError> responseEntity =
            exceptionHandling.handleHttpMessageNotReadableException(exception);
        runStandardVerifications(responseEntity, status, expectedPayload);
    }


    @Test
    void positiveHandleMethodArgumentTypeMismatchException() {
        InvalidPayloadError expectedPayload = new InvalidPayloadError();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        String name = "MyName";
        when(exception.getName()).thenReturn(name);

        expectedPayload.addMessage(name + ": could not be parsed");

        ResponseEntity<GenericError> responseEntity =
            exceptionHandling.handleMethodArgumentTypeMismatchException(exception);
        runStandardVerifications(responseEntity, status, expectedPayload);
    }

    @Test
    void positiveHandleHttpMediaTypeException() {
        InvalidContentTypeError expectedPayload = new InvalidContentTypeError();
        HttpStatus status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        HttpMediaTypeException exception = mock(HttpMediaTypeException.class);
        ResponseEntity<GenericError> responseEntity = exceptionHandling.handleHttpMediaTypeException(exception);
        runStandardVerifications(responseEntity, status, expectedPayload);
    }

    @Test
    void positiveHandleHttpRequestMethodNotSupportedException() {
        MethodNotSupportedError expectedPayload = new MethodNotSupportedError();
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        HttpRequestMethodNotSupportedException exception = mock(HttpRequestMethodNotSupportedException.class);
        ResponseEntity<GenericError> responseEntity =
            exceptionHandling.handleHttpRequestMethodNotSupportedException(exception);
        runStandardVerifications(responseEntity, status, expectedPayload);
    }

    @Test
    void positiveHandleBadCredentialsException() {
        UnauthorisedError expectedPayload = new UnauthorisedError();
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        BadCredentialsException exception = mock(BadCredentialsException.class);
        ResponseEntity<UnauthorisedError> responseEntity = exceptionHandling.handleBadCredentialsException(exception);
        runStandardVerifications(responseEntity, status, expectedPayload);
    }

    @Test
    void positiveHandleAccessDeniedException() {
        UnauthorisedError expectedPayload = new UnauthorisedError();
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        AccessDeniedException exception = mock(AccessDeniedException.class);
        ResponseEntity<UnauthorisedError> responseEntity = exceptionHandling.handleAccessDeniedException(exception);
        runStandardVerifications(responseEntity, status, expectedPayload);
    }

    @Test
    void positiveHandleAuthenticationException() {
        UnauthorisedError expectedPayload = new UnauthorisedError();
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        AuthenticationException exception = mock(AuthenticationException.class);
        ResponseEntity<UnauthorisedError> responseEntity = exceptionHandling.handleAuthenticationException(exception);
        runStandardVerifications(responseEntity, status, expectedPayload);
    }

    @Test
    void positiveCatchAll() {
        InternalServerError expectedPayload = new InternalServerError();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Throwable exception = mock(Throwable.class);
        ResponseEntity<GenericError> responseEntity = exceptionHandling.catchAll(exception);
        runStandardVerifications(responseEntity, status, expectedPayload);
    }

    private FieldError createFieldErrorMockAndUpdateExpectedResponse(InvalidPayloadError expectedResponse, String field,
                                                                     String message) {
        expectedResponse.addMessage(field + ": " + message);
        return createFieldErrorMock(field, message);
    }

    private FieldError createFieldErrorMock(String field, String message) {
        FieldError fieldError = mock(FieldError.class);
        when(fieldError.getField()).thenReturn(field);
        when(fieldError.getDefaultMessage()).thenReturn(message);
        return fieldError;
    }

    private ConstraintViolation<?> createConstraintViolationMockAndUpdateExpectedResponse(
        InvalidPayloadError expectedResponse, String field, String message) {
        expectedResponse.addMessage(field + ": " + message);
        return createConstraintViolationMock(field, message);
    }

    private ConstraintViolation<?> createConstraintViolationMock(String field, String message) {
        ConstraintViolation<?> constraintViolation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn(field);
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        when(constraintViolation.getMessage()).thenReturn(message);
        return constraintViolation;
    }


    private <T extends GenericError> void runStandardVerifications(ResponseEntity<T> responseEntity, HttpStatus status,
                                                                   GenericError payload) {
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, responseEntity.getHeaders().getContentType(),
            "Content type must match");
        assertEquals(status.value(), responseEntity.getStatusCode().value(), "Status code must match");
        assertNotNull(responseEntity.getBody(), "Body must not be null");
        assertEquals(payload.getClass(), responseEntity.getBody().getClass(), "Response entity class must match");
        assertEquals(payload.getCode(), responseEntity.getBody().getCode(), "Response body code must match");
        if (payload.getMessages() != null) {
            assertEquals(payload.getMessages().size(), responseEntity.getBody().getMessages().size(),
                "Message size must match");
            assertThat("Messages must match", responseEntity.getBody().getMessages(),
                Matchers.hasItems(payload.getMessages().toArray(new String[0])));
        } else {
            assertNull(responseEntity.getBody().getMessages(), "Messages must be null");
        }
    }
}
