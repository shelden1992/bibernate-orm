package com.bobocode.bibernateorm;

import javax.sql.DataSource;

public class SessionFactory {
    private final DataSource dataSource;

    public SessionFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Session createSession() {
        return new Session(dataSource);
    }
}
