package com.uselessmnemonic.pak.jni;

/**
 * Bindings for pakext in JNI mode.
 */
public final class JNIZStreamRef implements AutoCloseable {

    private final long stream = PakExt.newStream();
    private byte[] input = null;
    private byte[] output = null;

    private long totalIn = 0;
    private long totalOut = 0;

    private long adler = 1;

    public void setInput(byte[] input) {
        this.input = input;
        if (input == null) return;
        PakExt.setInput(stream, 0, input.length);
    }

    public void setInput(byte[] input, int offset, int length) {
        if (input == null) {
            this.input = null;
            return;
        }
        if (offset < 0) {
            String message = String.format("Offset cannot be negative, was %d", offset);
            throw new IllegalArgumentException(message);
        }
        if (length < 0) {
            String message = String.format("Length cannot be negative, was %d", offset);
            throw new IllegalArgumentException(message);
        }
        if (offset + length > input.length) {
            throw new IllegalArgumentException("Offset and length exceed array bounds");
        }
        this.input = input;
        PakExt.setInput(stream, offset, length);
    }

    public void setOutput(byte[] output) {
        this.output = output;
        if (output == null) return;
        PakExt.setOutput(stream, 0, input.length);
    }

    public void setOutput(byte[] output, int offset, int length) {
        if (output == null) {
            this.output = null;
            return;
        }
        if (offset < 0) {
            String message = String.format("Offset cannot be negative, was %d", offset);
            throw new IllegalArgumentException(message);
        }
        if (length < 0) {
            String message = String.format("Length cannot be negative, was %d", offset);
            throw new IllegalArgumentException(message);
        }
        if (offset + length > output.length) {
            throw new IllegalArgumentException("Offset and length exceed array bounds");
        }
        this.output = output;
        PakExt.setOutput(stream, offset, length);
    }

    public long getTotalIn() {
        return totalIn;
    }

    public long getTotalOut() {
        return totalOut;
    }

    public String getMsg() {
        return PakExt.getMsg(stream);
    }

    public long getAdler() {
        return adler;
    }

    public int deflateInit(int level) throws Throwable {
        return PakExt.deflateInit(stream, level);
    }

    public int deflateParams(int level, int strategy) {
        return PakExt.deflateParams(stream, input, output, level, strategy);
    }

    public int deflateGetDictionary(byte[] dictionary, int[] size) {
        return PakExt.deflateGetDictionary(stream, dictionary, size);
    }

    public int deflateSetDictionary(byte[] dictionary, int offset, int length) {
        if (offset < 0) {
            String message = String.format("Offset cannot be negative, was %d", offset);
            throw new IllegalArgumentException(message);
        }
        if (length < 0) {
            String message = String.format("Length cannot be negative, was %d", offset);
            throw new IllegalArgumentException(message);
        }
        if (offset + length > dictionary.length) {
            throw new IllegalArgumentException("Offset and length exceed array bounds");
        }
        return PakExt.deflateSetDictionary(stream, dictionary, offset, length);
    }

    public int deflate(int flush) {
        return PakExt.deflate(stream, input, output, flush);
    }

    public int deflateReset() {
        return PakExt.deflateReset(stream);
    }

    public int deflateEnd() {
        return PakExt.deflateEnd(stream);
    }

    public int inflateInit() {
        return PakExt.inflateInit(stream);
    }

    public int inflateGetDictionary(byte[] dictionary, int[] size) {
        return PakExt.deflateGetDictionary(stream, dictionary, size);
    }

    public int inflateSetDictionary(byte[] dictionary, int offset, int length) {
        if (offset < 0) {
            String message = String.format("Offset cannot be negative, was %d", offset);
            throw new IllegalArgumentException(message);
        }
        if (length < 0) {
            String message = String.format("Length cannot be negative, was %d", offset);
            throw new IllegalArgumentException(message);
        }
        if (offset + length > dictionary.length) {
            throw new IllegalArgumentException("Offset and length exceed array bounds");
        }
        return PakExt.deflateSetDictionary(stream, dictionary, offset, length);
    }

    public int inflate(int flush) {
        return PakExt.inflate(stream, input, output, flush);
    }

    public int inflateReset() {
        return PakExt.inflateReset(stream);
    }

    public int inflateEnd() {
        return PakExt.inflateEnd(stream);
    }

    @Override
    public void close() {
        try {
            inflateEnd();
            deflateEnd();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            PakExt.deleteStream(stream);
        }
    }
}
