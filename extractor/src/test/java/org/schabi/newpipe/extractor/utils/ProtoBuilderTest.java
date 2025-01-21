package org.schabi.newpipe.extractor.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProtoBuilderTest {
    @Test
    public void testProtoBuilder() {
        final ProtoBuilder pb = new ProtoBuilder();
        pb.varint(1, 128);
        pb.varint(2, 1234567890);
        pb.varint(3, 1234567890123456789L);
        pb.string(4, "Hello");
        pb.bytes(5, new byte[]{1, 2, 3});
        assertEquals("CIABENKF2MwEGJWCpu_HnoSRESIFSGVsbG8qAwECAw%3D%3D", pb.toUrlencodedBase64());
    }
}
