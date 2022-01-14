package com.bobocode.bibernateorm.actions;

import com.bobocode.bibernateorm.KeyTypeId;
import com.bobocode.bibernateorm.Session;


public abstract class AbstractAction {
    protected final Object entity;
    protected final Session session;
    protected final KeyTypeId<?> keyTypeId;

    public AbstractAction(Object entity, Session session, KeyTypeId<?> keyTypeId) {
        this.entity = entity;
        this.session = session;
        this.keyTypeId = keyTypeId;
    }

    abstract void execute();

}
