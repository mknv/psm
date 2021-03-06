package mknv.psm.server.model.repository;

import mknv.psm.server.model.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author mknv
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Retrieves all users. Fetches roles eagerly. Sorts the result by name.
     *
     * @return a list of users
     */
    @Query("select distinct u from User u left join fetch u.roles order by u.name")
    List<User> findAllFetchRoles();

    /**
     * Retrieves a user by id. Fetches roles eagerly.
     *
     * @param id a user id
     * @return a user or null if no users found
     */
    @Query("select distinct u from User u left join fetch u.roles where u.id = :id")
    User findByIdFetchRoles(@Param("id") Integer id);

    /**
     * Retrieves a user by name.
     *
     * @param name a user name
     * @return a user or null if no users found
     */
    @Query("select u from User u where u.name = :name")
    User findByName(@Param("name") String name);

    /**
     * Retrieves a user by name. Fetches roles eagerly.
     *
     * @param name a user name
     * @return a user or null if no users found
     */
    @Query("select distinct u from User u left join fetch u.roles where u.name = :name")
    User findByNameFetchRoles(@Param("name") String name);
}
