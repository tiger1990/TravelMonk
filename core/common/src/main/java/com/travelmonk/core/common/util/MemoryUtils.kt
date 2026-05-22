package com.travelmonk.core.common.util

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import com.travelmonk.core.logger.TravelMonkLogger

/**
 * Utility for memory and process lifecycle monitoring.
 */
class MemoryUtils {
    companion object {
        private const val TAG = "MemoryUtils"

        /**
         * Retrieves and logs the reason for the last process termination.
         * Only available on Android 11 (API 30) and above.
         * call: MemoryUtils.getExitInfo(context)
         */
        fun getExitInfo(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                // Retrieves the list of exit reasons for the current package
                val exitReasons = activityManager.getHistoricalProcessExitReasons(null, 0, 1)

                if (exitReasons.isNotEmpty()) {
                    val lastExit = exitReasons[0]
                    TravelMonkLogger.i(
                        tag = TAG,
                        msg = "Last process exit reason: ${getReasonString(lastExit.reason)}",
                        metadata = mapOf(
                            "reason_code" to lastExit.reason.toString(),
                            "description" to (lastExit.description ?: "none"),
                            "timestamp" to lastExit.timestamp.toString(),
                            "importance" to lastExit.importance.toString()
                        )
                    )
                }
            } else {
                TravelMonkLogger.d(TAG, "Exit reason tracking not supported on this Android version")
            }
        }

        private fun getReasonString(reason: Int): String {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return "UNKNOWN"
            
            return when (reason) {
                ApplicationExitInfo.REASON_EXIT_SELF -> "EXIT_SELF"
                ApplicationExitInfo.REASON_SIGNALED -> "SIGNALED"
                ApplicationExitInfo.REASON_LOW_MEMORY -> "LOW_MEMORY"
                ApplicationExitInfo.REASON_CRASH -> "CRASH"
                ApplicationExitInfo.REASON_CRASH_NATIVE -> "CRASH_NATIVE"
                ApplicationExitInfo.REASON_ANR -> "ANR"
                ApplicationExitInfo.REASON_INITIALIZATION_FAILURE -> "INITIALIZATION_FAILURE"
                ApplicationExitInfo.REASON_PERMISSION_CHANGE -> "PERMISSION_CHANGE"
                ApplicationExitInfo.REASON_USER_REQUESTED -> "USER_REQUESTED"
                ApplicationExitInfo.REASON_USER_STOPPED -> "USER_STOPPED"
                ApplicationExitInfo.REASON_DEPENDENCY_DIED -> "DEPENDENCY_DIED"
                ApplicationExitInfo.REASON_OTHER -> "OTHER"
                else -> "UNKNOWN ($reason)"
            }
        }
    }
}
