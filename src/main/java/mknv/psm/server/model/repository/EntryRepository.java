package mknv.psm.server.model.repository;

import mknv.psm.server.model.domain.Entry;
import mknv.psm.server.model.domain.Group;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import mknv.psm.server.model.domain.User;

/**
 *
 * @author mknv
 */
public interface EntryRepository extends JpaRepository<Entry, Integer> {

    /**
     * Retrieves a list of entries by user. Sorts the result by name.
     *
     * @param user a user paremeter
     * @return a list of entries
     */
    @Query("select e from Entry e where e.user = :user order by e.name")
    List<Entry> find(@Param("user") User user);

    /**
     * Retrieves a list of entries by group. Sorts the result by name.
     *
     * @param group a group parameter
     * @return a list of entries
     */
    @Query("select e from Entry e where e.group = :group order by e.name")
    List<Entry> find(@Param("group") Group group);

    /**
     * Retrieves a list of entries selected by user and name that contains case
     * insensitive string value from the name parameter. Sorts the result by
     * name.
     *
     * @param name a name parameter
     * @param user a user parameter
     * @return a list of entries
     */
    @Query("select e from Entry e where e.user = :user and lower(e.name) like lower(concat('%', :name, '%')) order by e.name")
    List<Entry> find(@Param("name") String name, @Param("user") User user);

    /**
     * Retrieves an entry by id with related user and group.
     *
     * @param id an id parameter
     * @return an entry
     */
    @Query("select e from Entry e join fetch e.group join fetch e.user where e.id = :id")
    Entry findByIdFetchAll(@Param("id") Integer id);

}
