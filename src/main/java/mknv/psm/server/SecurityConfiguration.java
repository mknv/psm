package mknv.psm.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import mknv.psm.server.web.auth.CustomAuthenticationProvider;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 *
 * @author mknv
 */
@EnableWebSecurity
public class SecurityConfiguration {

    @Configuration
    public static class MvcWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private CustomAuthenticationProvider authenticationProvider;

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(authenticationProvider);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers("/css/**", "/images/**", "/js/**").permitAll()
                    .antMatchers("/users/**").hasAuthority("admin")
                    .antMatchers("/rest/csrf").permitAll()
                    .anyRequest().hasAnyAuthority("admin", "user")
                    .and()
                    .formLogin()
                    .loginPage("/login")
                    .failureUrl("/login-failed")
                    .defaultSuccessUrl("/")
                    .permitAll()
                    .and()
                    .logout()
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login")
                    .permitAll()
                    .and().exceptionHandling().accessDeniedPage("/403")
                    .and().requiresChannel().anyRequest().requiresSecure();
        }
    }

    @Configuration
    @Order(1)
    public static class RestApiWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private CustomAuthenticationProvider authenticationProvider;

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(authenticationProvider);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/api/**")
                    .authorizeRequests()
                    .anyRequest().hasAnyAuthority("admin", "user")
                    .and()
                    .csrf().disable()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .requiresChannel().anyRequest().requiresSecure()
                    .and()
                    .httpBasic();
        }
    }
}
