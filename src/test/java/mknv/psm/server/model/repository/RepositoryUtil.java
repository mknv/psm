package mknv.psm.server.model.repository;



import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 *
 * @author mknv
 */
@Component
public class RepositoryUtil {

    @Autowired
    private DataSource dataSource;

    public void clearDatabase() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.update("delete from entries");
        template.update("delete from groups");
        template.update("delete from users_roles");
        template.update("delete from users");
        template.update("delete from roles");
    }
}