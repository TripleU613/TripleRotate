package com.tripleu.triplerotate

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

@Suppress("DEPRECATION")
class RotationTileService : TileService() {

    // Called when the tile is added to the Quick Settings panel
    override fun onTileAdded() {
        super.onTileAdded()
        updateTile()
    }

    // Called when the tile becomes visible
    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    // Called when the user taps the tile
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()

        // Check if the app has permission to modify system settings
        if (!Settings.System.canWrite(this)) {
            // If permission is not granted, open the settings screen for the user
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityAndCollapse(intent)
            return
        }

        // Get the current screen orientation setting
        val currentOrientation = try {
            Settings.System.getInt(contentResolver, Settings.System.USER_ROTATION)
        } catch (_: Settings.SettingNotFoundException) {
            // Default to portrait if not set
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // Toggle between Landscape and Portrait
        val newOrientation = if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        // Apply the new orientation setting
        Settings.System.putInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) // Disable auto-rotate
        Settings.System.putInt(contentResolver, Settings.System.USER_ROTATION, newOrientation)

        // Update the tile's appearance
        updateTile()
    }

    // Helper function to update the tile's state, icon, and label
    private fun updateTile() {
        val tile = qsTile ?: return // Exit if the tile is not available

        val isLandscape = try {
            Settings.System.getInt(contentResolver, Settings.System.USER_ROTATION) == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } catch (_: Exception) {
            false
        }

        // Update state: active for landscape, inactive for portrait
        if (isLandscape) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Landscape"
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Portrait"
        }

        tile.icon = Icon.createWithResource(this, R.drawable.ic_screen_rotation_tile)
        tile.updateTile()
    }
}
