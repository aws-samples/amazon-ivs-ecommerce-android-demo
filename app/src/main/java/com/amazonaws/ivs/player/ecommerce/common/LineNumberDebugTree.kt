package com.amazonaws.ivs.player.ecommerce.common

import timber.log.Timber

private const val DEBUG_TAG = "eCommerce"

class LineNumberDebugTree : Timber.DebugTree() {
    private var method = ""

    override fun createStackElementTag(element: StackTraceElement): String {
        method = "#${element.methodName}"
        return "(${element.fileName}:${element.lineNumber})"
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, "${DEBUG_TAG}: $method: $message", t)
    }
}
