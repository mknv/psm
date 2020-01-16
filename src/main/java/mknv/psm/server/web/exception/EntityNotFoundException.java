package mknv.psm.server.web.exception;

/**
 *
 * @author mknv
 */
public class EntityNotFoundException extends RuntimeException {

    private Class clazz;
    private Object entityId;

    public EntityNotFoundException(Class clazz, Object entityId) {
        this.clazz = clazz;
        this.entityId = entityId;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public Object getEntityId() {
        return entityId;
    }

    public void setEntityId(Object entityId) {
        this.entityId = entityId;
    }
}
