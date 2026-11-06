package org.schabi.newpipe.extractor.stream.interfaces;

import org.schabi.newpipe.extractor.stream.interfaces.base.BaseManifestStream;

/**
 * Interface for a manifest (generally a text file) describing multiple streams (whether demuxed or
 * not).
 *
 * <p>
 * {@link org.schabi.newpipe.extractor.stream.interfaces.base.BaseMuxedStream BaseMuxedStream} or
 * {@link org.schabi.newpipe.extractor.stream.interfaces.base.BaseDemuxedStream BaseDemuxedStream}
 * provide single streams delivered using a manifest.
 * </p>
 *
 * <p>
 * See {@link BaseManifestStream} and {@link Stream} for available methods.
 * </p>
 */
public interface ManifestStream extends BaseManifestStream, Stream {
}
