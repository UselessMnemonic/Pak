package com.uselessmnemonic.pak

/**
 * The flushing strategies.
 *
 * If explicitly flushing, `deflate` will return [ZResult.Ok] only when enough output space is available to complete
 * the flush. If insufficient output space is available, then `deflate` will return [ZResult.BufferError] and the same
 * flushing operation must be called again until [ZResult.Ok]. A program will be wise to proactively offer enough output
 * for each operation--at least six bytes especially during [FullFlush] or [SyncFlush].
 */
enum class ZFlush(val value: Int) {
    /**
     * Do not explicitly flush. This allows a decompressor to decide when to produce output and ideally maximize
     * compression.
     */
    NoFlush(0),

    /**
     * Available only for compressors. Causes all pending output to be flushed to the output buffer, but the output
     * stream may not necessarily become aligned to a byte boundary. This completes the current deflate block and
     * follows it with an empty fixed-codes block which is three header bits plus the seven-bit end-of-block code.
     * A second empty block may be emitted under certain conditions.
     */
    PartialFlush(1),

    /**
     * If used for a compressor, SyncFlush causes all pending output to be flushed to the output buffer such that the
     * output stream is aligned at a byte boundary. After the actual output, an empty stored block is emitted which is
     * three header bits plus up to seven 0 bits (to achieve byte alignment), and finally the sequence
     * `0x00 0x00 0xFF 0xFF`.
     *
     * If used for a decompressor, SyncFlush causes as much output to be generated as possible.
     */
    SyncFlush(2),

    /**
     * Available only for compressors. Behaves exactly as [SyncFlush] while also clearing the compressor's symbol
     * dictionary. This is particularly useful for damage recovery, as the output stream becomes byte-aligned and the
     * reciprocal decompressor may continue to process data with no knowledge of past data.
     */
    FullFlush(3),

    /**
     * Causes a compressor or decompressor to process its remaining input and end the stream. After the stream finishes,
     * the only valid operation is to reset or end it.
     */
    Finish(4),

    /**
     * If used for a compressor, Block causes the current block to be completed and flushed to the output buffer. The
     * output stream may not necessarily be aligned at a byte boundary, and up to seven bits of the current block may be
     * withheld to be emitted as the first byte of the following block. This is for advanced applications that need to
     * control the emission of deflate blocks.
     *
     * If used for a decompressor, Block causes `inflate` to return just before processing the next block. The first
     * call at the beginning of a deflated stream will return just after the compression header, but for a raw deflated
     * stream the first block is always processed.
     */
    Block(5),

    /**
     * Available only for decompressors. Trees behaves similarly to [Block], but causes `inflate` to return just after
     * the compression header of each block.
     */
    Trees(6)
}

/**
 * Expected result codes reported by zlib.
 */
enum class ZResult(val value: Int) {
    /**
     * Indicates no progress could be made for a call to `deflate`/`inflate`. Either no more input is available, or not
     * enough output buffer was available to accommodate some quantum of data for the call.
     */
    BufferError(-5),

    /**
     * Indicates some progress was made for a call to `deflate`/`inflate`. In particular, some quantum of data was read
     * and processed and/or written to the output buffer before the call returned. However, it may be the case that no
     * more input/output buffer remains after the call.
     */
    Ok(0),

    /**
     * Indicates that the end of the output stream has been reached; either the end of the compression stream for
     * `deflate` or the end of the decompression stream for `inflate`. After this result, the callee must be either
     * reset or closed.
     */
    StreamEnd(1),

    /**
     * Indicates that a decompressor needs the predefined dictionary chosen by the compressor.
     */
    NeedsDictionary(2)
}

/**
 * Result codes indicating error conditions reported by zlib.
 */
enum class ZError(val value: Int) {
    /**
     * Indicates failure in some external API that caused a zlib operation to fail.
     */
    Errno(-1),

    /**
     * Indicates zlib's own internal state is somehow inconsistent, or that the input/output buffers for `inflate` or
     * `deflate` are unexpectedly `null`.
     */
    StreamError(-2),

    /**
     * If returned by `deflate`, indicates the compression stream was closed too early causing some input or output to
     * be discarded.
     *
     * If returned by `inflate`, indicates the input stream was somehow corrupt.
     */
    DataError(-3),

    /**
     * Indicates that not enough memory could be allocated by zlib for its internal state. For most applications this is
     * fatal, so either a platform's own [RuntimeException] will be propagated or one will be raised.
     */
    MemoryError(-4),

    /**
     * Indicates a version mismatch between what the target header and loaded symbols indicate for the specific instance
     * of zlib loaded by the library.
     */
    VersionError(-6);

    internal fun thrown(msg: String? = null): Nothing {
        throw ZException(this,  msg ?: "The operation failed with $this ($value)")
    }
}

/**
 * Determines the compression level used in [ZStream.deflate] in conjunction with [ZCompressionStrategy]. There are
 * three approaches for the compression levels 0, 1..3, and 4..9 respectively in increasing order of memory usage and
 * aggression.
 */
enum class ZCompressionLevel(val value: Int) {
    DefaultCompression(-1),
    NoCompression(0),
    BestSpeed(1),
    Compression2(2),
    Compression3(3),
    Compression4(4),
    Compression5(5),
    Compression6(6),
    Compression7(7),
    Compression8(8),
    BestCompression(9)
}

/**
 * Used to tune the compression algorithm, affecting the compression ratio but never the correctness of the compressed
 * output (even if it is not set optimally for the given data).
 */
enum class ZCompressionStrategy(val value: Int) {
    /**
     * Useful for generic data.
     */
    DefaultStrategy(0),

    /**
     * Useful for data produced by some filter/predictor. Filtered data consists mostly of small values with a somewhat
     * random distribution (e.g. PNG filters). This forces more Huffman coding and less string matching than default.
     */
    Filtered(1),

    /**
     * Useful for forcing Huffman encoding only.
     */
    HuffmanOnly(2),

    /**
     * Run-length encoding; Useful for limiting match distances to one. It is almost as fast as [HuffmanOnly], but
     * should give better compression for PNG image data.
     */
    RLE(3),

    /**
     * Uses the default string matching, but prevents the use of dynamic Huffman codes, allowing for a simpler decoder
     * for special applications.
     */
    Fixed(4)
}
