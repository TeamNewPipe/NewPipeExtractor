package org.schabi.newpipe.extractor.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ProtoBuilder {
    ByteArrayOutputStream byteBuffer;

    public ProtoBuilder() {
        this.byteBuffer = new ByteArrayOutputStream();
    }

    public byte[] toBytes() {
        return byteBuffer.toByteArray();
    }

    public String toUrlencodedBase64() {
        final String b64 = Base64.getUrlEncoder().encodeToString(toBytes());
        return URLEncoder.encode(b64, StandardCharsets.UTF_8);
    }

    private void writeVarint(final long val) {
        try {
            if (val == 0) {
                byteBuffer.write(new byte[]{(byte) 0});
            } else {
                long v = val;
                while (v != 0) {
                    byte b = (byte) (v & 0x7f);
                    v >>= 7;

                    if (v != 0) {
                        b |= (byte) 0x80;
                    }
                    byteBuffer.write(new byte[]{b});
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void field(final int field, final byte wire) {
        final long fbits = ((long) field) << 3;
        final long wbits = ((long) wire) & 0x07;
        final long val = fbits | wbits;
        writeVarint(val);
    }

    public void varint(final int field, final long val) {
        field(field, (byte) 0);
        writeVarint(val);
    }

    public void string(final int field, final String string) {
        final byte[] strBts = string.getBytes(StandardCharsets.UTF_8);
        bytes(field, strBts);
    }

    public void bytes(final int field, final byte[] bytes) {
        field(field, (byte) 2);
        writeVarint(bytes.length);
        try {
            byteBuffer.write(bytes);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
