package mknv.psm.server.web.controller.rest;

import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author mknv
 */
@RestController
public class CsrfController {

    @GetMapping(value = "/rest/csrf", produces = MediaType.APPLICATION_JSON_VALUE)
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }
}