package com.uselessmnemonic.pak;

import java.lang.foreign.*;

/**
 * Bindings for pakext in FFM mode.
 */
public final class ZStreamRef implements AutoCloseable {

    private final Arena arena = Arena.ofShared();
    private final MemorySegment stream = Arena.global().allocate(PakExt.streamLayout);
    private MemorySegment input = MemorySegment.NULL;
    private MemorySegment output = MemorySegment.NULL;

    public void setInput(byte[] input) {
        if (input == null) {
            this.input = MemorySegment.NULL;
            return;
        }
        this.input = MemorySegment.ofArray(input);
    }

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

    public void setOutput(byte[] output) {
        if (output == null) {
            this.output = MemorySegment.NULL;
            return;
        }
        this.output = MemorySegment.ofArray(output);
    }

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

    public long getTotalIn() {
        if (ForeignHelpers.ULONG == ValueLayout.JAVA_INT) {
            return Integer.toUnsignedLong(stream.get(ValueLayout.JAVA_INT, PakExt.totalInOffset));
        }
        return stream.get(ValueLayout.JAVA_LONG, PakExt.totalInOffset);
    }

    public long getTotalOut() {
        if (ForeignHelpers.ULONG == ValueLayout.JAVA_INT) {
            return Integer.toUnsignedLong(stream.get(ValueLayout.JAVA_INT, PakExt.totalOutOffset));
        }
        return stream.get(ValueLayout.JAVA_LONG, PakExt.totalOutOffset);
    }

    public String getMsg() throws Throwable {
        return getMsgSegment().getString(0);
    }

    public MemorySegment getMsgSegment() throws Throwable {
        var segment = stream.get(ValueLayout.ADDRESS, PakExt.msgOffset).asReadOnly();
        if (segment.address() == 0) {
            return MemorySegment.NULL;
        }
        var msgLen = ForeignHelpers.strlen(segment);
        return segment.reinterpret(msgLen + 1);
    }

    public long getAdler() {
        if (ForeignHelpers.ULONG == ValueLayout.JAVA_INT) {
            return Integer.toUnsignedLong(stream.get(ValueLayout.JAVA_INT, PakExt.adlerOffset));
        }
        return stream.get(ValueLayout.JAVA_LONG, PakExt.adlerOffset);
    }

    public int deflateInit(int level) throws Throwable {
        return (int) PakExt.deflateInit.invokeExact(stream, level);
    }

    public int deflateParams(int level, int strategy) throws Throwable {
        return (int) PakExt.deflateParamsCritical.invokeExact(stream, input, output, (int) input.byteSize(), (int) output.byteSize(), level, strategy);
    }

    public int deflateGetDictionary(byte[] dictionary, int[] size) throws Throwable {
        var sizeSegment = MemorySegment.ofArray(size);
        return (int) PakExt.deflateGetDictionaryCritical.invokeExact(stream, dictionary, sizeSegment);
    }

    public int deflateGetDictionary(MemorySegment dictionary, MemorySegment size) throws Throwable {
        var byteSize = dictionary.byteSize();
        if (byteSize > ForeignHelpers.UINT_MAX) {
            var message = String.format("Buffer has more capacity than is usable (%d bytes)", byteSize);
            throw new IllegalArgumentException(message);
        }
        size = size.asSlice(0, 4);
        return (int) PakExt.deflateGetDictionaryCritical.invokeExact(stream, dictionary, size);
    }

    public int deflateSetDictionary(byte[] dictionary, int offset, int length) throws Throwable {
        var dictionarySegment = MemorySegment.ofArray(dictionary).asSlice(offset, length);
        return (int) PakExt.deflateSetDictionaryCritical.invokeExact(stream, dictionarySegment, (long) length);
    }

    public int deflateSetDictionary(MemorySegment dictionary) throws Throwable {
        long byteSize = dictionary.byteSize();
        if (byteSize > ForeignHelpers.UINT_MAX) {
            var message = String.format("Buffer is too large to process (%d bytes)", byteSize);
            throw new IllegalArgumentException(message);
        }
        return (int) PakExt.deflateSetDictionaryCritical.invokeExact(stream, dictionary, (int) byteSize);
    }

    public int deflate(int flush) throws Throwable {
        var prevTotalIn = getTotalIn();
        var prevTotalOut = getTotalOut();
        var result = (int) PakExt.deflateCritical.invokeExact(stream, input, output, (int) input.byteSize(), (int) output.byteSize(), flush);
        var totalRead = getTotalIn() - prevTotalIn;
        var totalWrite = getTotalOut() - prevTotalOut;
        input = input.asSlice(totalRead);
        output = output.asSlice(totalWrite);
        return result;
    }

    public int deflateReset() throws Throwable {
        return (int) PakExt.deflateReset.invokeExact(stream);
    }

    public int deflateEnd() throws Throwable {
        return (int) PakExt.deflateEnd.invokeExact(stream);
    }

    public int inflateInit() throws Throwable {
        return (int) PakExt.inflateInit.invokeExact(stream);
    }

    public int inflateGetDictionary(byte[] dictionary, int[] size) throws Throwable {
        var sizeSegment = MemorySegment.ofArray(size);
        return (int) PakExt.inflateGetDictionaryCritical.invokeExact(stream, dictionary, sizeSegment);
    }

    public int inflateGetDictionary(MemorySegment dictionary, MemorySegment size) throws Throwable {
        var byteSize = dictionary.byteSize();
        if (byteSize > ForeignHelpers.UINT_MAX) {
            var message = String.format("Buffer has more capacity than is usable (%d bytes)", byteSize);
            throw new IllegalArgumentException(message);
        }
        size = size.asSlice(0, 4);
        size.set(ValueLayout.JAVA_INT, 0, (int) byteSize);
        return (int) PakExt.inflateGetDictionaryCritical.invokeExact(stream, dictionary, size);
    }

    public int inflateSetDictionary(byte[] dictionary, int offset, int length) throws Throwable {
        var dictionarySegment = MemorySegment.ofArray(dictionary).asSlice(offset, length);
        return (int) PakExt.inflateSetDictionaryCritical.invokeExact(stream, dictionarySegment, (long) length);
    }

    public int inflateSetDictionary(MemorySegment dictionary) throws Throwable {
        long byteSize = dictionary.byteSize();
        if (byteSize > ForeignHelpers.UINT_MAX) {
            var message = String.format("Buffer is too large to process (%d bytes)", byteSize);
            throw new IllegalArgumentException(message);
        }
        return (int) PakExt.inflateSetDictionaryCritical.invokeExact(stream, dictionary, (int) byteSize);
    }

    public int inflate(int flush) throws Throwable {
        var prevTotalIn = getTotalIn();
        var prevTotalOut = getTotalOut();
        var result = (int) PakExt.inflateCritical.invokeExact(stream, input, output, (int) input.byteSize(), (int) output.byteSize(), flush);
        var totalRead = getTotalIn() - prevTotalIn;
        var totalWrite = getTotalOut() - prevTotalOut;
        input = input.asSlice(totalRead);
        output = output.asSlice(totalWrite);
        return result;
    }

    public int inflateReset() throws Throwable {
        return (int) PakExt.inflateReset.invokeExact(stream);
    }

    public int inflateEnd() throws Throwable {
        return (int) PakExt.inflateEnd.invokeExact(stream);
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
