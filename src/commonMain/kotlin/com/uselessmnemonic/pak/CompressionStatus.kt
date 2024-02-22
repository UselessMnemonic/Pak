package com.uselessmnemonic.pak

enum class CompressionStatus(val value: Int) {
    Z_OK(0),
    Z_STREAM_END(1),
    Z_NEED_DICT(2),
    Z_STREAM_ERROR(-2),
    Z_DATA_ERROR(-3),
    Z_MEM_ERROR(-4),
    Z_BUF_ERROR(-5)
}
