package com.studiomk.ktca.sample.home

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studiomk.ktca.core.store.StoreOf
import com.studiomk.ktca.sample.counter.CounterScreen
import com.studiomk.ktca.ui.FullScreenNavigation
import com.studiomk.ktca.ui.SheetNavigation


@Composable
fun HomeScreen(
    store: StoreOf<Home.State, Home.Action>
) {
    val state by store.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Text(text = state.title)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = { store.send(Home.Action.CounterButton1Tapped()) }) {
                Text("Counter 1")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { store.send(Home.Action.CounterButton2Tapped()) }) {
                Text("Counter 2")
            }
        }
    }
    FullScreenNavigation(
        item = store.optionalScope(
            statePath = Home.destinationKey + Home.Destination.Counter1.key,
            actionPath = Home.destinationCase + Home.Destination.Counter1.case
        )
    ) {
        CounterScreen(it)
    }

    SheetNavigation(
        item = store.optionalScope(
            statePath = Home.destinationKey + Home.Destination.Counter2.key,
            actionPath = Home.destinationCase + Home.Destination.Counter2.case
        )
    ) {
        CounterScreen(it)
    }
}
