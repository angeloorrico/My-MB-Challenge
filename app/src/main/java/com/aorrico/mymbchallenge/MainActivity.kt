package com.aorrico.mymbchallenge

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.aorrico.mymbchallenge.core.ui.theme.MyMbChallengeTheme
import com.aorrico.mymbchallenge.ui.ExchangeListDetailScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent {
            MyMbChallengeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ExchangeListDetailScreen()
                }
            }
        }
    }

    // Android 13+ requires POST_NOTIFICATIONS to be granted at runtime, not just declared in the
    // manifest - without it, Chucker's debug-only HTTP inspector notification never shows up even
    // though it's still capturing traffic. On release builds the permission isn't in the merged
    // manifest at all (library-no-op doesn't declare it), so this is simply a no-op there.
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val alreadyGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        if (!alreadyGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
