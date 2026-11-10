/*
 * Copyright (C) 2026 The RisingOS Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.accessibility.flashnotifications.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.preference.Preference
import com.android.settings.core.BasePreferenceController
import com.android.settingslib.datastore.HandlerExecutor
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.datastore.KeyedObserver
import com.android.settingslib.datastore.SettingsSystemStore
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.PreferenceLifecycleContext
import com.android.settingslib.metadata.PreferenceLifecycleProvider
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.preferencesapi.preconditions.PreconditionStability
import com.android.settingslib.preference.PreferenceBinding

/**
 * Catalyst entry for one of the custom flash notification settings (pulse count, speed,
 * frequency, ON/OFF duration, strength, call speed).
 *
 * The click behaviour (preset list + custom value dialog) and the summary are delegated to the
 * existing legacy [BasePreferenceController] so both the legacy XML screen and the Catalyst screen
 * share a single implementation.
 */
class FlashNotificationCustomPreference(
    override val key: String,
    @StringRes private val titleRes: Int,
    private val controllerFactory: (Context, String) -> BasePreferenceController,
) :
    PreferenceMetadata,
    PreferenceBinding,
    PreferenceLifecycleProvider,
    PreferenceAvailabilityProvider,
    KeyedObserver<String?> {

    private var lifecycleContext: PreferenceLifecycleContext? = null
    private var dataStore: KeyValueStore? = null

    override val purpose: Int
        get() = titleRes

    override val title: Int
        get() = titleRes

    override val availabilityDescription = "Flash notification customization is always available."

    override fun getAvailabilityStability() = PreconditionStability.STABLE_UNTIL_APK_UPDATE

    override fun isAvailable(context: Context): Boolean = true

    override fun onCreate(context: PreferenceLifecycleContext) {
        super.onCreate(context)
        lifecycleContext = context
        dataStore = SettingsSystemStore.get(context)
    }

    override fun onStart(context: PreferenceLifecycleContext) {
        super.onStart(context)
        OBSERVED_KEYS.forEach { dataStore?.addObserver(it, this, HandlerExecutor.main) }
    }

    override fun onStop(context: PreferenceLifecycleContext) {
        super.onStop(context)
        OBSERVED_KEYS.forEach { dataStore?.removeObserver(it, this) }
    }

    override fun onKeyChanged(key: String?, reason: Int) {
        lifecycleContext?.notifyPreferenceChange(this.key)
    }

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        val controller = controllerFactory(preference.context, key)
        preference.summary = controller.summary
        preference.isSelectable = true
        preference.setOnPreferenceClickListener { pref ->
            controller.handlePreferenceTreeClick(pref)
        }
    }

    companion object {
        private val OBSERVED_KEYS =
            arrayOf(
                "flash_notification_pulse_count",
                "flash_notification_on_ms",
                "flash_notification_off_ms",
                "flash_notification_strength",
                "flash_notification_call_on_ms",
                "flash_notification_call_off_ms",
            )
    }
}
