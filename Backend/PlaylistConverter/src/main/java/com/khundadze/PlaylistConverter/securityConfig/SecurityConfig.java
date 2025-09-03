package com.khundadze.PlaylistConverter.securityConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/home").permitAll()
                                                .anyRequest().authenticated() // require login everywhere
                                )
                                .formLogin(form -> form
                                                .loginPage("/login") // custom form login page
                                                .defaultSuccessUrl("/loginSuccess", true)
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                // allow multiple providers configured in application.properties / yml
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/loginSuccess", true));

                return http.build();
        }
}
