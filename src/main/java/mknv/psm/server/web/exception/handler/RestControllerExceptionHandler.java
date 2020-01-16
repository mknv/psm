package mknv.psm.server.web.exception.handler;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import mknv.psm.server.web.exception.ControllerSecurityException;
import mknv.psm.server.web.exception.EntityNotFoundException;
import mknv.psm.server.web.exception.ErrorInfo;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author mknv
 */
@RestControllerAdvice(annotations = RestController.class)
public class RestControllerExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    private static final Logger log = LoggerFactory.getLogger(WebControllerExceptionHandler.class);
    private static final Logger logSecurity = LoggerFactory.getLogger("security");

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = {
        HttpRequestMethodNotSupportedException.class,
        MissingServletRequestParameterException.class,
        EntityNotFoundException.class})
    public ErrorInfo handleNotFound(Exception ex, HttpServletRequest request) {
        log.info("URL: {}. {}", request.getRequestURL(), ex);
        String message = messageSource.getMessage("error.page.not.found", null, null);
        ErrorInfo errorInfo = new ErrorInfo(message);
        return errorInfo;
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorInfo handleBadRequest(Exception ex, HttpServletRequest request){
        log.info("URL: {}. {}", request.getRequestURL(), ex);
        String message = messageSource.getMessage("error.bad.request", null, null);
        ErrorInfo errorInfo = new ErrorInfo(message);
        return errorInfo;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ControllerSecurityException.class)
    public ErrorInfo handleControllerSecurityException(HttpServletRequest request, Authentication authentication) {
        String username = authentication.getName();
        logSecurity.info("Access denied. URL: {}. User: {}", request.getRequestURL(), username);
        String message = messageSource.getMessage("error.access.denied", null, null);
        ErrorInfo errorInfo = new ErrorInfo(message);
        return errorInfo;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorInfo handleException(Exception ex, HttpServletRequest request) {
        log.info("URL: {}. {}", request.getRequestURL(), ex);
        String message = messageSource.getMessage("error.internal.error", null, null);
        ErrorInfo errorInfo = new ErrorInfo(message);
        return errorInfo;
    }
}
