package mknv.psm.server.web.controller.rest;

import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Mikhail Konovalov
 */
@RestController
public class CsrfController {

    @GetMapping(value = "/rest/csrf", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CsrfToken csrf(CsrfToken csrfToken){
        return csrfToken;
    }
}