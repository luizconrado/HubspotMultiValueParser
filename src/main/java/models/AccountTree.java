package models;

import java.util.Objects;

public class AccountTree {

    private String id;
    private String parentId;
    private String rootId;
    private long group;
    private boolean processed;

    public AccountTree() {
        this.processed = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getRootId() {
        return rootId;
    }

    public void setRootId(String rootId) {
        this.rootId = rootId;
    }

    public long getGroup() {
        return group;
    }

    public void setGroup(long group) {
        this.group = group;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountTree that = (AccountTree) o;
        return group == that.group &&
                processed == that.processed &&
                Objects.equals(id, that.id) &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(rootId, that.rootId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentId, rootId, group, processed);
    }


    @Override
    public String toString() {
        return "AccountTree{" +
                "id='" + id + '\'' +
                ", parentId='" + parentId + '\'' +
                ", rootId='" + rootId + '\'' +
                ", group=" + group +
                ", processed=" + processed +
                '}';
    }
}
