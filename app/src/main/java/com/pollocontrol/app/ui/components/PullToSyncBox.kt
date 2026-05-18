package com.pollocontrol.app.ui.components

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.coroutines.launch

@Composable
fun PullToSyncBox(
    onSync: suspend () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val thresholdPx = with(LocalDensity.current) { 92.dp.toPx() }
    var pullDistance by remember { mutableFloatStateOf(0f) }
    var isSyncing by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag && available.y < 0 && pullDistance > 0f) {
                    pullDistance = (pullDistance + available.y).coerceAtLeast(0f)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.Drag && available.y > 0 && !isSyncing) {
                    pullDistance = (pullDistance + available.y).coerceAtMost(thresholdPx * 1.35f)
                    if (pullDistance >= thresholdPx) {
                        isSyncing = true
                        scope.launch {
                            runCatching { onSync() }
                            pullDistance = 0f
                            isSyncing = false
                        }
                    }
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        content()
        if (isSyncing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            )
        } else if (pullDistance > 0f) {
            LinearProgressIndicator(
                progress = { (pullDistance / thresholdPx).coerceIn(0f, 1f) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            )
        }
    }
}
