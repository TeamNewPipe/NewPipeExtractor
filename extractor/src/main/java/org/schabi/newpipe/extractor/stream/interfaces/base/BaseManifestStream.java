package org.schabi.newpipe.extractor.stream.interfaces.base;

/**
 * Base interface for a manifest (generally a text file) describing multiple streams (whether
 * demuxed or not).
 *
 * <p>
 * When only one quality is known to be returned, it should be provided as a
 * {@link BaseMuxedStream} or a {@link BaseDemuxedStream} instead.
 * </p>
 *
 * <p>
 * It does not represent the base of manifest to be directly used by clients, but the one of
 * extractor sub-definitions and custom implementations. See
 * {@link org.schabi.newpipe.extractor.stream.interfaces.ManifestStream ManifestStream} for an
 * interface to be used directly.
 * </p>
 */
public interface BaseManifestStream extends BaseStream {
}
