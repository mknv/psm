package mknv.psm.server.model.repository;

import mknv.psm.server.model.domain.Group;
import mknv.psm.server.model.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author mknv
 */
public interface GroupRepository extends JpaRepository<Group, Integer> {

    /**
     * Retrieves a list of groups by user. Sorts the result by name.
     *
     * @param user user parameter
     * @return a list of groups
     */
    @Query("select g from Group g where g.user = :user order by g.name")
    List<Group> findByUser(@Param("user") User user);

    /**
     * Retrieves a group by id with related user.
     *
     * @param id group id parameter
     * @return a group or null if no groups found
     */
    @Query("select g from Group g join fetch g.user where g.id = :id")
    Group findByIdFetchUser(@Param("id") Integer id);
}
