package org.schabi.newpipe.extractor.streamdata.format;

public interface MediaFormat {

    int id();

    String name();

    String suffix();

    String mimeType();
}
