package com.uselessmnemonic.pak.jni;

/**
 * Bindings for pak-rs in JNI mode.
 */
public final class JNIZStreamRef implements AutoCloseable {

    private final long stream = PakRs.newStream();
    private byte[] input = null;
    private byte[] output = null;

    private long totalIn = 0;
    private long totalOut = 0;

    private long adler = 1;

    public void setInput(byte[] input) {
        this.input = input;
        if (input == null) return;
        PakRs.setInput(stream, 0, input.length);
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
        PakRs.setInput(stream, offset, length);
    }

    public void setOutput(byte[] output) {
        this.output = output;
        if (output == null) return;
        PakRs.setOutput(stream, 0, input.length);
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
        PakRs.setOutput(stream, offset, length);
    }

    public long getTotalIn() {
        return totalIn;
    }

    public long getTotalOut() {
        return totalOut;
    }

    public String getMsg() {
        return PakRs.getMsg(stream);
    }

    public long getAdler() {
        return adler;
    }

    public int deflateInit(int level) throws Throwable {
        return PakRs.deflateInit(stream, level);
    }

    public int deflateParams(int level, int strategy) {
        return PakRs.deflateParams(stream, input, output, level, strategy);
    }

    public int deflateGetDictionary(byte[] dictionary, int[] size) {
        return PakRs.deflateGetDictionary(stream, dictionary, size);
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
        return PakRs.deflateSetDictionary(stream, dictionary, offset, length);
    }

    public int deflate(int flush) {
        return PakRs.deflate(stream, input, output, flush);
    }

    public int deflateReset() {
        return PakRs.deflateReset(stream);
    }

    public int deflateEnd() {
        return PakRs.deflateEnd(stream);
    }

    public int inflateInit() {
        return PakRs.inflateInit(stream);
    }

    public int inflateGetDictionary(byte[] dictionary, int[] size) {
        return PakRs.deflateGetDictionary(stream, dictionary, size);
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
        return PakRs.deflateSetDictionary(stream, dictionary, offset, length);
    }

    public int inflate(int flush) {
        return PakRs.inflate(stream, input, output, flush);
    }

    public int inflateReset() {
        return PakRs.inflateReset(stream);
    }

    public int inflateEnd() {
        return PakRs.inflateEnd(stream);
    }

    @Override
    public void close() {
        try {
            inflateEnd();
            deflateEnd();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            PakRs.deleteStream(stream);
        }
    }
}
