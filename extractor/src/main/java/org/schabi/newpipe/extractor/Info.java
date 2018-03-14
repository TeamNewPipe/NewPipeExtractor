package org.schabi.newpipe.extractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Info implements Serializable {

    private final int serviceId;
    /**
     * Id of this Info object <br>
     * e.g. Youtube:  https://www.youtube.com/watch?v=RER5qCTzZ7     &gt;    RER5qCTzZ7
     */
    private final String id;
    private final String url;
    private final String name;

    private final List<Throwable> errors = new ArrayList<>();

    public void addError(Throwable throwable) {
        this.errors.add(throwable);
    }

    public void addAllErrors(Collection<Throwable> errors) {
        this.errors.addAll(errors);
    }

    public Info(int serviceId, String id, String url, String name) {
        this.serviceId = serviceId;
        this.id = id;
        this.url = url;
        this.name = name;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[url=\"" + url + "\", name=\"" + name + "\"]";
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public List<Throwable> getErrors() {
        return errors;
    }
}
