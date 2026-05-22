package com.travelmonk.core.common.ui

import android.content.Context
import androidx.annotation.StringRes

/**
 * Bridges ViewModel error strings and localised Android resources.
 *
 * ViewModels cannot call [Context.getString] — they must not hold a Context reference.
 * Instead, they emit [UiText] into state:
 * - Known, static errors → [Res] with a string resource ID (fully localisable).
 * - API/server errors with dynamic messages → [Raw] with the raw message string.
 *
 * The UI resolves it at the last moment via [asString]:
 * ```kotlin
 * state.error?.asString(LocalContext.current)
 * ```
 */
sealed class UiText {

    /** A raw string — use for dynamic API error messages where localisation is not applicable. */
    data class Raw(val value: String) : UiText()

    /** A localised string resource — use for all known, static error messages. */
    data class Res(@param:StringRes val id: Int) : UiText()

    fun asString(context: Context): String = when (this) {
        is Raw -> value
        is Res -> context.getString(id)
    }
}
