package com.uselessmnemonic.pak.jni;

import dalvik.annotation.optimization.CriticalNative;
import dalvik.annotation.optimization.FastNative;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class PakExt {

    private PakExt() {}

    /* Management functions */

    static native long newStream();
    static native void deleteStream(long streamRef);
    static native void setInput(long streamRef, int nextIn, int availIn);
    static native void setOutput(long streamRef, int nextOut, int availOut);
    static native String getMsg(long streamRef);

    /* Deflate functions */

    static native int deflateInit(long streamRef, int level);
    static native int deflateParams(long streamRef, byte[] input, byte[] output, int level, int strategy);
    static native int deflateGetDictionary(long streamRef, byte[] dictionary, int[] size);
    static native int deflateSetDictionary(long streamRef, byte[] dictionary, int offset, int length);
    static native int deflate(long streamRef, byte[] input, byte[] output, int flush);
    static native int deflateReset(long streamRef);
    static native int deflateEnd(long streamRef);

    /* Inflate functions */

    static native int inflateInit(long streamRef);
    static native int inflateGetDictionary(long streamRef, byte[] dictionary, int[] size);
    static native int inflateSetDictionary(long streamRef, byte[] dictionary, int offset, int length);
    static native int inflate(long streamRef, byte[] input, byte[] output, int flush);
    static native int inflateReset(long streamRef);
    static native int inflateEnd(long streamRef);

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
            libName = "pakext";
            libExt = ".dll";
        } else if (osName.startsWith("Mac") || osName.contains("Darwin")) {
            libName = "libpakext";
            libExt = ".dylib";
        } else {
            libName = "libpakext";
            libExt = ".so";
        }

        String jarPath = String.format("%s/%s%s", libArch, libName, libExt);
        URL libUrl = JNIZStreamRef.class.getClassLoader().getResource(jarPath);
        if (libUrl == null) {
            String message = String.format("Unsupported platform %s/%s", osName, osArch);
            throw new RuntimeException(message);
        }

        try (InputStream libData = libUrl.openStream()) {
            Path tmp = Files.createTempFile(libName, libExt);
            Files.copy(libData, tmp, StandardCopyOption.REPLACE_EXISTING);
            tmp.toFile().deleteOnExit();
            System.load(tmp.toString());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
