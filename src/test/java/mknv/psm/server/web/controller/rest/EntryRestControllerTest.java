package mknv.psm.server.web.controller.rest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    private PasswordGenerator passwordGenerator;
    @Autowired
    private PasswordEncryptor passwordEncryptor;

    @Test
    public void findAll_OK() throws Exception {
        System.out.println("findAll_OK");
        User admin = new User(1, "admin", "password");
        User user = new User(2, "user", "password");

        Group adminGroup = new Group(1, "adminGroup", admin);
        Group userGroup = new Group(2, "userGroup", user);

        Entry adminEntry = new Entry(1, "adminEntry", null, null, null, null, null, adminGroup, admin);
        Entry userEntry1 = new Entry(2, "userEntry1", "login1", "email1", "password1", "description1", LocalDate.of(2018, 1, 1), userGroup, user);
        Entry userEntry2 = new Entry(3, "userEntry2", "login2", "email2", "password2", "description2", LocalDate.of(2018, 2, 2), userGroup, user);

        given(userRepository.findByName("admin")).willReturn(admin);
        given(userRepository.findByName("user")).willReturn(user);

        given(entryRepository.find(admin)).willReturn(Arrays.asList(adminEntry));
        given(entryRepository.find(user)).willReturn(Arrays.asList(userEntry1, userEntry2));
        given(entryRepository.find("", user)).willReturn(Arrays.asList(userEntry1, userEntry2));
        given(entryRepository.find("USER", user)).willReturn(Arrays.asList(userEntry1, userEntry2));
        given(entryRepository.find("1", user)).willReturn(Arrays.asList(userEntry1));
        given(entryRepository.find("2", user)).willReturn(Arrays.asList(userEntry2));
        given(entryRepository.find("WRONG", user)).willReturn(Collections.emptyList());

        //Returns a list of entries selected by current user. If a name parameter is present then
        //returns entries by user only else returns entries selected by user and name that contains case
        //insensitive string value from the name parameter
        //Should return only adminEntry. Without name parameter by user 'admin'.
        mockMvc.perform(get("/rest/entries").secure(true)
                .with(user("admin").authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("adminEntry")))
                .andExpect(jsonPath("$[0].login", nullValue()))
                .andExpect(jsonPath("$[0].email", nullValue()))
                .andExpect(jsonPath("$[0].password", nullValue()))
                .andExpect(jsonPath("$[0].description", nullValue()))
                .andExpect(jsonPath("$[0].expiredDate", nullValue()))
                .andExpect(jsonPath("$[0].group").doesNotExist())
                .andExpect(jsonPath("$[0].user").doesNotExist());

        //Should return only userEntry1. Without name parameter by user 'user'.
        mockMvc.perform(get("/rest/entries").secure(true)
                .with(user("user").authorities(new SimpleGrantedAuthority("user"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].name", is("userEntry1")))
                .andExpect(jsonPath("$[0].login", is("login1")))
                .andExpect(jsonPath("$[0].email", is("email1")))
                .andExpect(jsonPath("$[0].password", is("password1")))
                .andExpect(jsonPath("$[0].description", is("description1")))
                .andExpect(jsonPath("$[0].expiredDate", is("2018-01-01")))
                .andExpect(jsonPath("$[0].group").doesNotExist())
                .andExpect(jsonPath("$[0].user").doesNotExist())
                .andExpect(jsonPath("$[1].id", is(3)))
                .andExpect(jsonPath("$[1].name", is("userEntry2")))
                .andExpect(jsonPath("$[1].login", is("login2")))
                .andExpect(jsonPath("$[1].email", is("email2")))
                .andExpect(jsonPath("$[1].password", is("password2")))
                .andExpect(jsonPath("$[1].description", is("description2")))
                .andExpect(jsonPath("$[1].expiredDate", is("2018-02-02")))
                .andExpect(jsonPath("$[1].group").doesNotExist())
                .andExpect(jsonPath("$[1].user").doesNotExist());

        //Should return both entries. The name parameter is empty by user 'user'.
        mockMvc.perform(get("/rest/entries").param("name", "").secure(true)
                .with(user("user").authorities(new SimpleGrantedAuthority("user"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].name", is("userEntry1")))
                .andExpect(jsonPath("$[0].login", is("login1")))
                .andExpect(jsonPath("$[0].email", is("email1")))
                .andExpect(jsonPath("$[0].password", is("password1")))
                .andExpect(jsonPath("$[0].description", is("description1")))
                .andExpect(jsonPath("$[0].expiredDate", is("2018-01-01")))
                .andExpect(jsonPath("$[0].group").doesNotExist())
                .andExpect(jsonPath("$[0].user").doesNotExist())
                .andExpect(jsonPath("$[1].id", is(3)))
                .andExpect(jsonPath("$[1].name", is("userEntry2")))
                .andExpect(jsonPath("$[1].login", is("login2")))
                .andExpect(jsonPath("$[1].email", is("email2")))
                .andExpect(jsonPath("$[1].password", is("password2")))
                .andExpect(jsonPath("$[1].description", is("description2")))
                .andExpect(jsonPath("$[1].expiredDate", is("2018-02-02")))
                .andExpect(jsonPath("$[1].group").doesNotExist())
                .andExpect(jsonPath("$[1].user").doesNotExist());

        //Should return both entries. The name parameter is 'USER' by user 'user'.
        mockMvc.perform(get("/rest/entries").param("name", "USER").secure(true)
                .with(user("user").authorities(new SimpleGrantedAuthority("user"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].name", is("userEntry1")))
                .andExpect(jsonPath("$[0].login", is("login1")))
                .andExpect(jsonPath("$[0].email", is("email1")))
                .andExpect(jsonPath("$[0].password", is("password1")))
                .andExpect(jsonPath("$[0].description", is("description1")))
                .andExpect(jsonPath("$[0].expiredDate", is("2018-01-01")))
                .andExpect(jsonPath("$[0].group").doesNotExist())
                .andExpect(jsonPath("$[0].user").doesNotExist())
                .andExpect(jsonPath("$[1].id", is(3)))
                .andExpect(jsonPath("$[1].name", is("userEntry2")))
                .andExpect(jsonPath("$[1].login", is("login2")))
                .andExpect(jsonPath("$[1].email", is("email2")))
                .andExpect(jsonPath("$[1].password", is("password2")))
                .andExpect(jsonPath("$[1].description", is("description2")))
                .andExpect(jsonPath("$[1].expiredDate", is("2018-02-02")))
                .andExpect(jsonPath("$[1].group").doesNotExist())
                .andExpect(jsonPath("$[1].user").doesNotExist());

        //Should return only userEntry1. The name parameter is '1' by user 'user'.
        mockMvc.perform(get("/rest/entries").param("name", "1").secure(true)
                .with(user("user").authorities(new SimpleGrantedAuthority("user"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].name", is("userEntry1")))
                .andExpect(jsonPath("$[0].login", is("login1")))
                .andExpect(jsonPath("$[0].email", is("email1")))
                .andExpect(jsonPath("$[0].password", is("password1")))
                .andExpect(jsonPath("$[0].description", is("description1")))
                .andExpect(jsonPath("$[0].expiredDate", is("2018-01-01")))
                .andExpect(jsonPath("$[0].group").doesNotExist())
                .andExpect(jsonPath("$[0].user").doesNotExist());

        //Should return only userEntry2. The name parameter is '2' by user 'user'.
        mockMvc.perform(get("/rest/entries").param("name", "2").secure(true)
                .with(user("user").authorities(new SimpleGrantedAuthority("user"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(3)))
                .andExpect(jsonPath("$[0].name", is("userEntry2")))
                .andExpect(jsonPath("$[0].login", is("login2")))
                .andExpect(jsonPath("$[0].email", is("email2")))
                .andExpect(jsonPath("$[0].password", is("password2")))
                .andExpect(jsonPath("$[0].description", is("description2")))
                .andExpect(jsonPath("$[0].expiredDate", is("2018-02-02")))
                .andExpect(jsonPath("$[0].group").doesNotExist())
                .andExpect(jsonPath("$[0].user").doesNotExist());

        //Should return an empty list. The name parameter is 'WRONG' by user 'user'.
        mockMvc.perform(get("/rest/entries").param("name", "WRONG").secure(true)
                .with(user("user").authorities(new SimpleGrantedAuthority("user"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void findByGroup_OK() throws Exception {
        System.out.println("findByGroup_OK");
        User admin = new User(1, "admin", "password");
        User user = new User(2, "user", "password");

        Group adminGroup = new Group(1, "adminGroup", admin);
        Group userGroup = new Group(2, "userGroup", user);

        Entry adminEntry = new Entry(1, "adminEntry", null, null, null, null, null, adminGroup, admin);
        Entry userEntry1 = new Entry(2, "userEntry1", "login1", "email1", "password1", "description1", LocalDate.of(2018, 1, 1), userGroup, user);

        given(groupRepository.findByIdFetchUser(1)).willReturn(adminGroup);
        given(groupRepository.findByIdFetchUser(2)).willReturn(userGroup);

        given(entryRepository.find(adminGroup)).willReturn(Arrays.asList(adminEntry));
        given(entryRepository.find(userGroup)).willReturn(Arrays.asList(userEntry1));

        //Should return the adminEntry by the adminGroup
        mockMvc.perform(get("/rest/entries/group/1").secure(true)
                .with(user("admin").authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("adminEntry")))
                .andExpect(jsonPath("$[0].login", nullValue()))
                .andExpect(jsonPath("$[0].email", nullValue()))
                .andExpect(jsonPath("$[0].password", nullValue()))
                .andExpect(jsonPath("$[0].description", nullValue()))
                .andExpect(jsonPath("$[0].expiredDate", nullValue()))
                .andExpect(jsonPath("$[0].group").doesNotExist())
                .andExpect(jsonPath("$[0].user").doesNotExist());

        //Should return both entries by the userGroup
        mockMvc.perform(get("/rest/entries/group/2").secure(true)
                .with(user("user").authorities(new SimpleGrantedAuthority("user"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].name", is("userEntry1")))
                .andExpect(jsonPath("$[0].login", is("login1")))
                .andExpect(jsonPath("$[0].email", is("email1")))
                .andExpect(jsonPath("$[0].password", is("password1")))
                .andExpect(jsonPath("$[0].description", is("description1")))
                .andExpect(jsonPath("$[0].expiredDate", is("2018-01-01")))
                .andExpect(jsonPath("$[0].group").doesNotExist())
                .andExpect(jsonPath("$[0].user").doesNotExist());

        //Should return not found status when a group does not exist
        mockMvc.perform(get("/rest/entries/group/3").secure(true)
                .with(user("user").authorities(new SimpleGrantedAuthority("user"))))
                .andExpect(status().isNotFound());

        //Should return forbidden status when a group belongs to another user
        mockMvc.perform(get("/rest/entries/group/2").secure(true)
                .with(user("admin").authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"user"})
    public void generatePassword_OK() throws Exception {
        System.out.println("generatePassword_OK");
        given(passwordGenerator.generate(10, PasswordType.SIMPLE)).willReturn("simplepass");
        given(passwordGenerator.generate(11, PasswordType.COMPLEX)).willReturn("complexpass");

        //Should return a password of type simple with length 10
        mockMvc.perform(get("/rest/entries/generate-password")
                .param("length", "10").param("type", "simple").secure(true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password", is("simplepass")));

        //Should return a password of type complex with length 11
        mockMvc.perform(get("/rest/entries/generate-password")
                .param("length", "11").param("type", "complex").secure(true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password", is("complexpass")));
    }

    @Test
    public void getPassword_OK() throws Exception {
        System.out.println("getPassword_OK");
        User admin = new User(1, "admin", "password");
        User user = new User(2, "user", "password");

        Group adminGroup = new Group(1, "adminGroup", admin);
        Group userGroup = new Group(2, "userGroup", user);

        Entry adminEntry = new Entry(1, "adminEntry", null, null, null, null, null, adminGroup, admin);
        Entry userEntry1 = new Entry(2, "userEntry1", "login1", "email1",
                passwordEncryptor.encrypt("password1"), "description1", LocalDate.of(2018, 1, 1), userGroup, user);

        given(entryRepository.findByIdFetchAll(1)).willReturn(adminEntry);
        given(entryRepository.findByIdFetchAll(2)).willReturn(userEntry1);

        //Should return userEntry1's password
        mockMvc.perform(get("/rest/entries/getpassword/2").secure(true)
                .with(user("user").authorities(new SimpleGrantedAuthority("user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password", is("password1")));

        //Should return not found status when an entry is not exist
        mockMvc.perform(get("/rest/entries/getpassword/89").secure(true)
                .with(user("user").authorities(new SimpleGrantedAuthority("user"))))
                .andExpect(status().isNotFound());

        //Should return forbidden status when attempts to get password from the entry that
        //belongs to another user
        mockMvc.perform(get("/rest/entries/getpassword/1").secure(true)
                .with(user("user").authorities(new SimpleGrantedAuthority("user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void delete_OK() throws Exception {
        System.out.println("delete_OK");
        User admin = new User(1, "admin", "password");
        User user = new User(2, "user", "password");

        Group adminGroup = new Group(1, "adminGroup", admin);
        Group userGroup = new Group(2, "userGroup", user);

        Entry adminEntry = new Entry(1, "adminEntry", null, null, null, null, null, adminGroup, admin);
        Entry userEntry1 = new Entry(2, "userEntry1", "login1", "email1",
                "password1", "description1", LocalDate.of(2018, 1, 1), userGroup, user);

        given(entryRepository.findByIdFetchAll(1)).willReturn(adminEntry);
        given(entryRepository.findByIdFetchAll(2)).willReturn(userEntry1);

        //Should delete the entry succesfully
        mockMvc.perform(post("/rest/entries/delete/1").secure(true).with(csrf())
                .with(user("admin").authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk());

        //Should return not found status when attempts to delete the non existent entry
        mockMvc.perform(post("/rest/entries/delete/3").secure(true).with(csrf())
                .with(user("admin").password("password").authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isNotFound());

        //Should return forbidden status when the entry belongs to another user
        mockMvc.perform(post("/rest/entries/delete/2").secure(true).with(csrf())
                .with(user("admin").password("password").authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isForbidden());
    }

}
