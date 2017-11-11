package org.schabi.newpipe.extractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Info implements Serializable {

    public final int service_id;
    /**
     * Id of this Info object <br>
     * e.g. Youtube:  https://www.youtube.com/watch?v=RER5qCTzZ7     >    RER5qCTzZ7
     */
    public final String id;
    public final String url;
    public final String name;

    public List<Throwable> errors = new ArrayList<>();

    public void addError(Throwable throwable) {
        this.errors.add(throwable);
    }

    public void addAllErrors(Collection<Throwable> errors) {
        this.errors.addAll(errors);
    }

    public Info(int serviceId, String id, String url, String name) {
        this.service_id = serviceId;
        this.id = id;
        this.url = url;
        this.name = name;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "[url=\"" + url + "\", name=\"" + name + "\"]";
    }

    public int getServiceId() {
        return service_id;
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
