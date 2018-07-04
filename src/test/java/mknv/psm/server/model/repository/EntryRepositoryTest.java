package mknv.psm.server.model.repository;

import mknv.psm.server.model.repository.EntryRepository;
import mknv.psm.server.model.repository.UserRepository;
import mknv.psm.server.model.repository.GroupRepository;
import mknv.psm.server.model.repository.RoleRepository;
import mknv.psm.server.model.domain.Entry;
import mknv.psm.server.model.domain.Group;
import mknv.psm.server.model.domain.User;
import java.time.LocalDate;
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
        Entry entry1 = new Entry("entry1", "login1", "email1", "password1", "description1", LocalDate.now(), group1, user1);
        Entry entry2 = new Entry("entry2", "login2", "email2", "password2", "description2", LocalDate.now(), group2, user2);
        entryRepository.save(entry1);
        entryRepository.save(entry2);

        assertEquals(userRepository.findAll().size(), 2);
        assertEquals(groupRepository.findAll().size(), 2);
        assertEquals(entryRepository.findAll().size(), 2);

        //Should return only entry1. Entry's user and group are not fetched eagerly.
        List<Entry> result = entryRepository.find(user1);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0), entry1);
    }

    @Test
    public void findByGroup_OK() {
        System.out.println("findByGroup_OK");
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
        Entry entry1 = new Entry("entry1", "login1", "email1", "password1", "description1", LocalDate.now(), group1, user1);
        Entry entry2 = new Entry("entry2", "login2", "email2", "password2", "description2", LocalDate.now(), group2, user2);
        entryRepository.save(entry1);
        entryRepository.save(entry2);

        assertEquals(userRepository.findAll().size(), 2);
        assertEquals(groupRepository.findAll().size(), 2);
        assertEquals(entryRepository.findAll().size(), 2);

        //Should return only entry1. Entry's user and group are not fetched eagerly.
        List<Entry> result = entryRepository.find(group1);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0), entry1);
    }

    @Test
    public void findByNameAndUser_OK() {
        System.out.println("findByNameAndUser_OK");
        Role role = new Role(1, "role");
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);
        User user2 = new User("user2", "password");
        user2.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user1);
        userRepository.save(user2);
        Group group1 = new Group("group1", user1);
        groupRepository.save(group1);
        Entry entry1 = new Entry("entry1", "login1", "email1", "password1", "description1", LocalDate.now(), group1, user1);
        Entry entry2 = new Entry("entry2", "login2", "email2", "password2", "description2", LocalDate.now(), group1, user1);
        entryRepository.save(entry1);
        entryRepository.save(entry2);

        assertEquals(userRepository.findAll().size(), 2);
        assertEquals(groupRepository.findAll().size(), 1);
        assertEquals(entryRepository.findAll().size(), 2);

        //Should return an empty list because there are no entries by user2
        List<Entry> result = entryRepository.find("", user2);
        assertEquals(result.size(), 0);
        //Should return both entries because the name is empty
        result = entryRepository.find("", user1);
        assertEquals(result.size(), 2);
        //Should return an empty list because there are no entries with name that contains such text
        result = entryRepository.find("WRONG", user1);
        assertEquals(result.size(), 0);
        //Should return entry1
        result = entryRepository.find("RY1", user1);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0), entry1);
        //Should return entry2
        result = entryRepository.find("RY2", user1);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0), entry2);
        //Should return both entries
        result = entryRepository.find("eNTRy", user1);
        assertEquals(result.size(), 2);
    }

    @Test
    public void findByIdFetchAll_OK() {
        System.out.println("findByIdFetchAll_OK");
        Role role = new Role(1, "role");
        User user1 = new User("user1", "password");
        user1.getRoles().add(role);
        roleRepository.save(role);
        userRepository.save(user1);
        Group group1 = new Group("group1", user1);
        groupRepository.save(group1);
        Entry entry1 = new Entry("entry1", "login1", "email1", "password1", "description1", LocalDate.now(), group1, user1);
        entryRepository.save(entry1);

        assertEquals(userRepository.findAll().size(), 1);
        assertEquals(groupRepository.findAll().size(), 1);
        assertEquals(entryRepository.findAll().size(), 1);

        //Should return the entry1 with user and group that are fetched eagerly.
        Entry result = entryRepository.findByIdFetchAll(entry1.getId());
        assertEquals(result.getUser(), user1);
        assertEquals(result.getGroup(), group1);
    }

    @Test
    public void getDaysLeft_OK() {
        System.out.println("getDaysLeft_OK");
        LocalDate testDate = LocalDate.now().plusDays(5);
        Entry entry = new Entry();
        entry.setExpiredDate(null);
        //Should return null if the expired date parameter is null
        assertNull(entry.getDaysLeft());
        //Shoud return 5
        entry.setExpiredDate(testDate);
        assertEquals(entry.getDaysLeft(), Integer.valueOf(5));
    }
}