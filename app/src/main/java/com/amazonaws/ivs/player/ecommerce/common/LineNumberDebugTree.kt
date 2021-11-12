package com.amazonaws.ivs.player.ecommerce.common

import timber.log.Timber

private const val TIMBER_TAG = "eCommerce"

class LineNumberDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement) =
        "$TIMBER_TAG: (${element.fileName}:${element.lineNumber}) #${element.methodName} "
}
