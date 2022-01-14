package com.bobocode.bibernateorm;

public record KeyTypeId<T>(Class<T> type, Object id) {

    public Class<T> getType() {
        return type;
    }


    public Object getId() {
        return id;
    }
}
