package pako

object Constants {
    /* Allowed flush values; see deflate() and inflate() below for details */
    val Z_NO_FLUSH: Int = 0
    val Z_PARTIAL_FLUSH: Int = 1
    val Z_SYNC_FLUSH: Int = 2
    val Z_FULL_FLUSH: Int = 3
    val Z_FINISH: Int = 4
    val Z_BLOCK: Int = 5
    val Z_TREES: Int = 6

    /* Return codes for the compression/decompression functions. Negative values
    * are errors, positive values are used for special but normal events.
    */
    val Z_OK: Int = 0
    val Z_STREAM_END: Int = 1
    val Z_NEED_DICT: Int = 2
    val Z_ERRNO: Int = -1
    val Z_STREAM_ERROR: Int = -2
    val Z_DATA_ERROR: Int = -3
    val Z_MEM_ERROR: Int = -4
    val Z_BUF_ERROR: Int = -5
    //val Z_VERSION_ERROR: Int = -6

    /* compression levels */
    val Z_NO_COMPRESSION: Int = 0
    val Z_BEST_SPEED: Int = 1
    val Z_BEST_COMPRESSION: Int = 9
    val Z_DEFAULT_COMPRESSION: Int = -1


    val Z_FILTERED: Int = 1
    val Z_HUFFMAN_ONLY: Int = 2
    val Z_RLE: Int = 3
    val Z_FIXED: Int = 4
    val Z_DEFAULT_STRATEGY: Int = 0

    /* Possible values of the data_type field (though see inflate()) */
    val Z_BINARY: Int = 0
    val Z_TEXT: Int = 1
    //val Z_ASCII: Int = 1 // = Z_TEXT (deprecated)
    val Z_UNKNOWN: Int = 2

    /* The deflate compression method */
    val Z_DEFLATED: Int = 8
    //val Z_NULL: Int? = null // Use -1 or null inline, depending on var type
}
