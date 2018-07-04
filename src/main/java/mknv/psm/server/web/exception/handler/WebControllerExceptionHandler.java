package mknv.psm.server.web.exception.handler;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import mknv.psm.server.web.exception.ControllerSecurityException;
import mknv.psm.server.web.exception.EntityNotFoundException;

/**
 *
 * @author mknv
 */
@ControllerAdvice(annotations = Controller.class)
public class WebControllerExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebControllerExceptionHandler.class);
    private static final Logger logSecurity = LoggerFactory.getLogger("security");

    @Autowired
    private MessageSource messageSource;

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = {
        MethodArgumentTypeMismatchException.class,
        HttpRequestMethodNotSupportedException.class,
        MissingServletRequestParameterException.class,
        EntityNotFoundException.class})
    public ModelAndView handleBadRequest(Exception ex, HttpServletRequest request) {
        log.info("URL: {}. {}", request.getRequestURL(), ex);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", messageSource.getMessage("error.page.not.found", null, null));
        return mav;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ControllerSecurityException.class)
    public ModelAndView handleControllerSecurityException(HttpServletRequest request, Authentication authentication) {
        String username = authentication.getName();
        logSecurity.info("Access denied. URL: {}. User: {}", request.getRequestURL(), username);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", messageSource.getMessage("error.access.denied", null, null));
        return mav;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex, HttpServletRequest request) {
        log.info("URL: {}. {}", request.getRequestURL(), ex);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", messageSource.getMessage("error.internal.error", null, null));
        return mav;
    }
}
