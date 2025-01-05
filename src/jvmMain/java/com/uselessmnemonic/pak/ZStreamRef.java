package com.uselessmnemonic.pak;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;

class ZStreamRef implements AutoCloseable {
    private static final StructLayout z_streamLayout;

    static {
        z_streamLayout = ForeignHelpers.autoPad(
            ValueLayout.ADDRESS.withName("next_in"),
            ValueLayout.JAVA_INT.withName("avail_in"),
            ZLib.ULONG.withName("total_in"),

            ValueLayout.ADDRESS.withName("next_out"),
            ValueLayout.JAVA_INT.withName("avail_out"),
            ZLib.ULONG.withName("total_out"),

            ValueLayout.ADDRESS.withName("msg"),
            ValueLayout.ADDRESS.withName("state"),

            ValueLayout.ADDRESS.withName("zalloc"),
            ValueLayout.ADDRESS.withName("zfree"),
            ValueLayout.ADDRESS.withName("opaque"),

            ValueLayout.JAVA_INT.withName("data_type"),
            ZLib.ULONG.withName("adler"),
            ZLib.ULONG // reserved
        ).withName("z_stream");
    }

    private static final long total_inOffset = z_streamLayout.byteOffset(PathElement.groupElement("total_in"));
    private static final long total_outOffset = z_streamLayout.byteOffset(PathElement.groupElement("total_out"));

    private static final long msgOffset = z_streamLayout.byteOffset(PathElement.groupElement("msg"));
    private static final long adlerOffset = z_streamLayout.byteOffset(PathElement.groupElement("adler"));

    private final Arena arena = Arena.ofShared();
    private final MemorySegment z_stream = Arena.global().allocate(z_streamLayout);
    private MemorySegment input = MemorySegment.NULL;
    private MemorySegment output = MemorySegment.NULL;

    void setInput(MemorySegment input) {
        var byteSize = input.byteSize();
        if (byteSize > ZLib.UINT_MAX) {
            throw new IllegalArgumentException("Buffer is too large to process (%d bytes)".formatted(byteSize));
        }
        this.input = input;
    }

    void setOutput(MemorySegment output) {
        var byteSize = output.byteSize();
        if (byteSize > ZLib.UINT_MAX) {
            throw new IllegalArgumentException("Buffer has more capacity than is usable (%d bytes)".formatted(byteSize));
        }
        this.output = output;
    }

    MemorySegment getInput() {
        return input;
    }

    MemorySegment getOutput() {
        return output;
    }

    int getAvailIn() {
        var size = input.byteSize();
        return (int) size;
    }

    int getAvailOut() {
        var size = output.byteSize();
        return (int) size;
    }

    long getTotalIn() {
        if (ZLib.ULONG == ValueLayout.JAVA_INT) {
            return Integer.toUnsignedLong(z_stream.get(ValueLayout.JAVA_INT, total_inOffset));
        }
        return z_stream.get(ValueLayout.JAVA_LONG, total_inOffset);
    }

    long getTotalOut() {
        if (ZLib.ULONG == ValueLayout.JAVA_INT) {
            return Integer.toUnsignedLong(z_stream.get(ValueLayout.JAVA_INT, total_outOffset));
        }
        return z_stream.get(ValueLayout.JAVA_LONG, total_outOffset);
    }

    MemorySegment getMsg() throws Throwable {
        var segment = z_stream.get(ValueLayout.ADDRESS, msgOffset).asReadOnly();
        if (segment.address() == 0) {
            return MemorySegment.NULL;
        }
        var msgLen = ForeignHelpers.strlen(segment);
        return segment.reinterpret(msgLen + 1);
    }

    long getAdler() {
        if (ZLib.ULONG == ValueLayout.JAVA_INT) {
            return Integer.toUnsignedLong(z_stream.get(ValueLayout.JAVA_INT, adlerOffset));
        }
        return z_stream.get(ValueLayout.JAVA_LONG, adlerOffset);
    }

    int deflateInit(int level) throws Throwable {
        return ZLib.deflateInit(z_stream, level);
    }

    int deflateParams(int level, int strategy) throws Throwable {
        return PakExt.criticalDeflateParams(z_stream, input, output, (int) input.byteSize(), (int) output.byteSize(), level, strategy);
    }

    int deflateGetDictionary(MemorySegment dictionary, MemorySegment size) throws Throwable {
        var byteSize = dictionary.byteSize();
        if (byteSize > ZLib.UINT_MAX) {
            throw new IllegalArgumentException("Buffer has more capacity than is usable (%d bytes)".formatted(byteSize));
        }
        size = size.asSlice(0, 4);
        size.set(ValueLayout.JAVA_INT, 0, (int) byteSize);
        return ZLib.deflateGetDictionary(z_stream, dictionary, size);
    }

    int deflateSetDictionary(MemorySegment dictionary) throws Throwable {
        long byteSize = dictionary.byteSize();
        if (byteSize > ZLib.UINT_MAX) {
            throw new IllegalArgumentException("Buffer is too large to process (%d bytes)".formatted(byteSize));
        }
        return ZLib.deflateSetDictionary(z_stream, dictionary, (int) byteSize);
    }

    int deflate(int flush) throws Throwable {
        return PakExt.criticalDeflate(z_stream, input, output, (int) input.byteSize(), (int) output.byteSize(), flush);
    }

    int deflateReset() throws Throwable {
        return ZLib.deflateReset(z_stream);
    }

    int deflateEnd() throws Throwable {
        return ZLib.deflateEnd(z_stream);
    }

    int inflateInit() throws Throwable {
        return ZLib.inflateInit(z_stream);
    }

    int inflateGetDictionary(MemorySegment dictionary, MemorySegment size) throws Throwable {
        var byteSize = dictionary.byteSize();
        if (byteSize > ZLib.UINT_MAX) {
            throw new IllegalArgumentException("Buffer has more capacity than is usable (%d bytes)".formatted(byteSize));
        }
        size = size.asSlice(0, 4);
        size.set(ValueLayout.JAVA_INT, 0, (int) byteSize);
        return ZLib.inflateGetDictionary(z_stream, dictionary, size);
    }

    int inflateSetDictionary(MemorySegment dictionary) throws Throwable {
        long byteSize = dictionary.byteSize();
        if (byteSize > ZLib.UINT_MAX) {
            throw new IllegalArgumentException("Buffer is too large to process (%d bytes)".formatted(byteSize));
        }
        return ZLib.inflateSetDictionary(z_stream, dictionary, (int) byteSize);
    }

    int inflate(int flush) throws Throwable {
        return PakExt.criticalInflate(z_stream, input, output, (int) input.byteSize(), (int) output.byteSize(), flush);
    }

    int inflateReset() throws Throwable {
        return ZLib.inflateReset(z_stream);
    }

    int inflateEnd() throws Throwable {
        return ZLib.inflateEnd(z_stream);
    }

    @Override
    public void close() {
        try {
            inflateEnd();
            deflateEnd();
        } catch (Throwable _) {}
        arena.close();
    }
}
