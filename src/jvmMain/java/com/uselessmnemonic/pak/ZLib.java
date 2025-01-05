package com.uselessmnemonic.pak;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

/**
 * Bindings for zlib.
 */
final class ZLib {
    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup zLookup = ForeignHelpers.libraryLookupOneOf(
        Arena.global(),
        System.getenv().getOrDefault("ZLIB_LIBRARY", "libz"), "libz.so", "libz.dylib", "libz.dll", "ZLIB1.DLL"
    );

    static final ValueLayout ULONG;
    static final long ULONG_MAX;
    static final long UINT_MAX = 0x00000000FFFFFFFFL;

    static {
        var nativeLong = linker.canonicalLayouts().get("long");
        var nativeLongSize = nativeLong.byteSize();
        if (nativeLongSize == 4) {
            ULONG = ValueLayout.JAVA_INT;
            ULONG_MAX = 0x00000000FFFFFFFFL;
        } else if (nativeLongSize == 8) {
            ULONG = ValueLayout.JAVA_LONG;
            ULONG_MAX = 0xFFFFFFFFFFFFFFFFL;
        } else {
            var msg = "ZLib cannot be used on this platform because 'long' is %d bytes".formatted(nativeLongSize);
            throw new RuntimeException(msg);
        }
    }

    static final MethodHandle deflateInitHandle = linker.downcallHandle(
        zLookup.find("deflateInit_").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
    );

    static final MemorySegment deflateParamsSegment = zLookup.find("deflateParams").orElseThrow();
    static final MethodHandle deflateParamsHandle = linker.downcallHandle(
        deflateParamsSegment,
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
    );

    static final MethodHandle deflateGetDictionaryHandle = linker.downcallHandle(
        zLookup.find("deflateGetDictionary").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS),
        Linker.Option.critical(true)
    );

    static final MethodHandle deflateSetDictionaryHandle = linker.downcallHandle(
        zLookup.find("deflateSetDictionary").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT),
        Linker.Option.critical(true)
    );

    static final MemorySegment deflateSegment = zLookup.find("deflate").orElseThrow();
    static final MethodHandle deflateHandle = linker.downcallHandle(
        deflateSegment,
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT),
        Linker.Option.critical(true)
    );

    static final MethodHandle deflateResetHandle = linker.downcallHandle(
        zLookup.find("deflateReset").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );

    static final MethodHandle deflateEndHandle = linker.downcallHandle(
        zLookup.find("deflateEnd").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );

    static final MethodHandle inflateInitHandle = linker.downcallHandle(
        zLookup.find("inflateInit_").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
    );

    static final MethodHandle inflateGetDictionaryHandle = linker.downcallHandle(
        zLookup.find("inflateGetDictionary").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS),
        Linker.Option.critical(true)
    );

    static final MethodHandle inflateSetDictionaryHandle = linker.downcallHandle(
        zLookup.find("inflateSetDictionary").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT),
        Linker.Option.critical(true)
    );

    static final MemorySegment inflateSegment = zLookup.find("inflate").orElseThrow();
    static final MethodHandle inflateHandle = linker.downcallHandle(
        inflateSegment,
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT),
        Linker.Option.critical(true)
    );

    static final MethodHandle inflateResetHandle = linker.downcallHandle(
        zLookup.find("inflateReset").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );

    static final MethodHandle inflateEndHandle = linker.downcallHandle(
        zLookup.find("inflateEnd").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );

    static final MemorySegment zlibVersion;

    static {
        MemorySegment versionSegment;
        try {
            versionSegment = (MemorySegment) linker.downcallHandle(
                zLookup.find("zlibVersion").orElseThrow(), FunctionDescriptor.of(ValueLayout.ADDRESS)
            ).invokeExact();
            versionSegment = versionSegment.asReadOnly();
            var strLen = ForeignHelpers.strlen(versionSegment);
            zlibVersion = versionSegment.reinterpret(strLen + 1);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static final String version = zlibVersion.getString(0);

    public static int deflateInit(MemorySegment stream, int level) throws Throwable {
        return (int) deflateInitHandle.invokeExact(stream, level, zlibVersion, (int) stream.byteSize());
    }

    public static int deflateParams(MemorySegment stream, int level, int strategy) throws Throwable {
        return (int) deflateParamsHandle.invokeExact(stream, level, strategy);
    }

    public static int deflateGetDictionary(MemorySegment stream, MemorySegment dictionary, MemorySegment size) throws Throwable {
        return (int) deflateGetDictionaryHandle.invokeExact(stream, dictionary, size);
    }

    public static int deflateSetDictionary(MemorySegment stream, MemorySegment dictionary, int size) throws Throwable {
        return (int) deflateSetDictionaryHandle.invokeExact(stream, dictionary, size);
    }

    public static int deflate(MemorySegment stream, int flush) throws Throwable {
        return (int) deflateHandle.invokeExact(stream, flush);
    }

    public static int deflateReset(MemorySegment stream) throws Throwable {
        return (int) deflateResetHandle.invokeExact(stream);
    }

    public static int deflateEnd(MemorySegment stream) throws Throwable {
        return (int) deflateEndHandle.invokeExact(stream);
    }

    public static int inflateInit(MemorySegment stream) throws Throwable {
        return (int) inflateInitHandle.invokeExact(stream, zlibVersion, (int) stream.byteSize());
    }

    public static int inflateGetDictionary(MemorySegment stream, MemorySegment dictionary, MemorySegment size) throws Throwable {
        return (int) inflateGetDictionaryHandle.invokeExact(stream, dictionary, size);
    }

    public static int inflateSetDictionary(MemorySegment stream, MemorySegment dictionary, int size) throws Throwable {
        return (int) inflateSetDictionaryHandle.invokeExact(stream, dictionary, size);
    }

    public static int inflate(MemorySegment stream, int flush) throws Throwable {
        return (int) inflateHandle.invokeExact(stream, flush);
    }

    public static int inflateReset(MemorySegment stream) throws Throwable {
        return (int) inflateResetHandle.invokeExact(stream);
    }

    public static int inflateEnd(MemorySegment stream) throws Throwable {
        return (int) inflateEndHandle.invokeExact(stream);
    }

    private ZLib() {}
}
