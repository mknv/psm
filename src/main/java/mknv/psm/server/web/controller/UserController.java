package mknv.psm.server.web.controller;

import mknv.psm.server.model.domain.Role;
import mknv.psm.server.model.domain.User;
import mknv.psm.server.web.exception.EntityNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import mknv.psm.server.model.repository.RoleRepository;
import mknv.psm.server.model.repository.UserRepository;

/**
 *
 * @author mknv
 */
@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @InitBinder
    public void init(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    @ModelAttribute("roles")
    public List<Role> roles() {
        return roleRepository.findAll();
    }

    @GetMapping("/users")
    public String list(Model model) {
        model.addAttribute("users", userRepository.findAllFetchRoles());
        return "users/list";
    }

    @GetMapping("/users/create")
    public String prepareCreate(Model model) {
        model.addAttribute("user", new User());
        return "users/create";
    }

    @PostMapping("/users/create")
    public String create(@Valid @ModelAttribute User user, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "users/create";
        }
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", messageSource.getMessage("user.exists", null, null));
            return "users/create";
        }
        return "redirect:/users";
    }

    @GetMapping("/users/edit/{id}")
    public String prepareEdit(@PathVariable("id") Integer id, Model model) throws IOException {
        User user = userRepository.findByIdFetchRoles(id);
        if (user == null) {
            throw new EntityNotFoundException(User.class, id);
        }
        user.setPassword(null);
        model.addAttribute("user", user);
        return "users/edit";
    }

    @PostMapping("/users/update")
    public String update(@Valid @ModelAttribute User user, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "users/edit";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("error", messageSource.getMessage("user.exists", null, null));
            return "users/edit";
        }
        return "redirect:/users";
    }

    @PostMapping("/users/delete/{id}")
    public String delete(@PathVariable("id") Integer id, Model model) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (!user.isPresent()) {
                throw new EntityNotFoundException(User.class, id);
            }
            userRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("users", userRepository.findAllFetchRoles());
            model.addAttribute("error", messageSource.getMessage("user.groups.constraint", null, null));
            return "users/list";
        }
        return "redirect:/users";
    }
}
