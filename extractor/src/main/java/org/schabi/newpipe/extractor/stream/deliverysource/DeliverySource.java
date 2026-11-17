package org.schabi.newpipe.extractor.stream.deliverysource;

import java.io.Serializable;

/**
 * Interface representing how a stream can be accessed.
 *
 * <p>
 * This is a top level interface describing the notion of a delivery source, it does not require
 * any contract for implementations.
 * </p>
 *
 * <p>
 * Important: it doesn't mean in the case of an adaptive stream accessible with a manifest that its
 * segments are using this delivery source. For instance a manifest can be encoded in base 64 but
 * its segments URLs can be HTTP URLs.
 * </p>
 */
public interface DeliverySource extends Serializable {
}
