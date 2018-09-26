package org.schabi.newpipe.extractor.utils;

import android.os.Build;

import java.io.EOFException;
import java.io.IOException;

import org.schabi.newpipe.extractor.utils.io.SharpStream;

/**
 * @author kapodamy
 */
public class DataReader {

    public final SharpStream stream;
    private long pos;
    private final boolean rewind;

    public DataReader(SharpStream stream) {
        this.rewind = stream.canRewind();
        this.stream = stream;
        this.pos = 0L;
    }

    public long position() {
        return pos;
    }

    public final int readInt() throws IOException {
        primitiveRead(IntegerSize);
        return primitive[0] << 24 | primitive[1] << 16 | primitive[2] << 8 | primitive[3];
    }

    public final int read() throws IOException {
        int value = stream.read();
        if (value == -1) {
            throw new EOFException();
        }

        pos++;
        return value;
    }

    public final long skipBytes(long amount) throws IOException {
        amount = stream.skip(amount);
        pos += amount;
        return amount;
    }

    public final long readLong() throws IOException {
        primitiveRead(LongSize);
        long high = primitive[0] << 24 | primitive[1] << 16 | primitive[2] << 8 | primitive[3];
        long low = primitive[4] << 24 | primitive[5] << 16 | primitive[6] << 8 | primitive[7];
        return high << 32 | low;
    }

    public final short readShort() throws IOException {
        primitiveRead(ShortSize);
        return (short) (primitive[0] << 8 | primitive[1]);
    }

    public final int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    public final int read(byte[] buffer, int offset, int count) throws IOException {
        int res = stream.read(buffer, offset, count);
        pos += res;

        return res;
    }

    public final boolean available() {
        return stream.available() > 0;
    }

    public void rewind() throws IOException {
        stream.rewind();
        pos = 0;
    }

    public boolean canRewind() {
        return rewind;
    }

    private short[] primitive = new short[LongSize];

    private void primitiveRead(int amount) throws IOException {
        byte[] buffer = new byte[amount];
        int read = stream.read(buffer, 0, amount);
        pos += read;
        if (read != amount) {
            throw new EOFException("Truncated data, missing " + String.valueOf(amount - read) + " bytes");
        }

        for (int i = 0; i < buffer.length; i++) {
            primitive[i] = (short) (buffer[i] & 0xFF);// the "byte" datatype is signed and is very annoying
        }
    }

    public final static int ShortSize = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Short.BYTES : 2;
    public final static int LongSize = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Long.BYTES : 8;
    public final static int IntegerSize = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Integer.BYTES : 4;
    public final static int FloatSize = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Float.BYTES : 4;
}
