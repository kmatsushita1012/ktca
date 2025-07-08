package com.studiomk.ktca.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.studiomk.ktca.core.store.Store
import com.studiomk.ktca.sample.counter.Counter
import com.studiomk.ktca.sample.counter.CounterScreen
import com.studiomk.ktca.sample.home.Home
import com.studiomk.ktca.sample.home.HomeScreen
import com.studiomk.ktca.sample.ui.theme.KtcaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = Store(Home.State(), Home) { message ->
            Log.d("Main Store", message) // or use custom logger
        }
        setContent {
            KtcaTheme {
                HomeScreen(store)
            }
        }
//        val store = Store(Counter.State(), Counter) { message ->
//            Log.d("Main Store", message) // or use custom logger
//        }
//        setContent {
//            KtcaTheme {
//                CounterScreen(store)
//            }
//        }
    }
}

