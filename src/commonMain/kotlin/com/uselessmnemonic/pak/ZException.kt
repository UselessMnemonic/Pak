package com.uselessmnemonic.pak

/**
 * The base class for all ZLib exceptions.
 *
 * ZLib communicates errors via return codes, but since most error cases are exceptional it is preferred to encapsulate
 * them in an exception.
 *
 * @param error The [ZError] represented by this exception.
 * @param message A message string supplied by the compression engine.
 */
class ZException(
    val error: ZError,
    message: String
) : Exception(message)
