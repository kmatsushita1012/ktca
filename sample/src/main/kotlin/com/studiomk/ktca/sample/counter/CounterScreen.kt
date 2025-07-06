package com.studiomk.ktca.sample.counter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studiomk.ktca.core.store.StoreOf

@Composable
fun CounterScreen(
    store: StoreOf<Counter.State, Counter.Action>
) {
    val state by store.state.collectAsState()
    Column(modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()) {
        Text(text = "Count: ${state.count}")

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { store.send(Counter.Action.DecrementTapped) }) {
                Text("-")
            }
            Button(onClick = { store.send(Counter.Action.IncrementTapped) }) {
                Text("+")
            }
            Button(onClick = { store.send(Counter.Action.TimerTapped) }) {
                Text("Start Timer")
            }
            Button(onClick = { store.send(Counter.Action.CancelTapped) }) {
                Text("Stop Timer")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { store.send(Counter.Action.DismissTapped) }) {
            Text("Dismiss")
        }
    }
}
