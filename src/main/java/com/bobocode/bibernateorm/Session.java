package com.bobocode.bibernateorm;

import com.bobocode.bibernateorm.actions.ActionQuery;
import com.bobocode.bibernateorm.actions.DeleteAction;
import com.bobocode.bibernateorm.actions.InsertAction;
import com.bobocode.bibernateorm.actions.UpdateAction;
import com.bobocode.bibernateorm.dao.EntityDao;
import com.bobocode.bibernateorm.dao.EntityDaoImpl;
import com.bobocode.bibernateorm.exception.OrmException;
import com.bobocode.bibernateorm.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static com.bobocode.bibernateorm.util.reflection.ReflectionUtils.getFieldValue;
import static com.bobocode.bibernateorm.util.reflection.ReflectionUtils.getIdValue;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;

public class Session implements AutoCloseable {
    private final static Logger LOG = LogManager.getLogger(Session.class);
    private final EntityDao entityDao;
    private final Map<KeyTypeId<?>, Object> cache = new ConcurrentHashMap<>();
    private final Map<KeyTypeId<?>, Object[]> snapshotCopy = new ConcurrentHashMap<>();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final ActionQuery queryAction;

    public Session(DataSource dataSource) {
        this.entityDao = new EntityDaoImpl(dataSource);
        this.queryAction = new ActionQuery();
    }

    public <T> T find(Class<T> entityType, Object id) {
        checkSession(entityType, id);

        var key = new KeyTypeId<>(entityType, id);

        var entity = Optional.ofNullable(cache.get(key)).map(entityType::cast);

        if (entity.isPresent()) {
            LOG.debug("Session#Find. Get Entity from session.");
            return entity.get();
        }

        var findEntity = entityDao.selectEntity(key);

        Optional.ofNullable(findEntity)
                .ifPresent(findEnt -> {
                    cache.put(key, findEnt);
                    addSnapshot(key, findEnt);
                });

        return findEntity;
    }

    public <T> void persist(T entity) {
        checkSession(entity);

        if (getCacheKeyTypeId(entity) != null) {
            LOG.debug("Session#Persist. Entity already persisted.");
            return;
        }

        var keyTypeId = new KeyTypeId<>(entity.getClass(), getIdValue(entity));

        cache.put(keyTypeId, entity);
        addSnapshot(keyTypeId, entity);

        addInsertAction(entity, keyTypeId);
    }

    public <T> void remove(T entity) {
        checkSession(entity);

        var keyTypeId = Optional.ofNullable(getCacheKeyTypeId(entity));

        if (keyTypeId.isEmpty()) {
            LOG.debug("Session#Remove. Entity not persist yet. Cannot remove.");
            throw new OrmException("Entity not persist.");
        }

        addDeleteAction(entity, keyTypeId.get());
    }

    public void close() {
        checkSession();

        checkToChangeAndAddToUpdateAction();

        flush();

        isClosed.set(true);
    }

    public void flush() {
        checkSession();

        queryAction.execute();
    }

    public <T> void update(T entity) {
        checkSession();

        var keyTypeId = getCacheKeyTypeId(entity);

        if (keyTypeId == null) {
            LOG.debug("Session#Update. Entity not persist yet. Persist now.");
            persist(entity);
        } else {
            addUpdateAction(entity, keyTypeId);
        }
    }

    public <T> void removeSnapshot(KeyTypeId<?> key) {
        snapshotCopy.remove(key);
    }

    public Map<KeyTypeId<?>, Object> getCache() {
        return cache;
    }

    public EntityDao getEntityDao() {
        return entityDao;
    }

    private void checkSession(Object... object) {

        if (isClosed.get()) {
            LOG.debug("Session is closed.");
            throw new OrmException("Session is closed");
        }

        if (object == null) throw new NullPointerException("Not available NULL.");

        for (Object o : object) {
            requireNonNull(o);
        }

    }

    private void updateSnapshotValues(Map.Entry<KeyTypeId<?>, Object> cacheKeyEntities) {
        checkSession(cacheKeyEntities.getValue());

        var idValue = getIdValue(cacheKeyEntities.getValue());

        if (!Objects.equals(idValue, cacheKeyEntities.getKey().id())) {
            persist(cacheKeyEntities.getValue());
        } else {
            addUpdateAction(cacheKeyEntities.getValue(), cacheKeyEntities.getKey());
        }
    }

    public <T> void addSnapshot(KeyTypeId<?> key, T entity) {
        var fields = Stream.of(key.getType().getDeclaredFields()).sorted(comparing(Field::getName)).map(field -> getFieldValue(field, entity)).toArray();
        snapshotCopy.put(key, fields);
    }

    private boolean hasChanged(Map.Entry<KeyTypeId<?>, Object> keyTypeIdObjectEntry) {
        return Utils.hasChanged(keyTypeIdObjectEntry, snapshotCopy);
    }

    private <T> void addDeleteAction(T entity, KeyTypeId<?> keyTypeId) {
        queryAction.addDeleteAction(new DeleteAction(entity, this, keyTypeId));
    }

    private <T> void addInsertAction(T entity, KeyTypeId<?> keyTypeId) {
        queryAction.addInsertAction(new InsertAction(entity, this, keyTypeId));
    }

    private <T> void addUpdateAction(T entity, KeyTypeId<?> keyTypeId) {
        queryAction.addUpdateAction(new UpdateAction(entity, this, keyTypeId));
    }

    private <T> KeyTypeId<?> getCacheKeyTypeId(T entity) {
        return cache.entrySet().stream().filter(entry -> entry.getValue().equals(entity)).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    private void checkToChangeAndAddToUpdateAction() {
        cache.entrySet()
                .stream()
                .filter(this::hasChanged)
                .forEach(this::updateSnapshotValues);
    }

}
