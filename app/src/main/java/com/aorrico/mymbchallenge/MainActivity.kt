package com.aorrico.mymbchallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aorrico.mymbchallenge.core.ui.theme.MyMbChallengeTheme
import com.aorrico.mymbchallenge.ui.ExchangeListDetailScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyMbChallengeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ExchangeListDetailScreen()
                }
            }
        }
    }
}
