package mknv.psm.server.model.repository;

import mknv.psm.server.model.domain.Entry;
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
     * @param user a user
     * @return a list of entries
     */
    @Query("select e from Entry e where e.user = :user order by e.name")
    List<Entry> findByUser(@Param("user") User user);

    /**
     * Retrieves a list of entries where group is null. Sorts the result by
     * name.
     *
     * @param user a user
     * @return a list of entries
     */
    @Query("select e from Entry e where e.user = :user and e.group is null order by e.name")
    List<Entry> findByEmptyGroup(@Param("user") User user);

    /**
     * Retrieves a list of entries selected by user and name where name contains
     * case insensitive string value from the name parameter. Sorts the result
     * by name.
     *
     * @param name a name
     * @param user a user
     * @return a list of entries
     */
    @Query("select e from Entry e where e.user = :user and lower(e.name) like lower(concat('%', :name, '%')) order by e.name")
    List<Entry> find(@Param("name") String name, @Param("user") User user);

    /**
     * Retrieves an entry by id. Fetches user and group eagerly.
     *
     * @param id an id
     * @return an entry
     */
    @Query("select e from Entry e left join fetch e.group join fetch e.user where e.id = :id")
    Entry findByIdFetchAll(@Param("id") Integer id);

}
