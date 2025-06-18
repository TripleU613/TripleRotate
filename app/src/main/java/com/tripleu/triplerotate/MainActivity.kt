package com.tripleu.triplerotate

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.tripleu.triplerotate.ui.theme.RotationTileAppTheme
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    // Activity result launcher to handle the permission request
    private val writeSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (true && Settings.System.canWrite(this)) {
            Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
            // After getting permission, prompt to add the tile
            promptToAddTile()
        } else {
            Toast.makeText(this, "Permission not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply your custom Material 3 theme
            RotationTileAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Call the main screen Composable
                    MainScreen()
                }
            }
        }
    }

    // Composable function for the main UI
    @Composable
    fun MainScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Hi, Try This:",
                fontSize = 34.sp
            )
            Button(
                onClick = { requestWriteSettingsPermission() },
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text("Grant Permissions and Add Tile")
            }
        }
    }

    private fun requestWriteSettingsPermission() {
        if (!Settings.System.canWrite(this)) {
            // If permission is not granted, redirect to settings
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = "package:$packageName".toUri()
            writeSettingsLauncher.launch(intent)
        } else {
            // Permission is already granted
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show()
            promptToAddTile()
        }
    }

    private fun promptToAddTile() {
        // This feature is only for Android 13 (API 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val statusBarManager = getSystemService("statusbar") as StatusBarManager
            statusBarManager.requestAddTileService(
                ComponentName(
                    this,
                    RotationTileService::class.java
                ),
                "Rotation Toggle", // Label for the tile in the prompt
                Icon.createWithResource(this, R.drawable.ic_screen_rotation_tile),
                Executors.newSingleThreadExecutor()
            ) { result ->
                // Handle the result of the request
                val message = when (result) {
                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> "Tile added successfully!"
                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> "Tile was already added."
                    else -> "Failed to add tile."
                }
                // Show the result on the main thread
                runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
            }
        } else {
            // For older versions, inform the user to add it manually
            Toast.makeText(
                this,
                "Please add the 'Rotation Toggle' tile from your Quick Settings edit panel.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
