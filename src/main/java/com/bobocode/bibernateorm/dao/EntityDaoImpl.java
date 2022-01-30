package com.bobocode.bibernateorm.dao;

import com.bobocode.bibernateorm.KeyTypeId;
import com.bobocode.bibernateorm.annotation.GenerationType;
import com.bobocode.bibernateorm.exception.SqlException;
import com.bobocode.bibernateorm.util.reflection.FieldNameValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bobocode.bibernateorm.util.SqlQueryBuilder.*;
import static com.bobocode.bibernateorm.util.reflection.ReflectionUtils.*;
import static java.util.Objects.requireNonNull;


public record EntityDaoImpl(DataSource dataSource) implements EntityDao {
    private final static Logger LOG = LogManager.getLogger(EntityDaoImpl.class);

    @Override
    public <T> T selectEntity(KeyTypeId<T> key) {
        requireNonNull(key);

        var entity = initialEntity(key.getType());

        var sql = selectEntityQuery(key.getType());
        LOG.debug("Method selectEntity. Sql query = " + sql);

        final var finalEntity = entity;

        entity = executeQueryPs(ps -> {
            try {
                ps.setObject(1, key.getId());

                var resultSet = ps.executeQuery();

                if (resultSet.next()) {

                    Stream.of(key.getType().getDeclaredFields()).forEach(field -> {
                        var resultSetValue = getResultSetValue(resultSet, field);

                        setFieldValue(field, resultSetValue, finalEntity);
                    });

                    return finalEntity;
                }

                return null;

            } catch (SQLException e) {
                LOG.error(String.format("Error preparedStatement. Method selectEntity failed. Entity id = %s", key.getId()));
                throw new SqlException(String.format("Error preparedStatement. Method selectEntity failed. Entity id = %s", key.getId()), e);
            }
        }, sql);

        return entity;
    }

    @Override
    public <T> void saveEntity(T entity) {
        var id = getIdField(entity);

        GenerationType generationType = getGeneratedType(id);

        final Map<String, Object> columnNameValues;

        switch (generationType) {
            case IDENTITY -> columnNameValues = getColumnNameValuesWithOutId(entity);
            default -> columnNameValues = getColumnNameValues(entity);
        }

        var sql = saveEntityQuery(entity, columnNameValues.keySet());
        LOG.debug("Method saveEntity. Sql query = " + sql);

        executeUpdatePs(ps -> {

            var atomicInteger = new AtomicInteger(1);

            columnNameValues.values().forEach(value -> {

                try {
                    ps.setObject(atomicInteger.getAndIncrement(), value);
                } catch (SQLException e) {
                    LOG.error(String.format("Error setValue %s to preparedStatement ", value));
                    throw new SqlException(String.format("Error setValue to preparedStatement %s", entity), e);
                }

            });

            try {
                ps.executeUpdate();
            } catch (SQLException e) {
                LOG.error("Error saveEntity. Failed executeUpdate preparedStatement.");
                throw new SqlException("Error saveEntity. Failed executeUpdate preparedStatement.", e);
            }
        }, sql);

    }

    @Override
    public <T> void deleteEntity(T entity) {

        var sql = deleteEntityQuery(entity);
        LOG.debug("Method deleteEntity. Sql query = " + sql);

        executeUpdatePs(ps ->
        {
            try {
                ps.setObject(1, getIdValue(entity));
                ps.executeUpdate();
            } catch (Exception e) {
                LOG.error("Error deleteEntity.");
                throw new SqlException("Error deleteEntity", e);
            }
        }, sql);

    }

    @Override
    public <T> void updateEntity(T entity, Object id) {
        var fieldNameValues = Stream.of(entity.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .filter(field -> !getColumnName(field).equalsIgnoreCase("id"))
                .map(field -> new FieldNameValue(getColumnName(field), getFieldValue(field, entity))).collect(Collectors.toList());

        var sql = updateEntityQuery(entity, fieldNameValues);
        LOG.debug("Method updateEntity. Sql query =  = " + sql);

        executeUpdatePs(ps -> {
            int i = 1;
            try {
                for (FieldNameValue fieldNameValue : fieldNameValues) {
                    ps.setObject(i++, fieldNameValue.value());
                }
                ps.setObject(i, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, sql);

    }

    private Object getResultSetValue(final ResultSet resultSet, final Field field) {
        try {
            return resultSet.getObject(getColumnName(field));
        } catch (SQLException e) {
            LOG.error("Error getObject from ResultSet. Fail field name = " + field.getName());
            throw new SqlException("Error getObject from ResultSet. Fail field name = " + field.getName(), e);
        }
    }

    private void executeUpdatePs(Consumer<PreparedStatement> consumer, String sqlQuery) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                consumer.accept(preparedStatement);
            } catch (SQLException e) {
                LOG.error("Error executeUpdatePs.");
                throw new SqlException("Error executeUpdatePs.", e);
            }

        } catch (SQLException e) {
            LOG.error("Error getConnection. Method executeUpdatePs failed.");
            throw new SqlException("Error getConnection. Method executeUpdatePs failed.", e);
        }
    }

    private <T> T executeQueryPs(Function<PreparedStatement, T> function, String sqlQuery) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                return function.apply(preparedStatement);
            } catch (SQLException e) {
                LOG.error("Error preparedStatement. Method executeQueryPs failed.");
                throw new SqlException("Error preparedStatement. Method executeQueryPs failed.", e);
            }

        } catch (SQLException e) {
            LOG.error("Error getConnection. Method executeQueryPs failed.");
            throw new SqlException("Error getConnection. Method executeQueryPs failed.", e);
        }
    }

}
