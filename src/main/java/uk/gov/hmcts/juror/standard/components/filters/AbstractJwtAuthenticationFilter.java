package uk.gov.hmcts.juror.standard.components.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.juror.standard.service.contracts.auth.JwtService;

import java.io.IOException;

@Slf4j
public abstract class AbstractJwtAuthenticationFilter
    extends OncePerRequestFilter
    implements JwtAuthenticationFilter {

    protected final JwtService jwtService;

    abstract UserDetails getUserDetails(String jwt);

    protected AbstractJwtAuthenticationFilter(JwtService jwtService) {
        super();
        this.jwtService = jwtService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request,
                                 @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
        throws ServletException, IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.trace("Missing or incorrect auth header");
            filterChain.doFilter(request, response);
            return;
        }
        final String jwt = authHeader.substring(7);
        UserDetails userDetails = getUserDetails(jwt);
        if (userDetails != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.trace("User details found and user not already authentication");
            if (jwtService.isJwtValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                log.trace("Jwt is not valid");
            }
        } else {
            log.trace("User details not found or user already authentication");

        }
        filterChain.doFilter(request, response);
    }
}
