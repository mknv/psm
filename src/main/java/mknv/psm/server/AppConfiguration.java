package mknv.psm.server;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author mknv
 */
@Configuration
public class AppConfiguration implements WebMvcConfigurer {

    @Autowired
    private MessageSource messageSource;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
    }

    @Profile("prod")
    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder;
    }
    
    @Profile({"dev","test"})
    @Bean
    public PasswordEncoder testPasswordEncoder(){
        PasswordEncoder passwordEncoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.equals(encodedPassword);
            }
        };
        return passwordEncoder;
    }

    @Bean
    public ErrorAttributes errorAttributes() {
        DefaultErrorAttributes attributes = new DefaultErrorAttributes(false) {

            @Override
            public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
                Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace);
                //Replaces 'No message available' with empty string
                if (errorAttributes.get("message").equals("No message available")) {
                    errorAttributes.put("message", "");
                }
                Throwable error = getError(webRequest);
                if (error instanceof NoHandlerFoundException) {
                    errorAttributes.put("message", messageSource.getMessage("error.page.not.found", null, null));
                }
                return errorAttributes;
            }
        };
        return attributes;
    }
}