package mknv.psm.server.model.service;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import mknv.psm.server.model.domain.Entry;
import mknv.psm.server.model.domain.Group;
import mknv.psm.server.model.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author mknv
 */
@Service
@Transactional
public class EntryService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Retrieves a list of entries selected by user, name and group.
     *
     * @param user must not be null
     * @param name adds the case independent substring search criteria by name
     * if the length of the name is more that 1.
     * @param group adds the search criteria by group
     * @param isEmptyGroup adds the search criteria where group is null
     * @return a list of entries
     */
    public List<Entry> find(User user, String name, Group group, boolean isEmptyGroup) {
        if (user == null) {
            throw new IllegalArgumentException("The user parameter is null.");
        }
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Entry> cq = cb.createQuery(Entry.class);
        Root<Entry> root = cq.from(Entry.class);

        Predicate predicate = cb.equal(root.get("user"), user);
        if (name != null && name.length() > 1) {
            Predicate namePredicate = cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
            predicate = cb.and(predicate, namePredicate);
        }
        if (isEmptyGroup) {
            Predicate groupPredicate = cb.isNull(root.get("group"));
            predicate = cb.and(predicate, groupPredicate);
        } else if (group != null) {
            Predicate groupPredicate = cb.equal(root.get("group"), group);
            predicate = cb.and(predicate, groupPredicate);
        }
        cq.where(predicate);
        cq.orderBy(cb.asc(root.get("name")));
        return em.createQuery(cq).getResultList();
    }
}
