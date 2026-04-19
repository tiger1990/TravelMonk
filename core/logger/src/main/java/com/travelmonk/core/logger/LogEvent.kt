package com.travelmonk.core.logger

data class LogEvent(
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null,
    val traceId: String? = null,
    val spanId: String? = null,
    val flow: String? = null,
    val launchStack: String? = null,
    val metadata: Map<String, Any?> = emptyMap(),
    val thread: String = Thread.currentThread().name
)
