package co.com.crediya.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.exception.InvalidTokenException;
import co.com.crediya.model.auth.gateways.TokenVerifier;
import co.com.crediya.model.user.Rol;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {
    private static final AuthenticatedUser USER =
            new AuthenticatedUser(UUID.randomUUID(), "ana@example.com", Rol.ASESOR, null);

    private final TokenVerifier verifier = token -> {
        if ("valido".equals(token)) {
            return USER;
        }
        throw new InvalidTokenException("Token inválido.");
    };
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(verifier);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesRequestWithValidToken() throws Exception {
        MockFilterChain chain = run("Bearer valido");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(chain.getRequest());
        assertEquals(USER, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ASESOR".equals(authority.getAuthority())));
    }

    @Test
    void leavesRequestAnonymousWhenTokenIsInvalid() throws Exception {
        run("Bearer falso");

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void leavesRequestAnonymousWithoutAuthorizationHeader() throws Exception {
        run(null);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private MockFilterChain run(String authorization) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        return chain;
    }
}
