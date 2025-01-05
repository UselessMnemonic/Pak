package com.uselessmnemonic.pak;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Bindings for pakext in FFM mode.
 */
final class PakExt {
    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup zExtLookup;

    static {
        var osArch = System.getProperty("os.arch");
        String libArch;
        if (osArch.equals("amd64") || osArch.equals("x86_64")) {
            libArch = "x86_64";
        } else {
            throw new RuntimeException("Unsupported architecture %s".formatted(osArch));
        }

        var osName = System.getProperty("os.name");
        String libName;
        String libExt;
        if (osName.startsWith("Windows")) {
            libName = "pakext";
            libExt = ".dll";
        } else if (osName.startsWith("Mac") || osName.contains("Darwin")) {
            libName = "libpakext";
            libExt = ".dylib";
        } else {
            libName = "libpakext";
            libExt = ".so";
        }
        var libUrl = PakExt.class.getClassLoader().getResource(
            "%s/%s%s".formatted(libArch, libName, libExt)
        );
        if (libUrl == null) {
            throw new RuntimeException("Unsupported platform %s/%s".formatted(osName, osArch));
        }
        try (var libData = libUrl.openStream()) {
            var tmp = Files.createTempFile(libName, libExt);
            Files.copy(libData, tmp, StandardCopyOption.REPLACE_EXISTING);
            tmp.toFile().deleteOnExit();
            zExtLookup = SymbolLookup.libraryLookup(tmp, Arena.ofShared());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    static final MethodHandle criticalActionHandle = linker.downcallHandle(
        zExtLookup.find("criticalAction").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
            ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT
        ),
        Linker.Option.critical(true)
    );

    static final MethodHandle criticalDeflateParamsHandle = linker.downcallHandle(
        zExtLookup.find("criticalDeflateParams").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
            ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT
        ),
        Linker.Option.critical(true)
    );

    public static int criticalInflate(MemorySegment stream, MemorySegment input, MemorySegment output,
                                      int inputSize, int outputSize, int flush) throws Throwable {
        return (int) criticalActionHandle.invokeExact(ZLib.inflateSegment, stream, input, output, inputSize, outputSize, flush);
    }

    public static int criticalDeflate(MemorySegment stream, MemorySegment input, MemorySegment output,
                                      int inputSize, int outputSize, int flush) throws Throwable {
        return (int) criticalActionHandle.invokeExact(ZLib.deflateSegment, stream, input, output, inputSize, outputSize, flush);
    }

    public static int criticalDeflateParams(MemorySegment stream, MemorySegment input, MemorySegment output,
                                            int inputSize, int outputSize, int level, int strategy) throws Throwable {
        return (int) criticalDeflateParamsHandle.invokeExact(ZLib.deflateParamsSegment, stream, input, output, inputSize, outputSize, level, strategy);
    }

    private PakExt() {}
}
