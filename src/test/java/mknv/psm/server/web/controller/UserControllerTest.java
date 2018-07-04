package mknv.psm.server.web.controller;

import java.util.Arrays;
import java.util.Optional;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import org.junit.runner.RunWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import mknv.psm.server.model.domain.Role;
import mknv.psm.server.model.domain.User;
import mknv.psm.server.model.repository.UserRepository;

/**
 *
 * @author mknv
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;

    public UserControllerTest() {
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void list_OK() throws Exception {
        System.out.println("list_OK");

        Role role = new Role(1, "admin");
        User user = new User(1, "admin", "password");
        user.getRoles().add(role);

        given(userRepository.findAllFetchRoles()).willReturn(Arrays.asList(user));

        mockMvc.perform(get("/users").secure(true))
                .andExpect(status().isOk())
                .andExpect(view().name("users/list"))
                .andExpect(model().attribute("users", hasSize(1)))
                .andExpect(model().attribute("users", hasItem(user)));
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void prepareCreate_OK() throws Exception {
        System.out.println("prepareCreate_OK");

        mockMvc.perform(get("/users/create").secure(true))
                .andExpect(status().isOk())
                .andExpect(view().name("users/create"))
                .andExpect(model().attribute("user", is(new User())));
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void testCreate_OK() throws Exception {
        System.out.println("testCreate_OK");

        Role role = new Role(1, "admin");
        User validUser = new User("admin", "password");
        validUser.getRoles().add(role);

        User result = new User(1, "admin", "password");
        result.getRoles().add(role);

        //OK
        given(userRepository.save(validUser)).willReturn(result);
        mockMvc.perform(post("/users/create").secure(true).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "test")
                .param("password", "password")
                .param("roles", "1").param("roles", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users"));

        //Should forward to /users/create when validation failed.
        //Name, password and roles fields are not present.
        mockMvc.perform(post("/users/create").secure(true).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("users/create"))
                .andExpect(model().errorCount(3))
                .andExpect(model().attributeHasFieldErrorCode("user", "name", "NotBlank"))
                .andExpect(model().attributeHasFieldErrorCode("user", "password", "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("user", "roles", "Size"));

        //Should forward to /users/create when validation failed.
        //The name and the password fields have wrong length and the roles field is not present.
        mockMvc.perform(post("/users/create").secure(true).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "    ")
                .param("password", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("users/create"))
                .andExpect(model().errorCount(3))
                .andExpect(model().attributeHasFieldErrorCode("user", "name", "NotBlank"))
                .andExpect(model().attributeHasFieldErrorCode("user", "password", "Size"))
                .andExpect(model().attributeHasFieldErrorCode("user", "roles", "Size"));
        //userRepository.save should not be called if validation fails
        verify(userRepository, times(1)).save(validUser);

        //Should forward to /users/create with the error message.
        //The user with the same name is already exists.
        given(userRepository.save(validUser)).willThrow(new DataIntegrityViolationException(""));
        mockMvc.perform(post("/users/create").secure(true).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "test")
                .param("password", "password")
                .param("roles", "1").param("roles", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/create"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void prepareEdit_OK() throws Exception {
        System.out.println("prepareEdit_OK");
        Role role = new Role(1, "admin");
        User user = new User("admin", "password");
        user.getRoles().add(role);

        given(userRepository.findByIdFetchRoles(1)).willReturn(user);

        //OK
        mockMvc.perform(get("/users/edit/1").secure(true))
                .andExpect(status().isOk())
                .andExpect(view().name("users/edit"))
                .andExpect(model().attribute("user", user));

        //Should return not found error page when the user is not found
        mockMvc.perform(get("/users/edit/2").secure(true))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void update_OK() throws Exception {
        System.out.println("update_OK");

        Role role = new Role(1, "admin");
        User validUser = new User("admin", "password");
        validUser.getRoles().add(role);

        User result = new User(1, "admin", "password");
        result.getRoles().add(role);

        //OK
        given(userRepository.save(validUser)).willReturn(result);
        mockMvc.perform(post("/users/update").secure(true).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "test")
                .param("password", "password")
                .param("roles", "1").param("roles", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users"));

        //Should forward to /users/edit when validation failed.
        //Name, password and roles fields are not present.
        mockMvc.perform(post("/users/update").secure(true).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("users/edit"))
                .andExpect(model().errorCount(3))
                .andExpect(model().attributeHasFieldErrorCode("user", "name", "NotBlank"))
                .andExpect(model().attributeHasFieldErrorCode("user", "password", "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("user", "roles", "Size"));

        //Should forward to /users/edit when validation failed.
        //The name and the password fields have wrong length and the roles field is not present.
        mockMvc.perform(post("/users/update").secure(true).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "    ")
                .param("password", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("users/edit"))
                .andExpect(model().errorCount(3))
                .andExpect(model().attributeHasFieldErrorCode("user", "name", "NotBlank"))
                .andExpect(model().attributeHasFieldErrorCode("user", "password", "Size"))
                .andExpect(model().attributeHasFieldErrorCode("user", "roles", "Size"));
        //userRepository.save should not be called if validation fails
        verify(userRepository, times(1)).save(validUser);

        //Should forward to /users/edit with the error message.
        //The user with the same name is already exists.
        given(userRepository.save(validUser)).willThrow(new DataIntegrityViolationException(""));
        mockMvc.perform(post("/users/update").secure(true).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "test")
                .param("password", "password")
                .param("roles", "1").param("roles", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/edit"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void delete_OK() throws Exception {
        System.out.println("delete_OK");

        Role role = new Role(1, "admin");
        User user = new User(1, "admin", "password");
        user.getRoles().add(role);
        Optional<User> optionalUser = Optional.of(user);

        given(userRepository.findById(1)).willReturn(optionalUser);
        doNothing().when(userRepository).deleteById(1);

        //OK
        mockMvc.perform(post("/users/delete/1").secure(true).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users"));

        //Should return not found error page when the user is not found.
        //userRepository.deleteById should not be called in this case.
        mockMvc.perform(post("/users/delete/2").secure(true).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));

        verify(userRepository, times(1)).deleteById(1);
    }
}
