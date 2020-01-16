package mknv.psm.server.model.repository;

import java.util.List;
import mknv.psm.server.model.domain.Role;
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
    public void before() {
        repositoryUtil.clearDatabase();
    }

    @Test
    public void findAllUsers_FetchRoles_OK() {
        Role role = new Role(1, "role");
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);
        User user2 = new User("user2", "password");
        user2.getRoles().add(role);

        roleRepository.save(role);
        userRepository.save(user1);
        userRepository.save(user2);

        List<User> actual = userRepository.findAllFetchRoles();
        assertEquals(2, actual.size());
        assertEquals(1, actual.get(0).getRoles().size());
        assertEquals(1, actual.get(1).getRoles().size());
    }

    @Test
    public void findById_FetchRoles_OK() {
        Role role = new Role(1, "role");
        User expected = new User("expected", "password");
        expected.getRoles().add(role);

        roleRepository.save(role);
        userRepository.save(expected);

        User actual = userRepository.findByIdFetchRoles(expected.getId());
        assertEquals(expected, actual);
        assertEquals(1, actual.getRoles().size());
    }

    @Test
    public void findByName_OK() {
        Role role = new Role(1, "role");
        User expected = new User("expected", "password");
        expected.getRoles().add(role);
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);

        roleRepository.save(role);
        userRepository.save(expected);
        userRepository.save(user1);

        User actual = userRepository.findByName("expected");
        assertEquals(expected, actual);
    }

    @Test
    public void findByName_FetchRoles_OK() {
        Role role = new Role(1, "role");
        User expected = new User("expected", "password");
        expected.getRoles().add(role);
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);

        roleRepository.save(role);
        userRepository.save(expected);
        userRepository.save(user1);

        User actual = userRepository.findByNameFetchRoles("expected");
        assertEquals(expected, actual);
        assertEquals(1, actual.getRoles().size());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void saveUser_With_ExistingName_Failed() {
        Role role = new Role(1, "role");
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);

        roleRepository.save(role);
        userRepository.save(user1);

        //Attempts to save user with the same name in another case
        User user2 = new User("uSER1", "password");
        user2.getRoles().add(role);
        userRepository.save(user2);
    }
}
