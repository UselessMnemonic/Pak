package com.uselessmnemonic.pak

/**
 * The base class for all zlib exceptions.
 *
 * zlib communicates errors via return codes, but some platforms elect to raise exceptions instead. ZException is the
 * base class by which platform exceptions will be captured and propagated.
 *
 * In cases where the target platform does not raise an exception, one will be synthesized and its data populated
 * from the state of the ZStream that raised it.
 */
class ZException(
    val error: ZError,
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)
