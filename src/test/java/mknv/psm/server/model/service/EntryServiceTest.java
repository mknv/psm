package mknv.psm.server.model.service;

import java.util.List;
import mknv.psm.server.model.domain.Entry;
import mknv.psm.server.model.domain.Group;
import mknv.psm.server.model.domain.Role;
import mknv.psm.server.model.domain.User;
import mknv.psm.server.model.repository.EntryRepository;
import mknv.psm.server.model.repository.GroupRepository;
import mknv.psm.server.model.repository.RepositoryUtil;
import mknv.psm.server.model.repository.RoleRepository;
import mknv.psm.server.model.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author mknv
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EntryServiceTest {

    @Autowired
    private EntryService entryService;

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
    public void find_OK() {
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

        Entry entry1 = new Entry("entry1", user1);
        entry1.setGroup(group1);
        Entry entry2 = new Entry("entry2", user1);
        entry2.setGroup(group2);
        Entry entry3 = new Entry("entry3", user2);
        entry3.setGroup(group2);
        Entry entry4 = new Entry("entry4", user2);

        entryRepository.save(entry1);
        entryRepository.save(entry2);
        entryRepository.save(entry3);
        entryRepository.save(entry4);

        //Find by user1. Should return entry1 and entry2.
        List<Entry> result = entryService.find(user1, "", null, false);
        assertEquals(2, result.size());
        assertEquals(entry1, result.get(0));
        assertEquals(entry2, result.get(1));

        //The name is too short. Should return entry1 and entry2.
        result = entryService.find(user1, "E", null, false);
        assertEquals(2, result.size());
        assertEquals(entry1, result.get(0));
        assertEquals(entry2, result.get(1));

        //Find by name. Should return entry2.
        result = entryService.find(user1, "Y2", null, false);
        assertEquals(1, result.size());
        assertEquals(entry2, result.get(0));

        //Find by group2. Should return entry3.
        result = entryService.find(user2, null, group2, false);
        assertEquals(1, result.size());
        assertEquals(entry3, result.get(0));

        //Find by empty group. Should return entry4
        result = entryService.find(user2, "", null, true);
        assertEquals(1, result.size());
        assertEquals(entry4, result.get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void find_When_UserIsNull_Failed() {
        entryService.find(null, null, null, false);
    }

}
