package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import javax.annotation.Nonnull;
import java.io.IOException;

@SuppressWarnings("checkstyle:LeftCurly")
public interface RefreshableStream  {
    @Nonnull
    String fetchLatestUrl() throws IOException, ExtractionException;
    String initialUrl();

    String playlistId();
}
