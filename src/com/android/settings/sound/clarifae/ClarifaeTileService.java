/*
 * Copyright (C) 2026 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.clarifae;

import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.android.settings.R;

public class ClarifaeTileService extends TileService {
    private static final String TAG = "ClarifaeTileService";

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileState();
    }

    @Override
    public void onClick() {
        super.onClick();
        try {
            boolean currentState = ClarifaeManager.isEnabled();
            boolean newState = !currentState;
            ClarifaeManager.setEnabled(newState);
            updateTileState();
        } catch (Exception e) {
            Log.e(TAG, "Error toggling Clarifae tile: " + e.getMessage(), e);
        }
    }

    private void updateTileState() {
        Tile tile = getQsTile();
        if (tile == null) return;

        boolean isSupported = ClarifaeManager.isClarifaeSupported(this);
        if (!isSupported) {
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.updateTile();
            return;
        }

        boolean isEnabled = ClarifaeManager.isEnabled();
        tile.setState(isEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel(getString(R.string.clarifae_title));
        tile.setSubtitle(isEnabled
                ? (ClarifaeManager.isInEnabled() ? getString(R.string.clarifae_in_enabled_title) : getString(R.string.clarifae_summary_on, "Active"))
                : getString(R.string.clarifae_summary_off));
        tile.setIcon(Icon.createWithResource(this, R.drawable.ic_clarifae));
        tile.updateTile();
    }
}
