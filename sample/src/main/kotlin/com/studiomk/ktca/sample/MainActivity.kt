package com.studiomk.ktca.sample

import android.os.Bundle
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
import com.studiomk.ktca.sample.home.Home
import com.studiomk.ktca.sample.home.HomeScreen
import com.studiomk.ktca.sample.ui.theme.KtcaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = Store(Home.State(), Home)
        setContent {
            KtcaTheme {
                HomeScreen(store)
            }
        }
    }
}

