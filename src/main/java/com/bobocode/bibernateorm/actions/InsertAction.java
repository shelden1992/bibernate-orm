package com.bobocode.bibernateorm.actions;

import com.bobocode.bibernateorm.KeyTypeId;
import com.bobocode.bibernateorm.Session;


public class InsertAction extends AbstractAction {

    public InsertAction(Object entity, Session session, KeyTypeId<?> keyTypeId) {
        super(entity, session, keyTypeId);
    }

    @Override
    void execute() {
        session.getEntityDao().saveEntity(entity);
    }
}
