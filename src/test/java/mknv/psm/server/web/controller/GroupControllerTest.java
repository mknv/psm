package mknv.psm.server.web.controller;

import mknv.psm.server.model.domain.Group;
import mknv.psm.server.model.domain.User;
import mknv.psm.server.model.repository.GroupRepository;
import mknv.psm.server.model.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 *
 * @author mknv
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private GroupRepository groupRepository;
    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(authorities = "user")
    public void list_OK() throws Exception {
        mockMvc.perform(get("/groups").secure(true))
                .andExpect(view().name("groups/list"));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void prepareCreate_OK() throws Exception {
        mockMvc.perform(get("/groups/create").secure(true))
                .andExpect(view().name("groups/edit"))
                .andExpect(model().attribute("group", new Group()));
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void prepareEdit_OK() throws Exception {
        User user = new User(1, "user", "password");
        Group group = new Group(1, "group1", user);

        given(groupRepository.findByIdFetchUser(1)).willReturn(group);

        mockMvc.perform(get("/groups/edit/1").secure(true))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/edit"))
                .andExpect(model().attribute("group", group));
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void prepareEdit_When_GroupNotFound() throws Exception {
        mockMvc.perform(get("/groups/edit/10").secure(true))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void prepareEdit_When_GroupBelongsToAnotherUser() throws Exception {
        User anotherUser = new User(1, "another", "password");
        Group group = new Group(1, "group", anotherUser);

        given(groupRepository.findByIdFetchUser(1)).willReturn(group);

        mockMvc.perform(get("/groups/edit/1").secure(true))
                .andExpect(status().isForbidden())
                .andExpect(view().name("403"));
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void createGroup_OK() throws Exception {
        User user = new User(1, "user", "password");
        Group group = new Group();
        group.setName("group");

        given(userRepository.findByName("user")).willReturn(user);

        mockMvc.perform(post("/groups/save").secure(true).with(csrf())
                .flashAttr("group", group))
                .andExpect(redirectedUrl("/groups"));

        //The current user should be assigned to the group
        assertEquals(user, group.getUser());
        then(groupRepository).should(times(1)).save(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void updateGroup_OK() throws Exception {
        User user = new User(1, "user", "password");
        Group existing = new Group(1, "existing", user);

        Group newGroup = new Group(1, "new", null);

        given(userRepository.findByName("user")).willReturn(user);
        given(groupRepository.findByIdFetchUser(1)).willReturn(existing);

        mockMvc.perform(post("/groups/save").secure(true).with(csrf())
                .flashAttr("group", newGroup))
                .andExpect(redirectedUrl("/groups"));

        //The current user should be assigned to the group
        assertEquals(user, existing.getUser());
        then(groupRepository).should(times(1)).save(newGroup);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void createGroup_When_ValidationFailed() throws Exception {
        //The name is empty
        User currentUser = new User(1, "user", "password");
        Group newGroup = new Group();

        given(userRepository.findByName("user")).willReturn(currentUser);

        mockMvc.perform(post("/groups/save").secure(true).with(csrf())
                .flashAttr("group", newGroup))
                .andExpect(view().name("groups/edit"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrors("group", "name"));

        then(groupRepository).should(times(0)).save(newGroup);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void update_When_GroupNotFound() throws Exception {
        User user = new User(1, "user", "password");

        Group group = new Group(10, "group", null);

        given(userRepository.findByName("user")).willReturn(user);
        given(groupRepository.findByIdFetchUser(10)).willReturn(null);

        mockMvc.perform(post("/groups/save").secure(true).with(csrf())
                .flashAttr("group", group))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));

        then(groupRepository).should(times(0)).save(group);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void update_When_GroupBelongsToAnotherUser() throws Exception {
        User anotherUser = new User(1, "another", "password");
        Group existingGroup = new Group(1, "existing", anotherUser);
        Group newGroup = new Group(1, "new", null);

        given(userRepository.findByName("another")).willReturn(anotherUser);
        given(groupRepository.findByIdFetchUser(1)).willReturn(existingGroup);

        mockMvc.perform(post("/groups/save").secure(true).with(csrf())
                .flashAttr("group", newGroup))
                .andExpect(status().isForbidden())
                .andExpect(view().name("403"));

        then(groupRepository).should(times(0)).save(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void create_When_GroupAlreadyExists() throws Exception {
        User user = new User(1, "user", "password");
        Group existingGroup = new Group(1, "existing", user);
        Group newGroup = new Group(null, "existing", user);

        given(userRepository.findByName("user")).willReturn(user);
        given(groupRepository.findByIdFetchUser(1)).willReturn(existingGroup);
        willThrow(DataIntegrityViolationException.class).given(groupRepository).save(newGroup);

        mockMvc.perform(post("/groups/save").secure(true).with(csrf())
                .flashAttr("group", newGroup))
                .andExpect(view().name("groups/edit"))
                .andExpect(model().attributeExists("error"));

        then(groupRepository).should(times(1)).save(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void deleteGroup_OK() throws Exception {
        User currentUser = new User(1, "user", "password");
        Group existingGroup = new Group(1, "existing", currentUser);

        given(groupRepository.findByIdFetchUser(1)).willReturn(existingGroup);

        mockMvc.perform(post("/groups/delete/{id}", 1).secure(true).with(csrf()))
                .andExpect(redirectedUrl("/groups"));

        then(groupRepository).should(times(1)).deleteById(1);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void delete_When_GroupNotFound() throws Exception {
        mockMvc.perform(post("/groups/delete/{id}", 10).secure(true).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));

        then(groupRepository).should(times(0)).deleteById(1);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void delete_When_GroupBelongsToAnotherUser() throws Exception {
        User anotherUser = new User(1, "another", "password");
        Group existingGroup = new Group(1, "existing", anotherUser);

        given(groupRepository.findByIdFetchUser(1)).willReturn(existingGroup);

        mockMvc.perform(post("/groups/delete/{id}", 1).secure(true).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(view().name("403"));

        then(groupRepository).should(times(0)).deleteById(1);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void delete_When_DataIntegrityViolationException_IsThrown() throws Exception {
        User currentUser = new User(1, "user", "password");
        Group existingGroup = new Group(1, "existing", currentUser);

        given(groupRepository.findByIdFetchUser(1)).willReturn(existingGroup);
        willThrow(DataIntegrityViolationException.class).given(groupRepository).deleteById(1);

        mockMvc.perform(post("/groups/delete/{id}", 1).secure(true).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/list"))
                .andExpect(model().attributeExists("error"));

        then(groupRepository).should(times(1)).deleteById(1);
    }
}
