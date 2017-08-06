package org.schabi.newpipe.extractor;

import java.util.List;

public abstract class ListInfo extends Info {
    public List<InfoItem> related_streams;
    public boolean has_more_streams;
    public String next_streams_url;
}
