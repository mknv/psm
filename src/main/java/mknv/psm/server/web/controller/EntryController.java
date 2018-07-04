package mknv.psm.server.web.controller;

import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
    @Autowired
    private MessageSource messageSource;

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
        return "/entries/create";
    }

    @PostMapping("/entries/create")
    public String create(@Valid @ModelAttribute Entry entry, BindingResult bindingResult,
            @RequestParam(name = "password-validity", required = false) Integer passwordValidity,
            Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "entries/create";
        }
        //Throws a ControllerSecurityException if entry's group does not belong to a current user
        Group existingGroup = groupRepository.findByIdFetchUser(entry.getGroup().getId());
        if (!existingGroup.getUser().getName().equals(authentication.getName())) {
            throw new ControllerSecurityException();
        }
        entry.setUser(existingGroup.getUser());
        //Validates a password
        if (entry.getPassword() != null) {
            if (entry.getPassword().length() > 100) {
                bindingResult.addError(new FieldError("entry", "password",
                        messageSource.getMessage("Size.entry.password", new Integer[]{100}, null)));
                return "entries/create";
            }
            String encryptedPassword = passwordEncryptor.encrypt(entry.getPassword());
            entry.setPassword(encryptedPassword);
        }
        if (passwordValidity != null) {
            LocalDate expiredDate = LocalDate.now().plusMonths(passwordValidity);
            entry.setExpiredDate(expiredDate);
        }
        entryRepository.save(entry);
        return "redirect:/";
    }

    @GetMapping("/entries/edit/{id}")
    public String prepareEdit(@PathVariable("id") Integer id, Model model, Authentication authentication) {
        Entry entry = entryRepository.findByIdFetchAll(id);
        if (entry == null) {
            throw new EntityNotFoundException(Entry.class, id);
        }
        if (!entry.getUser().getName().equals(authentication.getName())) {
            throw new ControllerSecurityException();
        }
        if (entry.getPassword() != null) {
            entry.setPassword(passwordEncryptor.decrypt(entry.getPassword()));
        }
        model.addAttribute("entry", entry);
        return "/entries/edit";
    }

    @PostMapping("/entries/update")
    public String update(@Valid @ModelAttribute Entry entry,
            BindingResult bindingResult,
            @RequestParam(name = "remove-password-validity", required = false) String removePasswordValidity,
            @RequestParam(name = "password-validity", required = false) Integer passwordValidity,
            Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "entries/edit";
        }
        //Throws a ControllerSecurityException if entry's group does not belong to a current user
        Group existingGroup = groupRepository.findByIdFetchUser(entry.getGroup().getId());
        if (!existingGroup.getUser().getName().equals(authentication.getName())) {
            throw new ControllerSecurityException();
        }
        entry.setUser(existingGroup.getUser());
        //Validates a password
        if (entry.getPassword() != null) {
            if (entry.getPassword().length() > 100) {
                bindingResult.addError(new FieldError("entry", "password",
                        messageSource.getMessage("Size.entry.password", new Integer[]{100}, null)));
                return "entries/edit";
            }
            String encryptedPassword = passwordEncryptor.encrypt(entry.getPassword());
            entry.setPassword(encryptedPassword);
        }
        //Remove password validity is checked
        if (removePasswordValidity != null) {
            entry.setExpiredDate(null);
        } else {
            //Prolong password validity
            if (passwordValidity != null) {
                LocalDate expiredDate = LocalDate.now().plusMonths(passwordValidity);
                entry.setExpiredDate(expiredDate);
            }
        }
        entryRepository.save(entry);
        return "redirect:/entries";
    }

}
