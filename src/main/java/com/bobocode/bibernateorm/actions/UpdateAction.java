package com.bobocode.bibernateorm.actions;

import com.bobocode.bibernateorm.KeyTypeId;
import com.bobocode.bibernateorm.Session;


public class UpdateAction extends AbstractAction {
    public UpdateAction(Object entity, Session session, KeyTypeId<?> keyTypeId) {
        super(entity, session, keyTypeId);
    }

    @Override
    void execute() {
        session.getEntityDao().updateEntity(entity, keyTypeId.getId());
        session.getCache().put(keyTypeId, entity);
        session.addSnapshot(keyTypeId, entity);
    }
}
