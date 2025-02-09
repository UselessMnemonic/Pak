package com.uselessmnemonic.pak;

import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Bindings for pak-rs in JNI mode.
 */
public final class ZStreamRefImpl implements ZStreamRef {

    private static native long newStream();
    private static native void deleteStream(long handle);

    private static native int deflateInit(long streamRef, int level);
    private static native int deflateReset(long streamRef);
    private static native int deflateEnd(long streamRef);

    private static native int inflateInit(long streamRef);
    private static native int inflateReset(long streamRef);
    private static native int inflateEnd(long streamRef);

    private final long stream = newStream();
    private byte[] input = null;
    private byte[] output = null;

    private int availIn = 0;
    private int availOut = 0;
    private long totalIn = 0;
    private long totalOut = 0;
    private long adler = 1;

    private native void setInput(long handle, int offset, int length);

    public void setInput(byte[] input, int offset, int length) {
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
        this.availIn = length;
        this.availOut = length;
        setInput(stream, offset, length);
    }

    public void setInput(byte[] input) {
        setInput(input, 0, input.length);
    }

    private native void setOutput(long handle, int offset, int length);

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
        setOutput(stream, offset, length);
    }

    public void setOutput(byte[] output) {
        setOutput(output, 0, input.length);
    }

    @Override
    public int getAvailIn() {
        return availIn;
    }

    @Override
    public int getAvailOut() {
        return availOut;
    }

    public long getTotalIn() {
        return totalIn;
    }

    public long getTotalOut() {
        return totalOut;
    }

    public long getAdler() {
        return adler;
    }

    public native String getMsg();

    public int deflateInit(int level) {
        return deflateInit(stream, level);
    }

    public int deflateParams(int level, int strategy) {
        return PakRs.deflateParams(stream, input, output, level, strategy);
    }

    public int deflateGetDictionary(byte[] dictionary) {
        return PakRs.deflateGetDictionary(stream, dictionary);
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

    public int inflateGetDictionary(byte[] dictionary) {
        return PakRs.inflateGetDictionary(stream, dictionary);
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
        return PakRs.inflateSetDictionary(stream, dictionary, offset, length);
    }

    public int inflate(int flush) {
        return inflate(stream, input, output, flush);
    }

    public int inflateReset() {
        return inflateReset(stream);
    }

    public int inflateEnd() {
        return inflateEnd(stream);
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
