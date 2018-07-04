package mknv.psm.server.web.controller.rest;

import java.util.Arrays;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import mknv.psm.server.model.domain.Group;
import mknv.psm.server.model.domain.User;
import mknv.psm.server.model.repository.GroupRepository;
import mknv.psm.server.model.repository.UserRepository;

/**
 *
 * @author mknv
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GroupRestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private GroupRepository groupRepository;

    public GroupRestControllerTest() {
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"admin", "user"})
    public void list_OK() throws Exception {
        System.out.println("list_OK");
        User admin = new User(1, "admin", "password");
        User user = new User(2, "user", "password");
        Group adminGroup = new Group("adminGroup", admin);
        Group userGroup = new Group("userGroup", user);
        given(userRepository.findByName("admin")).willReturn(admin);
        given(userRepository.findByName("user")).willReturn(user);
        given(groupRepository.findByUser(admin)).willReturn(Arrays.asList(adminGroup));
        given(groupRepository.findByUser(user)).willReturn(Arrays.asList(userGroup));

        //Should return the first group by user 'admin'. A result group should not contain a user
        mockMvc.perform(get("/rest/groups").secure(true))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("adminGroup")))
                .andExpect(jsonPath("$[0].user").doesNotExist());
    }
}
