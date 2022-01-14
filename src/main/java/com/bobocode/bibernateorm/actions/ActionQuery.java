package com.bobocode.bibernateorm.actions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Setter(value = AccessLevel.PRIVATE)
@Getter
public class ActionQuery {
    private final List<DeleteAction> deleteActions = new ArrayList<>();
    private final List<InsertAction> insertActions = new ArrayList<>();
    private final List<UpdateAction> updateActions = new ArrayList<>();

    public ActionQuery() {

    }

    public void execute() {
        insertActions.forEach(AbstractAction::execute);
        updateActions.forEach(AbstractAction::execute);
        deleteActions.forEach(AbstractAction::execute);
        clear();
    }

    public void clear() {
        insertActions.clear();
        updateActions.clear();
        deleteActions.clear();
    }

    public void addDeleteAction(DeleteAction deleteAction) {
        deleteActions.add(deleteAction);
    }

    public void addUpdateAction(UpdateAction updateAction) {
        updateActions.add(updateAction);
    }

    public void addInsertAction(InsertAction insertAction) {
        insertActions.add(insertAction);
    }
}
