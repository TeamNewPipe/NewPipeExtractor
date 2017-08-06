package org.schabi.newpipe.extractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Info implements Serializable {

    public int service_id = -1;
    /**
     * Id of this Info object <br>
     * e.g. Youtube:  https://www.youtube.com/watch?v=RER5qCTzZ7     >    RER5qCTzZ7
     */
    public String id;
    public String url;
    public String name;

    public List<Throwable> errors = new ArrayList<>();
}
