package mknv.psm.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import mknv.psm.server.web.CustomAuthenticationProvider;

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
                    .antMatchers("/users", "/users/**").hasAuthority("admin")
                    .anyRequest().hasAnyAuthority("admin", "user")
                    .and()
                    .formLogin().permitAll()
                    .loginPage("/login")
                    .failureUrl("/login-failed")
                    .defaultSuccessUrl("/")
                    .and()
                    .logout().permitAll()
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login")
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
            http.antMatcher("/api/**").authorizeRequests()
                    .anyRequest().hasAnyAuthority("admin", "user")
                    .and().httpBasic().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                    .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and().csrf().ignoringAntMatchers("/api/**")
                    .and().requiresChannel().anyRequest().requiresSecure();
        }
    }
}
