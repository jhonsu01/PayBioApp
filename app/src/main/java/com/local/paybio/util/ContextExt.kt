package com.local.paybio.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** Walks the ContextWrapper chain to find the hosting Activity (for window flags / lock task). */
fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
