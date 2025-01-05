package com.uselessmnemonic.pak

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.config.LogLevel

object ProjectConfig : AbstractProjectConfig() {
    override val logLevel = LogLevel.Warn
}
