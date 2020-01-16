package mknv.psm.server.web.controller.rest;

import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import mknv.psm.server.model.domain.Entry;
import mknv.psm.server.model.domain.Group;
import mknv.psm.server.model.domain.User;
import mknv.psm.server.model.repository.EntryRepository;
import mknv.psm.server.model.repository.GroupRepository;
import mknv.psm.server.model.repository.UserRepository;
import mknv.psm.server.model.service.EntryService;
import mknv.psm.server.util.PasswordEncryptor;
import mknv.psm.server.util.PasswordGenerator;
import mknv.psm.server.util.PasswordType;

/**
 *
 * @author mknv
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EntryRestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private GroupRepository groupRepository;
    @MockBean
    private EntryRepository entryRepository;
    @MockBean
    private EntryService entryService;
    @MockBean
    private PasswordGenerator passwordGenerator;
    @MockBean
    private PasswordEncryptor passwordEncryptor;

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void findAllEntries_OK() throws Exception {
        User user = new User(1, "user", "password");
        Entry entry = new Entry("entry", user);

        given(userRepository.findByName("user")).willReturn(user);
        given(entryService.find(user, "entry", null, false)).willReturn(List.of(entry));

        mockMvc.perform(get("/rest/entries").secure(true)
                .param("name", "entry")
                .param("group", "all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("entry")))
                .andExpect(jsonPath("$[0].user").doesNotExist());

        then(entryService).should(times(1)).find(user, "entry", null, false);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void findEntries_WithEmptyGroup_OK() throws Exception {
        User user = new User(1, "user", "password");
        Entry entry = new Entry("entry", user);

        given(userRepository.findByName("user")).willReturn(user);
        given(entryService.find(user, "entry", null, true)).willReturn(List.of(entry));

        mockMvc.perform(get("/rest/entries").secure(true)
                .param("name", "entry")
                .param("group", "empty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("entry")))
                .andExpect(jsonPath("$[0].user").doesNotExist());

        then(entryService).should(times(1)).find(user, "entry", null, true);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void findEntries_ByGroup_OK() throws Exception {
        User user = new User(1, "user", "password");
        Group group = new Group(1, "group", user);
        Entry entry = new Entry("entry", user);

        given(userRepository.findByName("user")).willReturn(user);
        given(groupRepository.findByIdFetchUser(1)).willReturn(group);
        given(entryService.find(user, "entry", group, false)).willReturn(List.of(entry));

        mockMvc.perform(get("/rest/entries").secure(true)
                .param("name", "entry")
                .param("group", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("entry")))
                .andExpect(jsonPath("$[0].user").doesNotExist());

        then(entryService).should(times(1)).find(user, "entry", group, false);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void findByGroup_When_GroupId_IsNotInteger() throws Exception {
        User user = new User(1, "user", "password");
        Group group = new Group(1, "group", user);

        given(userRepository.findByName("user")).willReturn(user);
        given(groupRepository.findByIdFetchUser(1)).willReturn(null);

        mockMvc.perform(get("/rest/entries").secure(true)
                .param("name", "entry")
                .param("group", "wrong"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        then(entryService).should(times(0)).find(user, "entry", group, false);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void findEntries_When_GroupNotFound() throws Exception {
        User user = new User(1, "user", "password");
        Group group = new Group(1, "group", user);

        given(userRepository.findByName("user")).willReturn(user);
        given(groupRepository.findByIdFetchUser(1)).willReturn(null);

        mockMvc.perform(get("/rest/entries").secure(true)
                .param("name", "entry")
                .param("group", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        then(entryService).should(times(0)).find(user, "entry", group, false);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void findEntries_When_GroupBelongsToAnotherUser() throws Exception {
        User anotherUser = new User(1, "another", "password");
        Group group = new Group(1, "group", anotherUser);

        given(userRepository.findByName("user")).willReturn(anotherUser);
        given(groupRepository.findByIdFetchUser(1)).willReturn(group);

        mockMvc.perform(get("/rest/entries").secure(true)
                .param("name", "entry")
                .param("group", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        then(entryService).should(times(0)).find(anotherUser, "entry", group, false);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void deleteEntry_OK() throws Exception {
        User user = new User(1, "user", "password");
        Entry existingEntry = new Entry("existing", user);
        existingEntry.setId(1);

        given(entryRepository.findByIdFetchAll(1)).willReturn(existingEntry);

        mockMvc.perform(post("/rest/entries/delete/{id}", 1).secure(true).with(csrf()))
                .andExpect(status().isOk());

        then(entryRepository).should(times(1)).delete(existingEntry);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void delete_When_EntryNotFound() throws Exception {
        User user = new User(1, "user", "password");
        Entry existingEntry = new Entry("existing", user);
        existingEntry.setId(1);

        given(entryRepository.findByIdFetchAll(1)).willReturn(null);

        mockMvc.perform(post("/rest/entries/delete/{id}", 1).secure(true).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        then(entryRepository).should(times(0)).delete(existingEntry);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void delete_When_EntryBelongsToAnotherUser() throws Exception {
        User anotherUser = new User(1, "another", "password");
        Entry existingEntry = new Entry("existing", anotherUser);
        existingEntry.setId(1);

        given(entryRepository.findByIdFetchAll(1)).willReturn(existingEntry);

        mockMvc.perform(post("/rest/entries/delete/{id}", 1).secure(true).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        then(entryRepository).should(times(0)).delete(existingEntry);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void generatePassword_OK() throws Exception {
        given(passwordGenerator.generate(5, PasswordType.SIMPLE)).willReturn("abcde");
        given(passwordGenerator.generate(6, PasswordType.COMPLEX)).willReturn("abcdef");

        //Should return a simple password with length 5 characters
        mockMvc.perform(get("/rest/entries/generate-password")
                .param("length", "5")
                .param("type", "simple").secure(true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password", is("abcde")));

        then(passwordGenerator).should(times(1)).generate(5, PasswordType.SIMPLE);

        //Should return a complex password with length 10 characters
        mockMvc.perform(get("/rest/entries/generate-password")
                .param("length", "6")
                .param("type", "complex").secure(true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password", is("abcdef")));

        then(passwordGenerator).should(times(1)).generate(6, PasswordType.COMPLEX);
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void generatePassword_When_Length_IsNotInteger() throws Exception {
        mockMvc.perform(get("/rest/entries/generate-password")
                .param("length", "wrong")
                .param("type", "simple").secure(true))
                .andExpect(status().isBadRequest());

        then(passwordGenerator).shouldHaveNoInteractions();
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void getPassword_OK() throws Exception {
        User user = new User(1, "user", "encrypted");
        Entry existingEntry = new Entry("existing", user);
        existingEntry.setId(1);
        existingEntry.setPassword("encrypted");

        given(entryRepository.findByIdFetchAll(1)).willReturn(existingEntry);
        given(passwordEncryptor.decrypt("encrypted")).willReturn("password");

        mockMvc.perform(get("/rest/entries/getpassword/{id}", 1).secure(true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password", is("password")));

        then(passwordEncryptor).should(times(1)).decrypt("encrypted");

        //The entry has an empty password. Should return an empty string
        existingEntry.setPassword(null);

        mockMvc.perform(get("/rest/entries/getpassword/{id}", 1).secure(true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password", is("")));

        then(passwordEncryptor).should(times(1)).decrypt("encrypted");
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void getPassword_When_EntryNotFound() throws Exception {
        given(entryRepository.findByIdFetchAll(1)).willReturn(null);

        mockMvc.perform(get("/rest/entries/getpassword/{id}", 1).secure(true))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        then(passwordEncryptor).shouldHaveNoInteractions();
    }

    @Test
    @WithMockUser(username = "user", authorities = "user")
    public void getPassword_When_EntryBelongsToAnotherUser() throws Exception {
        User anotherUser = new User(1, "another", "password");
        Entry existingEntry = new Entry("existing", anotherUser);
        existingEntry.setId(1);

        given(entryRepository.findByIdFetchAll(1)).willReturn(existingEntry);

        mockMvc.perform(get("/rest/entries/getpassword/{id}", 1).secure(true))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        then(passwordEncryptor).shouldHaveNoInteractions();
    }

}