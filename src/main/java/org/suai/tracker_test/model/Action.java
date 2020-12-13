package org.suai.tracker_test.model;
public enum Action {
    CREATE,
    DELETE,
    UPDATE,
    PICK_UP,
    MOVE_IN_PROGRESS,
    CLOSE;

    @Override
    public String toString() {
        return super.toString().replaceAll("_", " ");
    }
}
