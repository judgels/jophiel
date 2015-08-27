package org.iatoki.judgels.jophiel;

public final class City {

    private final long id;
    private final String name;
    private final long referenceCount;

    public City(long id, String name, long referenceCount) {
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
