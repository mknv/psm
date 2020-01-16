package mknv.psm.server.model.repository;

import java.util.List;
import mknv.psm.server.model.domain.Group;
import mknv.psm.server.model.domain.User;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import mknv.psm.server.model.domain.Role;

/**
 *
 * @author mknv
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GroupRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RepositoryUtil repositoryUtil;

    @Before
    public void setUp() {
        repositoryUtil.clearDatabase();
    }

    @Test
    public void findByUser_OK() {
        Role role = new Role(1, "role");
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);
        User user2 = new User("user2", "password");
        user2.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user1);
        userRepository.save(user2);

        Group expected = new Group("expected", user1);
        Group group1 = new Group("group1", user2);

        groupRepository.save(expected);
        groupRepository.save(group1);

        List<Group> actual = groupRepository.findByUser(user1);
        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
    }

    @Test
    public void findByIdFetchUser_OK() {
        Role role = new Role(1, "role");
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user1);

        Group expected = new Group("expected", user1);
        groupRepository.save(expected);

        Group actual = groupRepository.findByIdFetchUser(expected.getId());
        assertEquals(expected, actual);
        assertNotNull(actual.getUser());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void saveGroup_With_ExistingName_Failed() {
        //Attempts to save a group with the same name in another case
        Role role = new Role(1, "role");
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user1);

        Group group1 = new Group("group1", user1);
        groupRepository.save(group1);

        Group group2 = new Group("gROUp1", user1);
        groupRepository.save(group2);
    }
}
