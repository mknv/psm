package mknv.psm.server.web.controller;

import mknv.psm.server.model.domain.Role;
import mknv.psm.server.model.domain.User;
import mknv.psm.server.web.exception.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpSession;
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
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author mknv
 */
@Controller
public class UserController {

    /**
     * Validates all fields except password.
     */
    private static class CustomValidator implements Validator {

        @Override
        public boolean supports(Class<?> clazz) {
            return User.class.isAssignableFrom(clazz);
        }

        @Override
        public void validate(Object target, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotBlank");

            User user = (User) target;
            if (!errors.hasErrors() && user.getName().trim().length() > 50) {
                errors.rejectValue("name", "Size");
            }
            if (!errors.hasErrors() && user.getRoles().isEmpty()) {
                errors.rejectValue("roles", "Size");
            }
        }
    }

    @Autowired
    private Validator standardValidator;
    private final Validator customValidator = new CustomValidator();
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
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
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

    @GetMapping("/users/edit/{id}")
    public String prepareEdit(@PathVariable(name = "id") Integer id, Model model) {
        User user = userRepository.findByIdFetchRoles(id);
        if (user == null) {
            throw new EntityNotFoundException(User.class, id);
        }
        user.setPassword(null);
        model.addAttribute("user", user);
        return "users/edit";
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

    @PostMapping("/users/update")
    public String update(@ModelAttribute User user, BindingResult bindingResult,
            @RequestParam(name = "change-password", required = false) String changePassword,
            Model model, Authentication authentication) {
        Optional<User> existingUser = userRepository.findById(user.getId());
        if (!existingUser.isPresent()) {
            throw new EntityNotFoundException(User.class, user.getId());
        }
        Validator validator = changePassword != null ? standardValidator : customValidator;
        validator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            return "users/edit";
        }
        //The change-password checkbox is checked.
        //A new password will be encoded and saved.
        if (changePassword != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            //The change-password checkbox is not checked.
            //An old password will be set to the user.
            user.setPassword(existingUser.get().getPassword());
        }
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", messageSource.getMessage("user.exists", null, null));
            return "users/edit";
        }
        return "redirect:/users";
    }

    @PostMapping("/users/delete/{id}")
    public String delete(@PathVariable("id") Integer id, Model model,
            Authentication authentication, HttpSession session) {
        Optional<User> user = userRepository.findById(id);
        if (!user.isPresent()) {
            throw new EntityNotFoundException(User.class, id);
        }
        try {
            userRepository.deleteById(id);
            //If a user is the current logged user, clears the session and redirects to the default url.
            if (user.get().getName().equals(authentication.getName())) {
                session.invalidate();
                return "redirect:/";
            }
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("users", userRepository.findAllFetchRoles());
            model.addAttribute("error", messageSource.getMessage("user.groups.constraint", null, null));
            return "users/list";
        }
        return "redirect:/users";
    }
}
