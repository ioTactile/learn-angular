package com.learn.api.infrastructure.security;

import com.learn.api.application.auth.UserRepository;
import com.learn.api.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lit Authorization: Bearer &lt;jwt&gt; et peuple le SecurityContext.
 * Rôle et version de session viennent de la base.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository users;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository users) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.users = users;
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
				UUID userId = UUID.fromString(claims.getSubject());
				User user = users.findById(userId).orElse(null);
				Number version = claims.get("ver", Number.class);
				if (user == null || version == null || version.intValue() != user.tokenVersion()) {
					SecurityContextHolder.clearContext();
				}
				else {
					var authentication = new UsernamePasswordAuthenticationToken(
							user.id().toString(),
							null,
							List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()))
					);
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
			catch (JwtException | IllegalArgumentException ignored) {
				SecurityContextHolder.clearContext();
			}
		}

		filterChain.doFilter(request, response);
	}
}
