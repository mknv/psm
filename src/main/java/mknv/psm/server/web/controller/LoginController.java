package mknv.psm.server.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author mknv
 */
@Controller
public class LoginController {

    @RequestMapping(value = "/login")
    public String login() {
        return "login";
    }

    @RequestMapping(value = "/login-failed")
    public String loginFailed() {
        return "login-failed";
    }

    @RequestMapping(value = "/403")
    public String accessDenied() {
        return "403";
    }
}
