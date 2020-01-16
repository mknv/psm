package mknv.psm.server.web.controller.rest;

import com.sun.tools.javac.util.List;
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
    @WithMockUser(username = "user", authorities = {"admin", "user"})
    public void list_OK() throws Exception {
        User user = new User(1, "user", "password");
        Group group = new Group("group", user);

        given(userRepository.findByName("user")).willReturn(user);
        given(groupRepository.findByUser(user)).willReturn(List.of(group));

        mockMvc.perform(get("/rest/groups").secure(true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("group")))
                .andExpect(jsonPath("$[0].user").doesNotExist());
    }
}
