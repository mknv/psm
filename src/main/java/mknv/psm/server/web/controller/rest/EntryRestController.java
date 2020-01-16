package mknv.psm.server.web.controller.rest;

import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import mknv.psm.server.model.domain.Entry;
import mknv.psm.server.model.domain.Group;
import mknv.psm.server.model.domain.User;
import mknv.psm.server.model.repository.EntryRepository;
import mknv.psm.server.model.repository.GroupRepository;
import mknv.psm.server.util.PasswordEncryptor;
import mknv.psm.server.util.PasswordGenerator;
import mknv.psm.server.util.PasswordType;
import mknv.psm.server.web.exception.ControllerSecurityException;
import mknv.psm.server.web.exception.EntityNotFoundException;
import mknv.psm.server.model.repository.UserRepository;
import mknv.psm.server.model.service.EntryService;

/**
 *
 * @author mknv
 */
@RestController
@RequestMapping(value = {"/rest", "/api"}, produces = MediaType.APPLICATION_JSON_VALUE)
public class EntryRestController {

    @Autowired
    private PasswordGenerator passwordGenerator;
    @Autowired
    private PasswordEncryptor passwordEncryptor;

    @Autowired
    private EntryRepository entryRepository;
    @Autowired
    private EntryService entryService;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/entries")
    public ResponseEntity find(
            @RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "group", required = true) String group,
            Authentication authentication) {
        User user = userRepository.findByName(authentication.getName());
        //The group parameter may be one of next values:
        //all - any group
        //empty - group is null
        //int value - group id
        if (group.equals("all")) {
            return ResponseEntity.ok(entryService.find(user, name.trim(), null, false));
        }
        if (group.equals("empty")) {
            return ResponseEntity.ok(entryService.find(user, name.trim(), null, true));
        }
        Group currentGroup = null;
        try {
            int groupId = Integer.parseInt(group);
            currentGroup = groupRepository.findByIdFetchUser(groupId);
            if (currentGroup == null) {
                throw new EntityNotFoundException(Group.class, groupId);
            }
            if (!currentGroup.getUser().getName().equals(authentication.getName())) {
                throw new ControllerSecurityException();
            }
        } catch (NumberFormatException e) {
            throw new EntityNotFoundException(Group.class, group);
        }
        return ResponseEntity.ok(entryService.find(user, name.trim(), currentGroup, false));
    }

    @PostMapping(value = "/entries/delete/{id}")
    public ResponseEntity delete(@PathVariable("id") Integer id, Authentication authentication) {
        Entry entry = entryRepository.findByIdFetchAll(id);
        if (entry == null) {
            throw new EntityNotFoundException(Entry.class, id);
        }
        if (!entry.getUser().getName().equals(authentication.getName())) {
            throw new ControllerSecurityException();
        }
        entryRepository.delete(entry);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/entries/generate-password")
    public ResponseEntity generatePassword(@RequestParam("length") Integer length, @RequestParam("type") String type) {
        PasswordType passwordType = null;
        if (type.equals("simple")) {
            passwordType = PasswordType.SIMPLE;
        } else if (type.equals("complex")) {
            passwordType = PasswordType.COMPLEX;
        }
        String password = passwordGenerator.generate(length, passwordType);
        return ResponseEntity.ok(Collections.singletonMap("password", password));
    }

    @GetMapping("/entries/getpassword/{id}")
    public Map<String, String> getPassword(@PathVariable("id") Integer id, Authentication authentication) {
        Entry entry = entryRepository.findByIdFetchAll(id);
        if (entry == null) {
            throw new EntityNotFoundException(Entry.class, id);
        }
        if (!entry.getUser().getName().equals(authentication.getName())) {
            throw new ControllerSecurityException();
        }
        String password = "";
        if (entry.getPassword() != null) {
            password = passwordEncryptor.decrypt(entry.getPassword());
        }
        return Collections.singletonMap("password", password);
    }
}
