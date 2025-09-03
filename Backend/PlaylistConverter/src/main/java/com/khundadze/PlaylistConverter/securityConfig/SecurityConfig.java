package com.khundadze.PlaylistConverter.securityConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/home", "/error").permitAll() // public endpoints
                                                .anyRequest().authenticated() // everything else requires login
                                )
                                .formLogin(form -> form
                                                .defaultSuccessUrl("/loginSuccess", true) // use Spring default login
                                                                                          // page
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .defaultSuccessUrl("/loginSuccess", true))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/home")
                                                .permitAll());

                return http.build();
        }
}
