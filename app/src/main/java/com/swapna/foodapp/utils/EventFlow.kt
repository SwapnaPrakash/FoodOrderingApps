package com.swapna.foodapp.utils

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// WHY EventFlow wrapper?
// Different events need different overflow strategies
// Navigation = must never drop (use SUSPEND)
// Snackbar = can drop oldest (use DROP_OLDEST)
// Wrapping in typed factory = no accidental wrong strategy

// ── Factory for Navigation Events ────────────────────────────
// SUSPEND on overflow = emit() waits until space available
// Navigation events MUST be processed in order
// e.g. OrderPlaced → ClearCart → NavigateHome
// Drop any of these = broken user flow

fun <T> navigationEventFlow(): MutableSharedFlow<T> =
    MutableSharedFlow(
        replay              = 0,
        // Large buffer = unlikely to overflow
        // even on rapid actions
        extraBufferCapacity = EVENT_BUFFER_NAVIGATION,
        // SUSPEND = emit() waits when buffer full
        // Guarantees ALL navigation events are processed
        onBufferOverflow    = BufferOverflow.SUSPEND,
    )

// ── Factory for UI Feedback Events ───────────────────────────
// DROP_OLDEST on overflow = newest events kept
// Old snackbar messages dropped = user sees latest
// e.g. multiple rapid error messages → show latest

fun <T> uiFeedbackEventFlow(): MutableSharedFlow<T> =
    MutableSharedFlow(
        replay              = 0,
        extraBufferCapacity = EVENT_BUFFER_UI,
        // DROP_OLDEST = if buffer full, oldest dropped
        // newest event always gets through
        onBufferOverflow    = BufferOverflow.DROP_OLDEST,
    )

// ── Factory for General Events ────────────────────────────────
// Safe default for mixed event types
fun <T> generalEventFlow(): MutableSharedFlow<T> =
    MutableSharedFlow(
        replay              = 0,
        extraBufferCapacity = EVENT_BUFFER_DEFAULT,
        // DROP_OLDEST = safe fallback
        // prevents suspend blocking ViewModel on fast events
        onBufferOverflow    = BufferOverflow.DROP_OLDEST,
    )