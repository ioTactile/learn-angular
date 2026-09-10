package com.learn.api.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lit Authorization: Bearer &lt;jwt&gt; et peuple le SecurityContext.
 * Équivalent d'un preHandler Fastify / middleware Express qui attache req.user.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			try {
				Claims claims = jwtTokenProvider.parse(token);
				String role = claims.get("role", String.class);
				if (role == null || role.isBlank()) {
					role = "USER";
				}
				var authentication = new UsernamePasswordAuthenticationToken(
						claims.getSubject(),
						null,
						List.of(new SimpleGrantedAuthority("ROLE_" + role))
				);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			catch (JwtException | IllegalArgumentException ignored) {
				SecurityContextHolder.clearContext();
			}
		}

		filterChain.doFilter(request, response);
	}
}
