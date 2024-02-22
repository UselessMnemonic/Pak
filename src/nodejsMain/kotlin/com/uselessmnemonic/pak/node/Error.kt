package com.uselessmnemonic.pak.node

external class Error {
    val name: String
    val message: String
    val stack: String?
    constructor()
    constructor(message: String)
}
