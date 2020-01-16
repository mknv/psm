package mknv.psm.server.model.repository;

import mknv.psm.server.model.domain.Entry;
import mknv.psm.server.model.domain.Group;
import mknv.psm.server.model.domain.User;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class EntryRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private EntryRepository entryRepository;
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

        Entry expected = new Entry("expected", user1);
        Entry entry1 = new Entry("entry1", user2);

        entryRepository.save(expected);
        entryRepository.save(entry1);

        List<Entry> actual = entryRepository.findByUser(user1);
        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
    }

    @Test
    public void findByEmptyGroupAndUser_OK() {
        Role role = new Role(1, "role");
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);
        User user2 = new User("user2", "password");
        user2.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user1);
        userRepository.save(user2);

        Group group = new Group("group", user1);
        groupRepository.save(group);

        Entry expected = new Entry("expected", user1);
        Entry entryWithGroup = new Entry("entryWithGroup", user1);
        entryWithGroup.setGroup(group);
        Entry entryWithAnotherUser = new Entry("entryWithAnotherUser", user2);

        entryRepository.save(expected);
        entryRepository.save(entryWithGroup);
        entryRepository.save(entryWithAnotherUser);

        List<Entry> actual = entryRepository.findByEmptyGroup(user1);
        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
    }

    @Test
    public void findByName_Substring_IgnoringCase_AndUser_OK() {
        Role role = new Role(1, "role");
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);
        User user2 = new User("user2", "password");
        user2.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user1);
        userRepository.save(user2);

        Entry expected = new Entry("expected", user1);
        Entry entryWithAnotherName = new Entry("entry1", user1);
        Entry entryWithAnotherUser = new Entry("expected", user2);

        entryRepository.save(expected);
        entryRepository.save(entryWithAnotherName);
        entryRepository.save(entryWithAnotherUser);

        List<Entry> actual = entryRepository.find("xPeCT", user1);
        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
    }

    @Test
    public void findByIdFetchAll_OK() {
        //Should return the entry with group and user
        Role role = new Role(1, "role");
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user1);

        Group group = new Group("group", user1);
        groupRepository.save(group);

        Entry expected = new Entry("expected", user1);
        expected.setGroup(group);
        Entry entry1 = new Entry("entry1", user1);

        entryRepository.save(expected);
        entryRepository.save(entry1);

        Entry actual = entryRepository.findByIdFetchAll(expected.getId());
        assertEquals(expected, actual);
        assertNotNull(actual.getGroup());
        assertNotNull(actual.getUser());
    }
}
