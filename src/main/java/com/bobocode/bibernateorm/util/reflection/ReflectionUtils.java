package com.bobocode.bibernateorm.util.reflection;

import com.bobocode.bibernateorm.annotation.Column;
import com.bobocode.bibernateorm.annotation.Id;
import com.bobocode.bibernateorm.annotation.Table;
import com.bobocode.bibernateorm.exception.AnnotationException;
import com.bobocode.bibernateorm.exception.ReflectionException;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

public class ReflectionUtils {
    private static final Logger LOG = LogManager.getLogger(ReflectionUtils.class);

    public static String getColumnName(@NonNull Field declaredField) {
        var column = declaredField.getAnnotation(Column.class);
        return nonNull(column) ? column.name() : declaredField.getName().toLowerCase();
    }

    public static <T> Map<String, Object> getColumnNameValues(@NonNull T entity) {
        return Stream.of(entity.getClass().getDeclaredFields()).collect(Collectors.toMap(ReflectionUtils::getColumnName, field -> getFieldValue(field, entity)));
    }


    public static <T> Object getFieldValue(@NonNull final Field field, @NonNull final T entity) {
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException e) {
            LOG.error("Error get value of the field = " + field.getName());
            throw new ReflectionException("Error get value of the field = " + field.getName(), e);
        }
    }

    public static <T> T initialEntity(@NonNull Class<T> type) {
        Constructor<?>[] declaredConstructors = type.getDeclaredConstructors();
        if (declaredConstructors.length != 1) {
            throw new IllegalArgumentException("Entity must have the one default constructor.");
        }
        try {
            return (T) declaredConstructors[0].newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            LOG.error("Error initial entity type = " + type.getSimpleName());
            throw new ReflectionException("Error initial entity type = " + type.getSimpleName(), ex);
        }
    }

    private static <T> Object getAnnotationValue(@NonNull Class<T> type, @NonNull Function<Class<T>, Object> annotation) {
        return annotation.apply(type);
    }

    public static <T> String getTableName(@NonNull Class<T> type) {
        return String.valueOf(getAnnotationValue(type, clazz -> {
            var table = clazz.getAnnotation(Table.class);
            return nonNull(table) ? table.name() : clazz.getName().toLowerCase();
        }));
    }

    public static <T> Object getIdValue(@NonNull T type) {
        return getAnnotationValue(type.getClass(), clazz -> {
            var ids = Arrays.stream(clazz.getDeclaredFields()).filter(field -> nonNull(field.getAnnotation(Id.class))).map(f -> getFieldValue(f, type)).toList();

            if (ids.isEmpty()) {
                LOG.error("Annotation \"Id\" not found in type" + type.getClass());
                throw new AnnotationException("Annotation \"Id\" not found in type" + type.getClass());
            }

            if (ids.size() != 1) {
                throw new AnnotationException("Annotation \"Id\" have be unique.");
            }
            return ids.get(0);

        });
    }

    public static <T> void setFieldValue(final Field field, final Object value, final T entity) {
        try {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            Object valueOf = value;
            if (Short.class.equals(fieldType)) valueOf = Short.valueOf(String.valueOf(value));
            if (Integer.class.equals(fieldType)) valueOf = Integer.valueOf(String.valueOf(value));
            if (Long.class.equals(fieldType)) valueOf = Long.valueOf(String.valueOf(value));
            if (Double.class.equals(fieldType)) valueOf = Double.valueOf(String.valueOf(value));
            if (Float.class.equals(fieldType)) valueOf = Float.parseFloat(String.valueOf(value));
            field.set(entity, valueOf);
        } catch (IllegalAccessException e) {
            LOG.error("Error setFieldValue to instance. Fail field name = " + field.getName());
            throw new ReflectionException("Error setField to instance. Fail field name = " + field.getName(), e);
        }
    }

}
