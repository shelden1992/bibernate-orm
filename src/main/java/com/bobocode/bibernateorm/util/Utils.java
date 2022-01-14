package com.bobocode.bibernateorm.util;

import com.bobocode.bibernateorm.KeyTypeId;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static com.bobocode.bibernateorm.util.reflection.ReflectionUtils.getFieldValue;
import static java.util.Comparator.comparing;


public class Utils {

    public static boolean hasChanged(Map.Entry<KeyTypeId<?>, Object> cacheKeyEntities, Map<KeyTypeId<?>, Object[]> snapshotCopy) {
        var changeValue = cacheKeyEntities.getValue();

        var snapshotCopyValues = snapshotCopy.get(cacheKeyEntities.getKey());

        var declaredFields = Arrays.stream(cacheKeyEntities.getKey().type().getDeclaredFields()).sorted(comparing(Field::getName)).toArray(Field[]::new);

        for (int i = 0; i < declaredFields.length; i++) {
            var field = declaredFields[i];
            field.setAccessible(true);
            if (!Objects.equals(snapshotCopyValues[i], getFieldValue(field, changeValue))) return true;
        }

        return false;

    }

}
