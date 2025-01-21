package com.uselessmnemonic.pak;

/**
 * Base API for Java-based ZStream implementations.
 */
public interface ZStreamRef extends AutoCloseable {
    void setInput(byte[] input);
    void setInput(byte[] input, int offset, int length);
    void setOutput(byte[] output);
    void setOutput(byte[] output, int offset, int length);
    int getAvailIn();
    int getAvailOut();
    long getTotalIn();
    long getTotalOut();
    long getAdler();
    String getMsg() throws Throwable;

    int deflateInit(int level) throws Throwable;
    int deflateParams(int level, int strategy) throws Throwable;
    int deflateGetDictionary(byte[] dictionary) throws Throwable;
    int deflateSetDictionary(byte[] dictionary, int offset, int length) throws Throwable;
    int deflate(int flush) throws Throwable;
    int deflateReset() throws Throwable;
    int deflateEnd() throws Throwable;

    int inflateInit() throws Throwable;
    int inflateGetDictionary(byte[] dictionary) throws Throwable;
    int inflateSetDictionary(byte[] dictionary, int offset, int length) throws Throwable;
    int inflate(int flush) throws Throwable;
    int inflateReset() throws Throwable;
    int inflateEnd() throws Throwable;
}
