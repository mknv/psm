package mknv.psm.server.web.controller.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import mknv.psm.server.model.domain.Group;
import mknv.psm.server.model.domain.User;
import mknv.psm.server.model.repository.GroupRepository;
import mknv.psm.server.model.repository.UserRepository;

/**
 *
 * @author mknv
 */
@RestController
@RequestMapping(value = {"/rest", "/api"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class GroupRestController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;

    @GetMapping("/groups")
    public List<Group> list(Authentication authentication) {
        User user = userRepository.findByName(authentication.getName());
        return groupRepository.findByUser(user);
    }
}
