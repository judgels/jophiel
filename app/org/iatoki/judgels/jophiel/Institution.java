package org.iatoki.judgels.jophiel;

public final class Institution {

    private final long id;
    private final String name;
    private final long referenceCount;

    public Institution(long id, String name, long referenceCount) {
        this.id = id;
        this.name = name;
        this.referenceCount = referenceCount;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getReferenceCount() {
        return referenceCount;
    }
}
