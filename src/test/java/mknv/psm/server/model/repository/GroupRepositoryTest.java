package mknv.psm.server.model.repository;

import mknv.psm.server.model.repository.UserRepository;
import mknv.psm.server.model.repository.GroupRepository;
import mknv.psm.server.model.repository.RoleRepository;
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
        System.out.println("findByUser_OK");
        Role role = new Role(1, "role");
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);
        User user2 = new User("user2", "password");
        user2.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user1);
        userRepository.save(user2);
        Group group1 = new Group("group1", user1);
        Group group2 = new Group("group2", user2);
        groupRepository.save(group1);
        groupRepository.save(group2);

        assertEquals(userRepository.findAll().size(), 2);
        assertEquals(groupRepository.findAll().size(), 2);
        //Should return only group1. A group's user is not fetched eagerly.
        List<Group> result = groupRepository.findByUser(user1);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0), group1);
    }

    @Test
    public void findByIdFetchUser_OK() {
        System.out.println("findByIdFetchUser_OK");
        Role role = new Role(1, "role");
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user1);
        Group group1 = new Group("group1", user1);
        groupRepository.save(group1);

        //Should return the group with the user
        Group result = groupRepository.findByIdFetchUser(group1.getId());
        assertEquals(result, group1);
        assertEquals(result.getUser(), user1);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void createWithExistingNameAndUser_Failed() {
        System.out.println("createWithExistingNameAndUser_Failed");
        Role role = new Role(1, "role");
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user1);
        Group group1 = new Group("group1", user1);
        groupRepository.save(group1);

        assertEquals(groupRepository.findAll().size(), 1);
        //The unique index in DB for the name field is case insensitive
        Group groupWithExistingNameAndUser = new Group("gROUp1", user1);
        groupRepository.save(groupWithExistingNameAndUser);
    }
}
