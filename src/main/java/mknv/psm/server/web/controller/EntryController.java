package mknv.psm.server.web.controller;

import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import mknv.psm.server.model.domain.Entry;
import mknv.psm.server.model.domain.Group;
import mknv.psm.server.model.domain.User;
import mknv.psm.server.model.repository.EntryRepository;
import mknv.psm.server.model.repository.GroupRepository;
import mknv.psm.server.model.repository.UserRepository;
import mknv.psm.server.util.PasswordEncryptor;
import mknv.psm.server.web.exception.ControllerSecurityException;
import mknv.psm.server.web.exception.EntityNotFoundException;

/**
 *
 * @author mknv
 */
@Controller
public class EntryController {

    @Autowired
    private EntryRepository entryRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncryptor passwordEncryptor;

    @InitBinder
    public void init(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ModelAttribute("groups")
    public List<Group> groups(Authentication authentication) {
        User currentUser = userRepository.findByName(authentication.getName());
        return groupRepository.findByUser(currentUser);
    }

    @GetMapping(value = {"/", "/entries"})
    public String list() {
        return "entries/list";
    }

    @GetMapping("/entries/create")
    public String prepareCreate(Model model) {
        model.addAttribute("entry", new Entry());
        return "entries/edit";
    }

    @GetMapping("/entries/edit/{id}")
    public String prepareEdit(@PathVariable(name = "id") Integer id, Model model, Authentication authentication) {
        Entry entry = entryRepository.findByIdFetchAll(id);
        if (entry == null) {
            throw new EntityNotFoundException(Entry.class, id);
        }
        if (!entry.getUser().getName().equals(authentication.getName())) {
            throw new ControllerSecurityException();
        }
        entry.setPassword(passwordEncryptor.decrypt(entry.getPassword()));
        model.addAttribute("entry", entry);
        return "entries/edit";
    }

    @PostMapping("/entries/save")
    public String save(@Valid @ModelAttribute Entry entry,
            BindingResult bindingResult,
            @RequestParam(name = "remove-password-validity", required = false) String removePasswordValidity,
            @RequestParam(name = "password-validity", required = false) Integer passwordValidity,
            Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "entries/edit";
        }

        User currentUser = userRepository.findByName(authentication.getName());
        entry.setUser(currentUser);

        //If the entry is existing, check that it belongs to the logged user.
        //Otherwise throw a ControllerSecurityException.
        if (entry.getId() != null) {
            Entry existingEntry = entryRepository.findByIdFetchAll(entry.getId());
            if (existingEntry == null) {
                throw new EntityNotFoundException(Entry.class, entry.getId());
            }
            if (!existingEntry.getUser().equals(currentUser)) {
                throw new ControllerSecurityException();
            }
        }

        //If a group is not null, check if this group belongs to the current user.
        //Otherwise throw a ControllerSecurityException.
        if (entry.getGroup() != null) {
            Group existingGroup = groupRepository.findByIdFetchUser(entry.getGroup().getId());
            if (existingGroup == null) {
                throw new EntityNotFoundException(Group.class, entry.getGroup().getId());
            }
            if (!existingGroup.getUser().equals(currentUser)) {
                throw new ControllerSecurityException();
            }
        }

        //Encrypt a password if it is not null
        if (entry.getPassword() != null) {
            String encryptedPassword = passwordEncryptor.encrypt(entry.getPassword());
            entry.setPassword(encryptedPassword);
        }

        //Remove password validity if necessary
        if (removePasswordValidity != null) {
            entry.setExpiredDate(null);
        } else {
            //Prolong password validity if necessary
            if (passwordValidity != null && passwordValidity > 0) {
                LocalDate expiredDate = LocalDate.now().plusMonths(passwordValidity);
                entry.setExpiredDate(expiredDate);
            }
        }
        entryRepository.save(entry);
        return "redirect:/entries";
    }
}
