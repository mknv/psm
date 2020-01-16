package mknv.psm.server.web.controller;

import java.util.Arrays;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import mknv.psm.server.model.domain.Role;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import mknv.psm.server.model.domain.User;
import mknv.psm.server.model.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * If the change-password parameter is present then the standard validator is
 * used. A new password will be encoded and saved. If this parameter is null,
 * the custom validator is used, that does not validate a password. In this case
 * the new password will not be saved.
 *
 * @author mknv
 */
@ExtendWith(SpringExtension.class)
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
        User user = new User(1, "user", "password");

        given(userRepository.findAllFetchRoles()).willReturn(Arrays.asList(user));

        mockMvc.perform(get("/users").secure(true))
                .andExpect(view().name("users/list"))
                .andExpect(model().attribute("users", hasSize(1)))
                .andExpect(model().attribute("users", hasItem(user)));
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void prepareCreate_OK() throws Exception {
        mockMvc.perform(get("/users/create").secure(true))
                .andExpect(view().name("users/create"))
                .andExpect(model().attribute("user", is(new User())));
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void prepareEdit_OK() throws Exception {
        User user = new User(1, "user", "password");

        given(userRepository.findByIdFetchRoles(1)).willReturn(user);

        mockMvc.perform(get("/users/edit/1").secure(true))
                .andExpect(view().name("users/edit"))
                .andExpect(model().attribute("user", is(user)))
                .andExpect(model().attribute("user", hasProperty("password", nullValue())));
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void prepareEdit_When_UserNotFound() throws Exception {
        given(userRepository.findByIdFetchRoles(10)).willReturn(null);
        mockMvc.perform(get("/users/edit/10").secure(true))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void createUser_OK() throws Exception {
        Role role = new Role(1, "user");
        User newUser = new User(1, "new", "password");
        newUser.getRoles().add(role);

        mockMvc.perform(post("/users/create").secure(true).with(csrf())
                .flashAttr("user", newUser))
                .andExpect(redirectedUrl("/users"));

        then(userRepository).should(times(1)).save(newUser);
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void createUser_When_ValidationFailed() throws Exception {
        mockMvc.perform(post("/users/create").secure(true).with(csrf()))
                .andExpect(view().name("users/create"))
                .andExpect(model().errorCount(3))
                .andExpect(model().attributeHasFieldErrorCode("user", "name", "NotBlank"))
                .andExpect(model().attributeHasFieldErrorCode("user", "password", "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("user", "roles", "Size"));
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void create_When_UserAlreadyExists() throws Exception {
        Role role = new Role(1, "user");
        User newUser = new User(1, "new", "password");
        newUser.getRoles().add(role);

        given(userRepository.save(newUser)).willThrow(DataIntegrityViolationException.class);

        mockMvc.perform(post("/users/create").secure(true).with(csrf())
                .flashAttr("user", newUser))
                .andExpect(view().name("users/create"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void updateUser_WithUpdatingPassword_OK() throws Exception {
        Role role = new Role(1, "admin");
        User existingUser = new User(1, "existing user", "old password");
        User newUser = new User(1, "new", "new password");
        newUser.getRoles().add(role);

        given(userRepository.findById(1)).willReturn(Optional.of(existingUser));

        mockMvc.perform(post("/users/update").secure(true).with(csrf())
                .flashAttr("user", newUser)
                .param("change-password", "on"))
                .andExpect(redirectedUrl("/users"));

        //The new password should be saved
        assertEquals("new password", newUser.getPassword());
        then(userRepository).should(times(1)).save(newUser);
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void updateUser_WithoutUpdatingPassword_OK() throws Exception {
        Role role = new Role(1, "admin");
        User existingUser = new User(1, "existing user", "old password");
        User newUser = new User(1, "new", "new password");
        newUser.getRoles().add(role);

        given(userRepository.findById(1)).willReturn(Optional.of(existingUser));

        mockMvc.perform(post("/users/update").secure(true).with(csrf())
                .flashAttr("user", newUser))
                .andExpect(redirectedUrl("/users"));

        //The new password should not be saved
        assertEquals("old password", newUser.getPassword());
        then(userRepository).should(times(1)).save(newUser);
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void updateUser_When_ValidationFailed() throws Exception {
        User existingUser = new User(1, "existing user", "old password");
        User newUser = new User(1, null, null);

        given(userRepository.findById(1)).willReturn(Optional.of(existingUser));

        mockMvc.perform(post("/users/update").secure(true).with(csrf())
                .flashAttr("user", newUser)
                .param("change-password", "on"))
                .andExpect(view().name("users/edit"))
                .andExpect(model().errorCount(3))
                .andExpect(model().attributeHasFieldErrorCode("user", "name", "NotBlank"))
                .andExpect(model().attributeHasFieldErrorCode("user", "password", "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("user", "roles", "Size"));

        then(userRepository).should(times(0)).save(newUser);
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void update_When_UserNotFound() throws Exception {
        User newUser = new User(1, "new", "password");

        given(userRepository.findById(1)).willReturn(Optional.empty());

        mockMvc.perform(post("/users/update").secure(true).with(csrf())
                .flashAttr("user", newUser)
                .param("change-password", "on"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));

        then(userRepository).should(times(0)).save(newUser);
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void update_When_UserAlreadyExists() throws Exception {
        Role role = new Role(1, "admin");
        User existingUser = new User(1, "existing user", "old password");
        User newUser = new User(1, "new", "password");
        newUser.getRoles().add(role);

        given(userRepository.findById(1)).willReturn(Optional.of(existingUser));
        given(userRepository.save(newUser)).willThrow(new DataIntegrityViolationException(""));

        mockMvc.perform(post("/users/update").secure(true).with(csrf())
                .flashAttr("user", newUser))
                .andExpect(view().name("users/edit"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void deleteUser_OK() throws Exception {
        User existingUser = new User(1, "existing", "password");

        given(userRepository.findById(1)).willReturn(Optional.of(existingUser));

        mockMvc.perform(post("/users/delete/1").secure(true).with(csrf()))
                .andExpect(redirectedUrl("/users"));
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void delete_When_UserNotFound() throws Exception {
        given(userRepository.findById(1)).willReturn(Optional.empty());

        mockMvc.perform(post("/users/delete/1").secure(true).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "admin")
    public void delete_When_UserIsSameAsLoggedUser_OK() throws Exception {
        User admin = new User(1, "admin", "password");

        given(userRepository.findById(1)).willReturn(Optional.of(admin));

        HttpSession session = mockMvc.perform(post("/users/delete/1").secure(true).with(csrf()))
                .andExpect(redirectedUrl("/")).andReturn().getRequest().getSession();

        //Should invalidate the session
        assertFalse(session.getAttributeNames().hasMoreElements());
    }

    @Test
    @WithMockUser(authorities = "admin")
    public void delete_When_DataIntegrityViolationException_IsThrown() throws Exception {
        User user = new User(1, "user", "password");

        given(userRepository.findById(1)).willReturn(Optional.of(user));
        willThrow(DataIntegrityViolationException.class).given(userRepository).deleteById(1);

        mockMvc.perform(post("/users/delete/1").secure(true).with(csrf())
                .with(user("user").authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(view().name("users/list"))
                .andExpect(model().attributeExists("users", "error"));
    }
}
