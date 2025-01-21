package com.uselessmnemonic.pak;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * Bindings for pak-rs in FFM mode.
 */
public final class ZStreamRefImpl implements ZStreamRef {

    private final Arena arena = Arena.ofShared();
    private final MemorySegment stream = Arena.global().allocate(PakRs.streamLayout);
    private MemorySegment input = MemorySegment.NULL;
    private MemorySegment output = MemorySegment.NULL;

    @Override
    public void setInput(byte[] input) {
        if (input == null) {
            this.input = MemorySegment.NULL;
            return;
        }
        this.input = MemorySegment.ofArray(input);
    }

    @Override
    public void setInput(byte[] input, int offset, int length) {
        if (input == null || length == 0) {
            this.input = MemorySegment.NULL;
            return;
        }
        this.input = MemorySegment.ofArray(input).asSlice(offset, length);
    }

    public void setInput(MemorySegment input) {
        var byteSize = input.byteSize();
        if (byteSize > ForeignHelpers.UINT_MAX) {
            var message = String.format("Buffer is too large to process (%d bytes)", byteSize);
            throw new IllegalArgumentException(message);
        }
        this.input = input;
    }

    @Override
    public void setOutput(byte[] output) {
        if (output == null) {
            this.output = MemorySegment.NULL;
            return;
        }
        this.output = MemorySegment.ofArray(output);
    }

    @Override
    public void setOutput(byte[] output, int offset, int length) {
        if (output == null || length == 0) {
            this.output = MemorySegment.NULL;
            return;
        }
        this.output = MemorySegment.ofArray(output).asSlice(offset, length);
    }

    public void setOutput(MemorySegment output) {
        var byteSize = output.byteSize();
        if (byteSize > ForeignHelpers.UINT_MAX) {
            var message = String.format("Buffer has more capacity than is usable (%d bytes)", byteSize);
            throw new IllegalArgumentException(message);
        }
        this.output = output;
    }

    @Override
    public int getAvailIn() {
        return (int) input.byteSize();
    }

    @Override
    public int getAvailOut() {
        return (int) output.byteSize();
    }

    @Override
    public long getTotalIn() {
        if (ForeignHelpers.ULONG == ValueLayout.JAVA_INT) {
            return Integer.toUnsignedLong(stream.get(ValueLayout.JAVA_INT, PakRs.totalInOffset));
        }
        return stream.get(ValueLayout.JAVA_LONG, PakRs.totalInOffset);
    }

    @Override
    public long getTotalOut() {
        if (ForeignHelpers.ULONG == ValueLayout.JAVA_INT) {
            return Integer.toUnsignedLong(stream.get(ValueLayout.JAVA_INT, PakRs.totalOutOffset));
        }
        return stream.get(ValueLayout.JAVA_LONG, PakRs.totalOutOffset);
    }

    @Override
    public String getMsg() throws Throwable {
        var segment = stream.get(ValueLayout.ADDRESS, PakRs.msgOffset).asReadOnly();
        if (segment.address() == 0) {
            return null;
        }
        var msgLen = ForeignHelpers.strlen(segment);
        return segment.reinterpret(msgLen + 1).getString(0);
    }

    @Override
    public long getAdler() {
        if (ForeignHelpers.ULONG == ValueLayout.JAVA_INT) {
            return Integer.toUnsignedLong(stream.get(ValueLayout.JAVA_INT, PakRs.adlerOffset));
        }
        return stream.get(ValueLayout.JAVA_LONG, PakRs.adlerOffset);
    }

    @Override
    public int deflateInit(int level) throws Throwable {
        return (int) PakRs.deflateInit.invokeExact(stream, level);
    }

    @Override
    public int deflateParams(int level, int strategy) throws Throwable {
        return (int) PakRs.deflateParamsCritical.invokeExact(stream, input, output, (int) input.byteSize(), (int) output.byteSize(), level, strategy);
    }

    @Override
    public int deflateGetDictionary(byte[] dictionary) throws Throwable {
        MemorySegment dictionarySegment = MemorySegment.NULL;
        int dictionaryLength = 0;
        if (dictionary != null) {
            dictionarySegment = MemorySegment.ofArray(dictionary);
            dictionaryLength = dictionary.length;
        }
        return (int) PakRs.deflateGetDictionaryCritical.invokeExact(stream, dictionarySegment, dictionaryLength);
    }

    public int deflateGetDictionary(MemorySegment dictionary) throws Throwable {
        var byteSize = dictionary.byteSize();
        if (byteSize > ForeignHelpers.UINT_MAX) {
            var message = String.format("Buffer has more capacity than is usable (%d bytes)", byteSize);
            throw new IllegalArgumentException(message);
        }
        return (int) PakRs.deflateGetDictionaryCritical.invokeExact(stream, dictionary, (int) byteSize);
    }

    @Override
    public int deflateSetDictionary(byte[] dictionary, int offset, int length) throws Throwable {
        var dictionarySegment = MemorySegment.ofArray(dictionary).asSlice(offset, length);
        return (int) PakRs.deflateSetDictionaryCritical.invokeExact(stream, dictionarySegment, (long) length);
    }

    public int deflateSetDictionary(MemorySegment dictionary) throws Throwable {
        long byteSize = dictionary.byteSize();
        if (byteSize > ForeignHelpers.UINT_MAX) {
            var message = String.format("Buffer is too large to process (%d bytes)", byteSize);
            throw new IllegalArgumentException(message);
        }
        return (int) PakRs.deflateSetDictionaryCritical.invokeExact(stream, dictionary, (int) byteSize);
    }

    @Override
    public int deflate(int flush) throws Throwable {
        var prevTotalIn = getTotalIn();
        var prevTotalOut = getTotalOut();
        var result = (int) PakRs.deflateCritical.invokeExact(stream, input, output, (int) input.byteSize(), (int) output.byteSize(), flush);
        var totalRead = getTotalIn() - prevTotalIn;
        var totalWrite = getTotalOut() - prevTotalOut;
        input = input.asSlice(totalRead);
        output = output.asSlice(totalWrite);
        return result;
    }

    @Override
    public int deflateReset() throws Throwable {
        return (int) PakRs.deflateReset.invokeExact(stream);
    }

    @Override
    public int deflateEnd() throws Throwable {
        return (int) PakRs.deflateEnd.invokeExact(stream);
    }

    @Override
    public int inflateInit() throws Throwable {
        return (int) PakRs.inflateInit.invokeExact(stream);
    }

    @Override
    public int inflateGetDictionary(byte[] dictionary) throws Throwable {
        MemorySegment dictionarySegment = MemorySegment.NULL;
        int dictionaryLength = 0;
        if (dictionary != null) {
            dictionarySegment = MemorySegment.ofArray(dictionary);
            dictionaryLength = dictionary.length;
        }
        return (int) PakRs.inflateGetDictionaryCritical.invokeExact(stream, dictionarySegment, dictionaryLength);
    }

    public int inflateGetDictionary(MemorySegment dictionary) throws Throwable {
        var byteSize = dictionary.byteSize();
        if (byteSize > ForeignHelpers.UINT_MAX) {
            var message = String.format("Buffer has more capacity than is usable (%d bytes)", byteSize);
            throw new IllegalArgumentException(message);
        }
        return (int) PakRs.inflateGetDictionaryCritical.invokeExact(stream, dictionary, (int) byteSize);
    }

    @Override
    public int inflateSetDictionary(byte[] dictionary, int offset, int length) throws Throwable {
        var dictionarySegment = MemorySegment.ofArray(dictionary).asSlice(offset, length);
        return (int) PakRs.inflateSetDictionaryCritical.invokeExact(stream, dictionarySegment, (long) length);
    }

    public int inflateSetDictionary(MemorySegment dictionary) throws Throwable {
        long byteSize = dictionary.byteSize();
        if (byteSize > ForeignHelpers.UINT_MAX) {
            var message = String.format("Buffer is too large to process (%d bytes)", byteSize);
            throw new IllegalArgumentException(message);
        }
        return (int) PakRs.inflateSetDictionaryCritical.invokeExact(stream, dictionary, (int) byteSize);
    }

    @Override
    public int inflate(int flush) throws Throwable {
        var prevTotalIn = getTotalIn();
        var prevTotalOut = getTotalOut();
        var result = (int) PakRs.inflateCritical.invokeExact(stream, input, output, (int) input.byteSize(), (int) output.byteSize(), flush);
        var totalRead = getTotalIn() - prevTotalIn;
        var totalWrite = getTotalOut() - prevTotalOut;
        input = input.asSlice(totalRead);
        output = output.asSlice(totalWrite);
        return result;
    }

    @Override
    public int inflateReset() throws Throwable {
        return (int) PakRs.inflateReset.invokeExact(stream);
    }

    @Override
    public int inflateEnd() throws Throwable {
        return (int) PakRs.inflateEnd.invokeExact(stream);
    }

    @Override
    public void close() {
        try {
            inflateEnd();
            deflateEnd();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            arena.close();
        }
    }
}
