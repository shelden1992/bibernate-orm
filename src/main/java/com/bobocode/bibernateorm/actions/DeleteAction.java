package com.bobocode.bibernateorm.actions;

import com.bobocode.bibernateorm.KeyTypeId;
import com.bobocode.bibernateorm.Session;


public class DeleteAction extends AbstractAction {

    public DeleteAction(Object entity, Session session, KeyTypeId<?> keyTypeId) {
        super(entity, session, keyTypeId);
    }


    void execute() {
        session.getEntityDao().deleteEntity(entity);
        session.getCache().remove(keyTypeId);
        session.removeSnapshot(keyTypeId);
    }
}
