package uk.gov.hmcts.juror.standard.api;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.standard.api.model.error.GenericError;
import uk.gov.hmcts.juror.standard.api.model.error.InternalServerError;
import uk.gov.hmcts.juror.standard.api.model.error.InvalidContentTypeError;
import uk.gov.hmcts.juror.standard.api.model.error.InvalidPayloadError;
import uk.gov.hmcts.juror.standard.api.model.error.MethodNotSupportedError;
import uk.gov.hmcts.juror.standard.api.model.error.UnauthorisedError;
import uk.gov.hmcts.juror.standard.service.exceptions.APIHandleableException;
import uk.gov.hmcts.juror.standard.service.exceptions.InvalidEnumValueException;

@ControllerAdvice
@Slf4j
@SuppressWarnings({
    "PMD.TooManyMethods"
})
public class ExceptionHandling extends ResponseEntityExceptionHandler {
    @ExceptionHandler(APIHandleableException.class)
    protected ResponseEntity<GenericError> handleAPIHandleException(APIHandleableException ex) {
        switch (ex.getExceptionType()) {
            case SYSTEM_ERROR -> log.error("A unexpected exception has occurred", ex);
            case USER_ERROR, INFORMATIONAL -> log.debug("A user exception has occurred", ex);
            default -> log.error("An unexpected Exception type has occurred: " + ex.getExceptionType());
        }
        return ResponseEntity.status(ex.getStatusCode()).contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(ex.getErrorObject());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<GenericError> handleConstraintViolationException(ConstraintViolationException exception) {
        InvalidPayloadError invalidPayloadError = new InvalidPayloadError();
        exception.getConstraintViolations().forEach(constraintViolation -> invalidPayloadError.addMessage(
            constraintViolation.getPropertyPath() + ": " + constraintViolation.getMessage()));
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_PROBLEM_JSON).body(invalidPayloadError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<GenericError> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException exception) {
        InvalidPayloadError invalidPayloadError = new InvalidPayloadError();
        exception.getFieldErrors().forEach(fieldError -> invalidPayloadError.addMessage(
            fieldError.getField() + ": " + fieldError.getDefaultMessage()));
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_PROBLEM_JSON).body(invalidPayloadError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<GenericError> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException exception) {
        if (exception.getRootCause() != null
            && exception.getRootCause() instanceof InvalidEnumValueException invalidEnumValueException) {
            return handleAPIHandleException(invalidEnumValueException);
        }
        log.error("HttpMessageNotReadableException raised", exception);
        GenericError invalidPayloadError = new InvalidPayloadError().addMessage("Unable to read payload content");
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_PROBLEM_JSON).body(invalidPayloadError);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<GenericError> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException exception) {
        GenericError invalidPayloadError = new InvalidPayloadError()
            .addMessage("Missing Parameter: " + exception.getParameterName());
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_PROBLEM_JSON).body(invalidPayloadError);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<GenericError> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException exception) {
        GenericError genericError = new InvalidPayloadError().addMessage(exception.getName() + ": could not be parsed");
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_PROBLEM_JSON).body(genericError);
    }

    //Also covers HttpMediaTypeNotSupportedException
    @ExceptionHandler(HttpMediaTypeException.class)
    protected ResponseEntity<GenericError> handleHttpMediaTypeException(HttpMediaTypeException exception) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(new InvalidContentTypeError());
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<GenericError> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(new MethodNotSupportedError());
    }


    //TODO consider removing
    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<UnauthorisedError> handleBadCredentialsException(BadCredentialsException exception) {
        return getUnauthorisedResponse();
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<UnauthorisedError> handleAccessDeniedException(AccessDeniedException exception) {
        return getUnauthorisedResponse();
    }

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<UnauthorisedError> handleAuthenticationException(AuthenticationException exception) {
        return getUnauthorisedResponse();
    }

    private ResponseEntity<UnauthorisedError> getUnauthorisedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(new UnauthorisedError());
    }


    @ExceptionHandler(Throwable.class)
    protected ResponseEntity<GenericError> catchAll(Throwable throwable) {
        log.error("Unexpected throwable raised: " + throwable.getMessage(), throwable);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(new InternalServerError());
    }
}
