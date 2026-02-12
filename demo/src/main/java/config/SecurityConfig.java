package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import util.JwtProvider;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtProvider jwtProvider) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/oauth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}