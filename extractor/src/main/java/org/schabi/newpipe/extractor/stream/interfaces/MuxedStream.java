package org.schabi.newpipe.extractor.stream.interfaces;

import org.schabi.newpipe.extractor.stream.interfaces.base.BaseMuxedStream;

/**
 * A {@link Stream} aggregating multiple stream types.
 *
 * <p>
 * See {@link BaseMuxedStream} and {@link Stream} for available methods.
 * </p>
 */
public interface MuxedStream extends BaseMuxedStream, Stream {
}
