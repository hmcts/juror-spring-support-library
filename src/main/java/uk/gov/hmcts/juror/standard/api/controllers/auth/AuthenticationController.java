package uk.gov.hmcts.juror.standard.api.controllers.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.standard.api.model.auth.AssignPermissionsRequest;
import uk.gov.hmcts.juror.standard.api.model.auth.JwtResponse;
import uk.gov.hmcts.juror.standard.api.model.auth.LoginRequest;
import uk.gov.hmcts.juror.standard.api.model.auth.RegisterRequest;
import uk.gov.hmcts.juror.standard.api.model.auth.ResetPasswordRequest;
import uk.gov.hmcts.juror.standard.api.model.auth.UserEmailRequest;
import uk.gov.hmcts.juror.standard.api.model.auth.UserResponse;
import uk.gov.hmcts.juror.standard.api.model.error.InternalServerError;
import uk.gov.hmcts.juror.standard.api.model.error.InvalidPayloadError;
import uk.gov.hmcts.juror.standard.api.model.error.NotFoundError;
import uk.gov.hmcts.juror.standard.api.model.error.UnauthorisedError;
import uk.gov.hmcts.juror.standard.api.model.error.bvr.CannotAssignPermissionsToSelfError;
import uk.gov.hmcts.juror.standard.api.model.error.bvr.CannotDeleteSelfError;
import uk.gov.hmcts.juror.standard.api.model.error.bvr.UserAlreadyRegisteredError;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.UserPermissionConstants;
import uk.gov.hmcts.juror.standard.mapping.UserMapper;
import uk.gov.hmcts.juror.standard.service.contracts.auth.UserService;

@RestController
@Tag(name = "Authentication")
@Slf4j
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE)
@Validated
@ConditionalOnProperty(prefix = "uk.gov.hmcts.juror.security", name = "use-database", havingValue = "true")
@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.AvoidDuplicateLiterals"
})
public class AuthenticationController {

    private final UserService userService;
    private final UserMapper userMapper;

    public AuthenticationController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    @Operation(summary = "Login",
        description = "Generate a Json Web Token to be used for authentication based on " + "user credentials.",
        responses = {@ApiResponse(responseCode = "200", description = "Successfully logged", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = JwtResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid Payload", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = InvalidPayloadError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorised", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = UnauthorisedError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = InternalServerError.class))})})
    public ResponseEntity<JwtResponse> createToken(@Valid @NotNull @RequestBody LoginRequest request) {
        String jwt = userService.authenticate(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('" + UserPermissionConstants.CREATE + "')")
    @Operation(summary = "Register a new user", description = "This operation will register a new user",
        responses = {@ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid Payload", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = InvalidPayloadError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorised", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = UnauthorisedError.class))}),
            @ApiResponse(responseCode = "422", description = "Business Validation Rule", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = UserAlreadyRegisteredError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = InternalServerError.class))})})
    public ResponseEntity<Void> register(@Valid @NotNull @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/user")
    @PreAuthorize("hasAuthority('" + UserPermissionConstants.DELETE + "')")
    @Operation(summary = "Delete a user", description = "This operation will delete a from the system",
        responses = {@ApiResponse(responseCode = "202", description = "User successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid Payload", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = InvalidPayloadError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorised", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = UnauthorisedError.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = NotFoundError.class))}),
            @ApiResponse(responseCode = "422", description = "Business Validation Rule", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = CannotDeleteSelfError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = InternalServerError.class))})})
    public ResponseEntity<Void> deleteUser(@Valid @NotNull @RequestBody UserEmailRequest request) {
        userService.deleteUser(request.getEmail());
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/user/reset_password")
    @PreAuthorize("hasAuthority('" + UserPermissionConstants.PASSWORD_RESET_ALL
        + "') OR (#request.email == authentication.principal.username and hasAuthority('"
        + UserPermissionConstants.PASSWORD_RESET_SELF + "'))")
    @Operation(summary = "Reset a user's password", description = "This operation will change a users password",
        responses = {@ApiResponse(responseCode = "202", description = "Password successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid Payload", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = InvalidPayloadError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorised", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = UnauthorisedError.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = NotFoundError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = InternalServerError.class))})})
    public ResponseEntity<Void> resetPassword(@Valid @NotNull @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PutMapping("/user/permissions")
    @PreAuthorize("hasAuthority('" + UserPermissionConstants.PERMISSIONS_ASSIGN + "')")
    @Operation(summary = "Updates a users permissions", description = "This operation will update a users permissions",
        responses = {@ApiResponse(responseCode = "202", description = "User permissions successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid Payload", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = InvalidPayloadError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorised", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = UnauthorisedError.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = NotFoundError.class))}),
            @ApiResponse(responseCode = "422", description = "Business Validation Rule", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = CannotAssignPermissionsToSelfError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = InternalServerError.class))})})
    public ResponseEntity<JwtResponse> updatePermissions(
        @Valid @NotNull @RequestBody AssignPermissionsRequest request) {
        userService.updatePermissions(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/user")
    @PreAuthorize("hasAuthority('" + UserPermissionConstants.VIEW_ALL + "') "
        + "OR (#request.email == authentication.principal.username " + "and hasAuthority('"
        + UserPermissionConstants.VIEW_SELF + "'))")
    @Operation(summary = "View the users details", description = "View the user details", responses = {
        @ApiResponse(responseCode = "200", description = "Found", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UserResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid Payload", content = {
            @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = InvalidPayloadError.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorised", content = {
            @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = UnauthorisedError.class))}),
        @ApiResponse(responseCode = "404", description = "Not Found", content = {
            @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = NotFoundError.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
            @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = InternalServerError.class))})})
    public ResponseEntity<UserResponse> getUser(@Valid @NotNull @RequestBody UserEmailRequest request) {
        return ResponseEntity.ok(userMapper.toUserResponse(userService.getUser(request.getEmail())));
    }
}
