package com.jhr.anfunsanimeplugin.plugin.util

object Text {
    private val trimRegex = Regex("\\s+")
    fun String.trimAll() = trimRegex.replace(this, "")
}