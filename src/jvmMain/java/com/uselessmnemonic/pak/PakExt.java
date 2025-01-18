package com.uselessmnemonic.pak;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Bindings for pak-rs in FFM mode.
 */
final class PakRs {

    private PakRs() {}

    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup lookup;

    static {
        String osArch = System.getProperty("os.arch");
        String libArch;
        if (osArch.equals("amd64") || osArch.equals("x86_64")) {
            libArch = "x86_64";
        } else {
            String message = String.format("Unsupported architecture %s", osArch);
            throw new RuntimeException(message);
        }

        String osName = System.getProperty("os.name");
        String libName;
        String libExt;
        if (osName.startsWith("Windows")) {
            libName = "pak";
            libExt = ".dll";
        } else if (osName.startsWith("Mac") || osName.contains("Darwin")) {
            libName = "libpak";
            libExt = ".dylib";
        } else {
            libName = "libpak";
            libExt = ".so";
        }

        String jarPath = String.format("%s/%s%s", libArch, libName, libExt);
        URL libUrl = ZStreamRef.class.getClassLoader().getResource(jarPath);
        if (libUrl == null) {
            String message = String.format("Unsupported platform %s/%s", osName, osArch);
            throw new RuntimeException(message);
        }
        try (var libData = libUrl.openStream()) {
            var tmp = Files.createTempFile(libName, libExt);
            Files.copy(libData, tmp, StandardCopyOption.REPLACE_EXISTING);
            tmp.toFile().deleteOnExit();
            lookup = SymbolLookup.libraryLookup(tmp, Arena.ofShared());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    static final StructLayout streamLayout = ForeignHelpers.autoPad(
        ValueLayout.ADDRESS.withName("next_in"),
        ValueLayout.JAVA_INT.withName("avail_in"),
        ForeignHelpers.ULONG.withName("total_in"),

        ValueLayout.ADDRESS.withName("next_out"),
        ValueLayout.JAVA_INT.withName("avail_out"),
        ForeignHelpers.ULONG.withName("total_out"),

        ValueLayout.ADDRESS.withName("msg"),
        ValueLayout.ADDRESS.withName("state"),

        ValueLayout.ADDRESS.withName("zalloc"),
        ValueLayout.ADDRESS.withName("zfree"),
        ValueLayout.ADDRESS.withName("opaque"),

        ValueLayout.JAVA_INT.withName("data_type"),
        ForeignHelpers.ULONG.withName("adler"),
        ForeignHelpers.ULONG // reserved
    ).withName("z_stream");

    static final long totalInOffset = streamLayout.byteOffset(MemoryLayout.PathElement.groupElement("total_in"));
    static final long totalOutOffset = streamLayout.byteOffset(MemoryLayout.PathElement.groupElement("total_out"));
    static final long msgOffset = streamLayout.byteOffset(MemoryLayout.PathElement.groupElement("msg"));
    static final long adlerOffset = streamLayout.byteOffset(MemoryLayout.PathElement.groupElement("adler"));

    static final MethodHandle deflateInit = linker.downcallHandle(
        lookup.find("deflate_init").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT),
        Linker.Option.critical(false)
    );

    static final MethodHandle deflateParamsCritical = linker.downcallHandle(
        lookup.find("deflate_params_critical").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                                                    ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                                                    ValueLayout.JAVA_INT, ValueLayout.JAVA_INT),
        Linker.Option.critical(true)
    );

    static final MethodHandle deflateGetDictionaryCritical = linker.downcallHandle(
        lookup.find("deflate_get_dictionary_critical").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS),
        Linker.Option.critical(true)
    );

    static final MethodHandle deflateSetDictionaryCritical = linker.downcallHandle(
        lookup.find("deflate_set_dictionary_critical").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT),
        Linker.Option.critical(true)
    );

    static final MethodHandle deflateCritical = linker.downcallHandle(
        lookup.find("deflate_critical").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                              ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                              ValueLayout.JAVA_INT),
        Linker.Option.critical(true)
    );

    static final MethodHandle deflateReset = linker.downcallHandle(
        lookup.find("deflate_reset").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS),
        Linker.Option.critical(false)
    );

    static final MethodHandle deflateEnd = linker.downcallHandle(
        lookup.find("deflate_end").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS),
        Linker.Option.critical(false)
    );

    static final MethodHandle inflateInit = linker.downcallHandle(
        lookup.find("inflate_init").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS),
        Linker.Option.critical(false)
    );

    static final MethodHandle inflateGetDictionaryCritical = linker.downcallHandle(
        lookup.find("inflate_get_dictionary_critical").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS),
        Linker.Option.critical(true)
    );

    static final MethodHandle inflateSetDictionaryCritical = linker.downcallHandle(
        lookup.find("inflate_set_dictionary_critical").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT),
        Linker.Option.critical(true)
    );

    static final MethodHandle inflateCritical = linker.downcallHandle(
        lookup.find("inflate_critical").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                              ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                              ValueLayout.JAVA_INT),
        Linker.Option.critical(true)
    );

    static final MethodHandle inflateReset = linker.downcallHandle(
        lookup.find("inflate_reset").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS),
        Linker.Option.critical(false)
    );

    static final MethodHandle inflateEnd = linker.downcallHandle(
        lookup.find("inflate_end").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS),
        Linker.Option.critical(false)
    );
}
