package com.bobocode.bibernateorm.dao;

import com.bobocode.bibernateorm.KeyTypeId;

public interface EntityDao {
    <T> void updateEntity(T entity, Object id);

    <T> T selectEntity(KeyTypeId<T> key);

    <T> void saveEntity(T entity);

    <T> void deleteEntity(T entity);
}
