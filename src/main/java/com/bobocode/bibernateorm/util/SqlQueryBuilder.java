package com.bobocode.bibernateorm.util;

import com.bobocode.bibernateorm.annotation.GeneratedValue;
import com.bobocode.bibernateorm.annotation.GenerationType;
import com.bobocode.bibernateorm.util.reflection.FieldNameValue;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static com.bobocode.bibernateorm.util.reflection.ReflectionUtils.getTableName;


public class SqlQueryBuilder {

    public static <T> String selectEntityQuery(Class<T> type) {
        return String.format("SELECT * FROM %s WHERE id = ?", getTableName(type));
    }

    public static <T> String updateEntityQuery(T entity, List<FieldNameValue> columnNames) {
        var tableName = getTableName(entity.getClass());
        var stringBuilder = new StringBuilder();
        columnNames.stream().map(FieldNameValue::name).forEach(name -> stringBuilder.append(name).append("= ?").append(","));
        var arguments = stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return String.format("UPDATE %s SET %s WHERE id = ?", tableName, arguments);
    }

    public static <T> String saveEntityQuery(T entity, Set<String> columnNames) {
        var tableName = getTableName(entity.getClass());

        var names = new StringBuilder("(");
        columnNames.forEach(columnName -> names.append(columnName).append(","));
        names.deleteCharAt(names.length() - 1);
        names.append(")");

        var values = new StringBuilder("(");
        columnNames.forEach(columnName -> values.append("?").append(","));
        values.deleteCharAt(values.length() - 1);
        values.append(")");

        return String.format("INSERT INTO %s %s VALUES %s", tableName, names, values);

    }

    public static <T> String deleteEntityQuery(T entity) {
        var tableName = getTableName(entity.getClass());
        return String.format("DELETE FROM %s WHERE id = ?", tableName);
    }


}
