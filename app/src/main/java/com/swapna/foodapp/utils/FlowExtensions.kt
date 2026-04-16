package com.swapna.foodapp.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

// WHY this file?
// Backpressure handling used in EVERY ViewModel
// Central utilities = consistent strategy everywhere
// Change once here → all ViewModels updated

// ── Strategy 1: Buffer with DROP_OLDEST ──────────────────────
// When buffer full → oldest event dropped → newest kept
// Good for: UI events where latest state matters most
//   e.g. multiple rapid ShowError events
//   → show latest error not first one

// ── Strategy 2: Suspend on overflow ──────────────────────────
// When buffer full → emit() suspends (waits)
// Good for: navigation events that MUST not be dropped
//   e.g. NavigateBack, OrderPlaced

// ── Strategy 3: Large buffer ─────────────────────────────────
// When buffer large enough → overflow unlikely
// Good for: events where all must be processed in order

// ── Our App Strategy ─────────────────────────────────────────
// Navigation events → must never drop → suspend on overflow
// UI feedback events → can drop oldest → DROP_OLDEST
// All events use Channel underneath for ordering guarantee

// Buffer sizes matched to usage:
const val EVENT_BUFFER_NAVIGATION = 10  // nav events must not drop
const val EVENT_BUFFER_UI         = 3   // snackbars can drop oldest
const val EVENT_BUFFER_DEFAULT    = 5   // general events