package com.hexaware.cozy_heaven.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hexaware.cozy_heaven.security.CustomUserDetailsService;
import com.hexaware.cozy_heaven.security.JwtAuthenticationEntryPoint;
import com.hexaware.cozy_heaven.security.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private JwtAuthenticationFilter jwtAuthenticationFilter;

	private CustomUserDetailsService customUserDetailsService;

	private JwtAuthenticationEntryPoint entryPoint;
	
	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, CustomUserDetailsService customUserDetailsService, JwtAuthenticationEntryPoint entryPoint) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.customUserDetailsService = customUserDetailsService;
		this.entryPoint = entryPoint;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) {

		return config.getAuthenticationManager();
	}

	@Bean
	DaoAuthenticationProvider authenticationProvider() {

		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);

		provider.setPasswordEncoder(passwordEncoder());

		return provider;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) {

		http

				.csrf(csrf -> csrf.disable())

				.cors(Customizer.withDefaults())

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.exceptionHandling(exception -> exception.authenticationEntryPoint(entryPoint))

				.authenticationProvider(authenticationProvider())

				.authorizeHttpRequests(auth -> auth

						// Public APIs
						.requestMatchers("/auth/**").permitAll().requestMatchers(HttpMethod.POST, "/users").permitAll()

						// Allow Swagger
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

						// Public APIs (home endpoint used by keep-alive cron)
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()

						// Admin APIs
						.requestMatchers("/users/**").authenticated()
						.requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")

						// Remaining APIs require login
						.anyRequest().authenticated())

				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

}