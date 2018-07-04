package mknv.psm.server.model.repository;

import mknv.psm.server.model.repository.UserRepository;
import mknv.psm.server.model.repository.RoleRepository;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import mknv.psm.server.model.domain.Role;
import mknv.psm.server.model.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author mknv
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RepositoryUtil repositoryUtil;

    @Before
    @Transactional
    public void before() {
        repositoryUtil.clearDatabase();
    }

    @Test
    public void findAllFetchRoles_OK() {
        System.out.println("findAllFetchRoles_OK");
        Role role = new Role(1, "role1");
        User user1 = new User("user1", "password");
        User user2 = new User("user2", "password");
        user1.getRoles().add(role);
        user2.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user1);
        userRepository.save(user2);

        List<User> result = userRepository.findAllFetchRoles();
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getRoles().get(0), role);
        assertEquals(result.get(1).getRoles().get(0), role);
    }

    @Test
    public void findByIdFetchRoles_OK() {
        System.out.println("findByIdFetchRoles_OK");
        Role role = new Role(1, "role1");
        User user = new User("user1", "password");
        user.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user);

        User result = userRepository.findByIdFetchRoles(user.getId());
        assertEquals(result, user);
        assertEquals(result.getRoles().get(0), role);
    }

    @Test
    public void findByName_OK() {
        System.out.println("findByName_OK");
        Role role = new Role(1, "role1");
        String username = "user1";
        User user = new User(username, "password");
        user.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user);

        //Roles are not fetched eagerly
        User result = userRepository.findByName(username);
        assertEquals(result, user);
    }

    @Test
    public void findByNameFetchRoles_OK() {
        System.out.println("findByNameFetchRoles_OK");
        Role role = new Role(1, "role1");
        String username = "user1";
        User user = new User(username, "password");
        user.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user);

        User result = userRepository.findByNameFetchRoles(username);
        assertEquals(result, user);
        assertEquals(result.getRoles().get(0), role);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void saveWithExistingName_Failed() {
        System.out.println("saveWithExistingName_Failed");
        Role role = new Role(1, "role1");
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user1);

        //The unique index in DB for the name field is case insensitive
        User userWithExistingName = new User("uSER1", "password");
        userWithExistingName.getRoles().add(role);
        userRepository.save(userWithExistingName);
    }
}
