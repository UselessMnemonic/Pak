package com.uselessmnemonic.pak;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class PakRs {

    private PakRs() {}

    /* Management functions */

    static native long newStream();
    static native void deleteStream(long streamRef);
    static native void setInput(long streamRef, int nextIn, int availIn);
    static native void setOutput(long streamRef, int nextOut, int availOut);
    static native String getMsg(long streamRef);

    /* Deflate functions */

    static native int deflateInit(long streamRef, int level);
    static native int deflateParams(long streamRef, byte[] input, byte[] output, int level, int strategy);
    static native int deflateGetDictionary(long streamRef, byte[] dictionary);
    static native int deflateSetDictionary(long streamRef, byte[] dictionary, int offset, int length);
    static native int deflate(long streamRef, byte[] input, byte[] output, int flush);
    static native int deflateReset(long streamRef);
    static native int deflateEnd(long streamRef);

    /* Inflate functions */

    static native int inflateInit(long streamRef);
    static native int inflateGetDictionary(long streamRef, byte[] dictionary);
    static native int inflateSetDictionary(long streamRef, byte[] dictionary, int offset, int length);
    static native int inflate(long streamRef, byte[] input, byte[] output, int flush);
    static native int inflateReset(long streamRef);
    static native int inflateEnd(long streamRef);
}
