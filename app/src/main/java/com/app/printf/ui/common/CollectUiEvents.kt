package com.app.printf.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

@Composable
fun <T> CollectUiEvents(
    events: Flow<T>,
    onEvent: (T) -> Unit,
) {
    LaunchedEffect(events) {
        events.collect(onEvent)
    }
}
