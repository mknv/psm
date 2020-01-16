package mknv.psm.server.web.controller;

import java.time.LocalDate;
import mknv.psm.server.model.domain.Entry;
import mknv.psm.server.model.domain.Group;
import mknv.psm.server.model.domain.User;
import mknv.psm.server.model.repository.EntryRepository;
import mknv.psm.server.model.repository.GroupRepository;
import mknv.psm.server.model.repository.UserRepository;
import mknv.psm.server.util.PasswordEncryptor;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.BDDMockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

/**
 *
 * @author mknv
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EntryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EntryRepository entryRepository;
    @MockBean
    private GroupRepository groupRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private PasswordEncryptor passwordEncryptor;

    @Test
    @WithMockUser(authorities = "user")
    public void list_OK() throws Exception {
        mockMvc.perform(get("/").secure(true))
                .andExpect(view().name("entries/list"));

        mockMvc.perform(get("/entries").secure(true))
                .andExpect(view().name("entries/list"));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void prepareCreate_OK() throws Exception {
        mockMvc.perform(get("/entries/create").secure(true))
                .andExpect(view().name("entries/edit"))
                .andExpect(model().attribute("entry", new Entry()));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void prepareEdit_OK() throws Exception {
        User user = new User(1, "user", "password");

        Entry entry = new Entry();
        entry.setId(1);
        entry.setUser(user);

        given(entryRepository.findByIdFetchAll(1)).willReturn(entry);
        given(passwordEncryptor.decrypt(entry.getPassword())).willReturn("password");

        mockMvc.perform(get("/entries/edit/1").secure(true))
                .andExpect(view().name("entries/edit"))
                .andExpect(model().attribute("entry", entry));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void prepareEdit_When_EntryIsNotFound() throws Exception {
        Entry entry = new Entry();
        entry.setId(1);

        mockMvc.perform(get("/entries/edit/{id}", 10).secure(true))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void prepareEdit_When_EntryBelongsToAnotherUser() throws Exception {
        User anotherUser = new User(1, "another", "password");

        Entry entry = new Entry();
        entry.setId(1);
        entry.setUser(anotherUser);

        given(entryRepository.findByIdFetchAll(1)).willReturn(entry);

        mockMvc.perform(get("/entries/edit/1").secure(true))
                .andExpect(status().isForbidden())
                .andExpect(view().name("403"));
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void createEntry_OK() throws Exception {
        User user = new User(1, "user", "password");
        Entry newEntry = new Entry();
        newEntry.setName("new");

        given(userRepository.findByName("user")).willReturn(user);

        mockMvc.perform(post("/entries/save").secure(true).with(csrf())
                .flashAttr("entry", newEntry))
                .andExpect(redirectedUrl("/entries"));

        //A logged user should be assigned to the newEntry
        assertEquals(user, newEntry.getUser());
        then(entryRepository).should(times(1)).save(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void updateEntry_OK() throws Exception {
        User user = new User(1, "user", "password");

        Entry existingEntry = new Entry();
        existingEntry.setId(1);
        existingEntry.setName("existing");
        existingEntry.setUser(user);

        Entry newEntry = new Entry();
        newEntry.setId(1);
        newEntry.setName("new");

        given(userRepository.findByName("user")).willReturn(user);
        given(entryRepository.findByIdFetchAll(1)).willReturn(existingEntry);

        mockMvc.perform(post("/entries/save").secure(true).with(csrf())
                .flashAttr("entry", newEntry))
                .andExpect(redirectedUrl("/entries"));

        //A logged user should be assigned to the newEntry
        assertEquals(user, newEntry.getUser());
        then(entryRepository).should(times(1)).save(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void createEntry_When_ValidationFailed() throws Exception {
        mockMvc.perform(post("/entries/save").secure(true).with(csrf()))
                .andExpect(view().name("entries/edit"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrors("entry", "name"));

        then(entryRepository).should(times(0)).save(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void update_When_EntryNotFound() throws Exception {
        User user = new User(1, "user", "password");
        Entry entry = new Entry();
        entry.setId(1);
        entry.setName("entry");

        given(userRepository.findByName("user")).willReturn(user);
        given(entryRepository.findByIdFetchAll(1)).willReturn(null);

        mockMvc.perform(post("/entries/save").secure(true).with(csrf())
                .flashAttr("entry", entry))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));

        then(entryRepository).should(times(0)).save(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void update_When_EntryBelongsToAnotherUser() throws Exception {
        User anotherUser = new User(1, "another", "password");

        Entry existingEntry = new Entry();
        existingEntry.setId(1);
        existingEntry.setName("existing");
        existingEntry.setUser(anotherUser);

        Entry newEntry = new Entry();
        newEntry.setId(1);
        newEntry.setName("new");

        given(userRepository.findByName("another")).willReturn(anotherUser);
        given(entryRepository.findByIdFetchAll(1)).willReturn(existingEntry);

        mockMvc.perform(post("/entries/save").secure(true).with(csrf())
                .flashAttr("entry", newEntry))
                .andExpect(status().isForbidden())
                .andExpect(view().name("403"));

        then(entryRepository).should(times(0)).save(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void createEntry_With_Group_OK() throws Exception {
        User currentUser = new User(1, "user", "password");

        Group group = new Group(1, "group", currentUser);

        Entry newEntry = new Entry();
        newEntry.setName("new");
        newEntry.setGroup(group);

        given(userRepository.findByName("user")).willReturn(currentUser);
        given(groupRepository.findByIdFetchUser(1)).willReturn(group);

        mockMvc.perform(post("/entries/save").secure(true).with(csrf())
                .flashAttr("entry", newEntry))
                .andExpect(redirectedUrl("/entries"));

        then(entryRepository).should(times(1)).save(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void createEntry_When_GroupNotFound() throws Exception {
        User currentUser = new User(1, "user", "password");

        Group group = new Group(1, "group", currentUser);

        Entry newEntry = new Entry();
        newEntry.setName("new");
        newEntry.setGroup(group);

        given(userRepository.findByName("user")).willReturn(currentUser);
        given(groupRepository.findByIdFetchUser(1)).willReturn(null);

        mockMvc.perform(post("/entries/save").secure(true).with(csrf())
                .flashAttr("entry", newEntry))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));

        then(entryRepository).should(times(0)).save(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void createEntry_When_GroupBelongsToAnotherUser() throws Exception {
        User anotherUser = new User(1, "another", "password");

        Group group = new Group(1, "group", anotherUser);

        Entry newEntry = new Entry();
        newEntry.setName("new");
        newEntry.setGroup(group);

        given(userRepository.findByName("another")).willReturn(anotherUser);
        given(groupRepository.findByIdFetchUser(1)).willReturn(group);

        mockMvc.perform(post("/entries/save").secure(true).with(csrf())
                .flashAttr("entry", newEntry))
                .andExpect(status().isForbidden())
                .andExpect(view().name("403"));

        then(entryRepository).should(times(0)).save(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void createEntry_With_Password_OK() throws Exception {
        User currentUser = new User(1, "user", "password");
        Entry newEntry = new Entry();
        newEntry.setName("new");
        newEntry.setPassword("password");

        given(userRepository.findByName("user")).willReturn(currentUser);
        given(passwordEncryptor.encrypt("password")).willReturn("encrypted");

        mockMvc.perform(post("/entries/save").secure(true).with(csrf())
                .flashAttr("entry", newEntry))
                .andExpect(redirectedUrl("/entries"));

        //The password should be encrypted
        assertEquals("encrypted", newEntry.getPassword());
        then(entryRepository).should(times(1)).save(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void updateEntry_RemovePasswordValidity_OK() throws Exception {
        User currentUser = new User(1, "user", "password");

        Entry existingEntry = new Entry();
        existingEntry.setId(1);
        existingEntry.setName("existing");
        existingEntry.setUser(currentUser);
        existingEntry.setExpiredDate(LocalDate.of(2000, 1, 1));

        given(userRepository.findByName("user")).willReturn(currentUser);
        given(entryRepository.findByIdFetchAll(1)).willReturn(existingEntry);

        mockMvc.perform(post("/entries/save").secure(true).with(csrf())
                .flashAttr("entry", existingEntry)
                .param("remove-password-validity", "on"))
                .andExpect(redirectedUrl("/entries"));

        assertEquals(null, existingEntry.getExpiredDate());
        then(entryRepository).should(times(1)).save(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void updateEntry_ProlongPasswordValidity_OK() throws Exception {
        //If the remove-password-validity is null and the password-validity > 0
        //then assigns a new value to the expiredDate property.
        //This expiredDate value is equal to LocalDate.now() + number of months from the password-validity parameter
        User currentUser = new User(1, "user", "password");

        Entry existingEntry = new Entry();
        existingEntry.setId(1);
        existingEntry.setName("existing");
        existingEntry.setUser(currentUser);

        given(userRepository.findByName("user")).willReturn(currentUser);
        given(entryRepository.findByIdFetchAll(1)).willReturn(existingEntry);

        mockMvc.perform(post("/entries/save").secure(true).with(csrf())
                .flashAttr("entry", existingEntry)
                .param("password-validity", "0"))
                .andExpect(redirectedUrl("/entries"));

        assertEquals(null, existingEntry.getExpiredDate());

        mockMvc.perform(post("/entries/save").secure(true).with(csrf())
                .flashAttr("entry", existingEntry)
                .param("password-validity", "3"))
                .andExpect(redirectedUrl("/entries"));

        assertEquals(LocalDate.now().plusMonths(3), existingEntry.getExpiredDate());
        then(entryRepository).should(times(2)).save(any());
    }
}
