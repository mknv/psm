package mknv.psm.server.web.controller.rest;

import java.util.Collections;
import java.util.List;
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

/**
 *
 * @author mknv
 */
@RestController
@RequestMapping(value = {"/rest", "/api"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class EntryRestController {

    @Autowired
    private PasswordGenerator passwordGenerator;
    @Autowired
    private PasswordEncryptor passwordEncryptor;

    @Autowired
    private EntryRepository entryRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/entries")
    public ResponseEntity findAll(@RequestParam(value = "name", required = false) String name,
            Authentication authentication) {
        User user = userRepository.findByName(authentication.getName());
        if (name == null) {
            return ResponseEntity.ok(entryRepository.find(user));
        } else {
            return ResponseEntity.ok(entryRepository.find(name, user));
        }
    }

    @GetMapping("/entries/group/{id}")
    public ResponseEntity findByGroup(@PathVariable("id") Integer id, Authentication authentication) {
        Group group = groupRepository.findByIdFetchUser(id);
        if (group == null) {
            throw new EntityNotFoundException(Entry.class, id);
        }
        if (!group.getUser().getName().equals(authentication.getName())) {
            throw new ControllerSecurityException();
        }
        List<Entry> entries = entryRepository.find(group);
        return ResponseEntity.ok().body(entries);
    }

    @GetMapping(value = "/entries/generate-password")
    public Map generatePassword(@RequestParam("length") Integer length, @RequestParam("type") String type) {
        PasswordType passwordType = null;
        if (type.equals("simple")) {
            passwordType = PasswordType.SIMPLE;
        } else if (type.equals("complex")) {
            passwordType = PasswordType.COMPLEX;
        }
        String password = passwordGenerator.generate(length, passwordType);
        return Collections.singletonMap("password", password);
    }

    @GetMapping("/entries/getpassword/{id}")
    public Map getPassword(@PathVariable("id") Integer id, Authentication authentication) {
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
}
