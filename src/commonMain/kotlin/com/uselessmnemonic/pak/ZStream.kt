package com.uselessmnemonic.pak

/**
 * An interface which represents a ZLib stream object.
 *
 * This interface is implemented per-platform to take advantage of existing compression machinery. It conforms closely
 * to the ZLib specification, encapsulating as many operations as is permissible across platforms.
 *
 * Implementations are expected to initialize the ZStream to a known state. In particular, any buffers should be
 * initialized to empty rather than null states, the [totalIn]/[totalOut] fields to zero, and the [adler] field to the
 * starting value of 1. After a stream is closed, it may be left in any arbitrary state and the stream must not be used
 * again.
 *
 * Though zlib communicates error conditions through return values, the target platform may elect to raise exceptions.
 * Implementations therefore must propagate exceptions or raise them in response to zlib error codes.
 *
 * Applications are expected to manage ZStream carefully, since the underlying resources are not guaranteed to be
 * garbage collected.
 */
interface ZStream : AutoCloseable {

    /**
     * The number of bytes available for the compression engine to read.
     */
    val availIn: UInt

    /**
     * The number of bytes available for the compression engine to read.
     */
    val availOut: UInt

    /**
     * The number of bytes read as input by the compression engine.
     */
    val totalIn: ULong

    /**
     * The number of bytes written as output by the compression engine.
     */
    val totalOut: ULong

    /**
     * The ADLER-32 checksum of compressed data.
     */
    val adler: ULong

    /**
     * Provides input to the compression engine from the given slice of the byte array. Once set, the engine will
     * continue to slice the same input buffer automatically until it is exhausted.
     *
     * @param buffer The input buffer
     * @param indices The bounds of input data in the buffer
     */
    fun setInput(buffer: ByteArray, indices: IntRange = buffer.indices)

    /**
     * Provides output space to the compression engine from the given slice of the byte array. Once set, the engine will
     * fill as much output as is permitted.
     *
     * @param buffer The output buffer
     * @param indices The bounds of output space in the buffer
     */
    fun setOutput(buffer: ByteArray, indices: IntRange = buffer.indices)

    /**
     * Initializes the internal stream state for compression.
     *
     * @param level The [ZCompressionLevel] level; Not all platforms support all levels.
     * @return [ZResult.Ok]
     * @throws ZException
     */
    fun deflateInit(level: ZCompressionLevel): ZResult

    /**
     * Dynamically update the compression level and compression strategy. This can be used to switch between compression
     * and straight copy of the input data, or to switch to a different kind of input data requiring a different
     * strategy.
     *
     * If the compression approach (which is a function of the level) or the strategy is changed, and if there have been
     * any calls to [deflate] since the state was initialized or reset, then the input available so far is compressed
     * with the old level and strategy and flushed with [ZFlush.Block]. The new level and strategy will take effect at
     * the next operation.
     *
     * If there is not enough output space to complete a compression block, then the parameter change will not take
     * effect. In this case, more output space needs to be supplied before another call can succeed.
     *
     * In order to assure a change in the parameters on the first try, the stream should first be flushed using
     * `deflate()` with [ZFlush.Block] or other flush request until [availOut] is not zero. Then no more input data
     * should be provided before the call to [deflateParams].
     *
     * @param level The desired [ZCompressionLevel]
     * @param strategy The desired [ZCompressionStrategy]
     * @return [ZResult.Ok] if successful, or [ZResult.BufferError] if there was not enough output space to complete the
     * compression of any available input data.
     */
    fun deflateParams(level: ZCompressionLevel, strategy: ZCompressionStrategy): ZResult

    /**
     * A non-standard operation which retrieves the size in bytes of the dictionary maintained by [deflate].
     */
    fun deflateGetDictionaryLength(): UInt

    /**
     * Returns the sliding dictionary being maintained by [deflate]. The provided buffer must have enough space
     * where 32768 bytes is always enough.
     *
     * It may return a length less than the window size, even when more than the window size in input has been provided.
     * In that case, it may return up to 258 bytes less due to how zlib's implementation of deflate manages the sliding
     * window and lookahead for matches.
     *
     * If the application needs the last window-size bytes of input, then that would need to be saved by the
     * application.
     *
     * @param dictionary The buffer in which to write data
     * @param indices The range in the buffer which accepts data
     * @return [ZResult.Ok]
     * @throws ZException
     */
    fun deflateGetDictionary(dictionary: ByteArray, indices: IntRange = dictionary.indices): IntRange

    /**
     * Initializes the compression dictionary from the given byte sequence without producing any compressed output.
     * When using the zlib format, this function must be called immediately after [deflateInit] or [deflateReset], and
     * before any call of [deflate]. The compressor and decompressor must use exactly the same dictionary (see
     * [inflateSetDictionary]).
     *
     * The dictionary should consist of strings (byte sequences) that are likely to be encountered later in the data to
     * be compressed, with the most commonly used strings preferably put towards the end of the dictionary. Using a
     * dictionary is most useful when the data to be compressed is short and can be predicted with good accuracy; the
     * data can then be compressed better than with the default empty dictionary.
     *
     * Depending on the size of the compression data structures selected by [deflateInit], a part of the dictionary may
     * in effect be discarded, for example if the dictionary is larger than the window size.
     *
     * Upon return of this function, [adler] is set to the Adler-32 value of the dictionary; the decompressor may later
     * use this value to determine which dictionary has been used by the compressor.
     *
     * @param dictionary The buffer from which to read the dictionary
     * @param indices The bounds of the dictionary data in the buffer
     * @return [ZResult.Ok]
     * @throws ZException
     */
    fun deflateSetDictionary(dictionary: ByteArray, indices: IntRange = dictionary.indices): ZResult

    /**
     * Compresses as much data as possible, and stops when the input buffer becomes empty or the output buffer becomes
     * full. It may introduce some output latency (reading input without producing any output) except when
     * forced to flush. In particular, this operation may:
     *
     * - Compress more input and update [availIn] accordingly. If not all input can be processed (because there is not
     * enough room in the output buffer, for example), processing will resume at this point for the next call.
     *
     * - Generate more output and update [availOut] accordingly. This action is forced if the parameter [flush] is
     * anything but [ZFlush.NoFlush]. Forcing flush frequently degrades the compression ratio, so this parameter should
     * be set only when necessary. Some output may be provided even if [flush] is [ZFlush.NoFlush].
     *
     * Before the call to [deflate], the application should ensure that at least one of the above actions is possible
     * by providing more input and/or consuming more output. [availOut] should never read zero before the call.
     * The application can consume the compressed output as frequently as it wants, for example when the output buffer
     * is full or after each call of deflate. If deflate returns [ZResult.Ok] while [availOut] is 0, it must be called
     * again after making room in the output buffer because there might be more output pending.
     *
     * It sets [adler] to the Adler-32 checksum of all input read so far (that is, [totalIn] bytes).
     *
     * @param flush instructs deflate when to flush. See [ZFlush] for complete details
     * @return [ZResult.Ok] if some progress was made, [ZResult.BufferError] if no progress was possible, and
     * [ZResult.StreamEnd] if all output has been produced
     * @throws ZException
     */
    fun deflate(flush: ZFlush): ZResult

    /**
     * This function is equivalent to [deflateEnd] followed by [deflateInit], but does not free and reallocate the
     * internal compression state. The stream will leave the compression level and any other attributes that may have
     * been set unchanged. [totalIn], [totalOut], and [adler] are initialized.
     *
     * @return [ZResult.Ok] if successful
     * @throws ZException
     */
    fun deflateReset(): ZResult

    /**
     * All dynamically allocated data structures for this stream are freed. This function discards any unprocessed input
     * and does not flush any pending output.
     *
     * @return [ZResult.Ok] if successful
     * @throws ZException containing [ZError.DataError] if some input or output was discarded
     */
    fun deflateEnd(): ZResult

    /**
     * Initializes the internal stream state for decompression.
     *
     * @return [ZResult.Ok]
     * @throws ZException
     */
    fun inflateInit(): ZResult

    /**
     * A non-standard operation which retrieves the size in bytes of the dictionary maintained by [inflate].
     */
    fun inflateGetDictionaryLength(): UInt

    /**
     * Returns the sliding dictionary being maintained by [inflateInit]. The provided buffer must have enough space
     * where 32768 bytes is always enough.
     *
     * @param dictionary The buffer in which to write data
     * @param indices The range in the buffer which accepts data
     * @return [ZResult.Ok]
     * @throws ZException
     */
    fun inflateGetDictionary(dictionary: ByteArray, indices: IntRange = dictionary.indices): IntRange

    /**
     * Initializes the decompression dictionary from the given uncompressed byte sequence. This function must be called
     * immediately after a call of [inflate], if that call returned [ZResult.NeedsDictionary].
     *
     * The dictionary chosen by the compressor can be determined from the Adler-32 value returned by that call of
     * inflate. The compressor and decompressor must use exactly the same dictionary (see [deflateSetDictionary]).
     * If the provided dictionary is smaller than the window and there is already data in the window, then the provided
     * dictionary will amend what's there. The application must ensure that the dictionary that was used for compression
     * is provided.
     *
     * @return [ZResult.Ok] if success
     * @throws ZException
     */
    fun inflateSetDictionary(dictionary: ByteArray, indices: IntRange = dictionary.indices): ZResult

    /**
     * Decompresses as much data as possible, and stops when the input buffer becomes empty or the output buffer becomes
     * full. It may introduce some output latency (reading input without producing any output) except when forced to
     * flush. In particular, this operation may:
     *
     * - Decompress more input and update [availIn] accordingly. If not all input can be processed (because there is not
     * enough room in the output buffer), then processing will resume at this point for the next call of inflate.
     *
     * - Generate more output and update [availOut] accordingly. inflate provides as much output as possible, until
     * there is no more input data or no more space in the output buffer (see [flush].)
     *
     * If a preset dictionary is needed after this call (see [inflateSetDictionary]), inflate sets [adler] to the
     * Adler-32 checksum of the dictionary chosen by the compressor and returns [ZResult.NeedsDictionary]; otherwise it
     * sets [adler] to the Adler-32 checksum of all output produced so far (that is, [totalOut] bytes).
     * At the end of the stream, inflate() checks that its computed Adler-32 checksum is equal to that saved by the
     * compressor and returns [ZResult.StreamEnd] only if the checksum is correct.
     *
     * @param flush Can be [ZFlush.NoFlush], [ZFlush.SyncFlush], [ZFlush.Finish], [ZFlush.Block] or [ZFlush.Trees],
     * barring limitations of the host platform.
     * @return [ZResult.Ok] if some progress was made, [ZResult.BufferError] if no progress was possible, and
     * [ZResult.StreamEnd] if all output has been produced with a matching checksum.
     * @throws ZException
     */
    fun inflate(flush: ZFlush): ZResult

    /**
     * This function is equivalent to [inflateEnd] followed by [inflateInit], but does not free and reallocate the
     * internal decompression state. [totalIn], [totalOut], and [adler] are initialized.
     *
     * @return [ZResult.Ok] if successful
     * @throws ZException
     */
    fun inflateReset(): ZResult

    /**
     * All dynamically allocated data structures for this stream are freed. This function discards any unprocessed input
     * and does not flush any pending output.
     *
     * @return [ZResult.Ok] if successful
     * @throws ZException containing [ZError.DataError] if some input or output was discarded
     */
    fun inflateEnd(): ZResult

    /**
     * Closes this stream and explicitly releases any underlying resources. It is an error to use a stream after it has
     * been closed.
     */
    override fun close()
}

/**
 * Creates the default ZStream for the host platform.
 */
expect fun ZStream(): ZStream
