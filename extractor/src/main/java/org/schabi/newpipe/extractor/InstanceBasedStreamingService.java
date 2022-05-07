package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.instance.Instance;

public interface InstanceBasedStreamingService<I extends Instance> {
    I getInstance();

    void setInstance(I instance);
}
