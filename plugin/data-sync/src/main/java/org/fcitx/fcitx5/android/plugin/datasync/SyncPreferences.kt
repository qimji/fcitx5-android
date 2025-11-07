/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.plugin.datasync

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.fcitx.fcitx5.android.common.data.UserDataSection

class SyncPreferences(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)

    val exportEnabled: Boolean
        get() = prefs.getBoolean(KEY_EXPORT_ENABLED, false)

    val importEnabled: Boolean
        get() = prefs.getBoolean(KEY_IMPORT_ENABLED, false)

    val exportIntervalMillis: Long
        get() = minutesToMillis(prefs.getString(KEY_EXPORT_INTERVAL, DEFAULT_EXPORT_INTERVAL))

    val importIntervalMillis: Long
        get() = minutesToMillis(prefs.getString(KEY_IMPORT_INTERVAL, DEFAULT_IMPORT_INTERVAL))

    fun exportSections(): Set<UserDataSection> = parseSections(
        prefs.getStringSet(KEY_EXPORT_SECTIONS, null)
    )

    fun importSections(): Set<UserDataSection> = parseSections(
        prefs.getStringSet(KEY_IMPORT_SECTIONS, null)
    )

    fun exportFile(context: Context = appContext): java.io.File =
        resolveBaseDir(context).resolve(EXPORT_FILE_NAME)

    fun importFile(context: Context = appContext): java.io.File =
        resolveBaseDir(context).resolve(IMPORT_FILE_NAME)

    fun register(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregister(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun parseSections(values: Set<String>?): Set<UserDataSection> {
        if (values.isNullOrEmpty()) return DEFAULT_SECTIONS
        return values.mapNotNull(UserDataSection::fromId).toSet().ifEmpty { DEFAULT_SECTIONS }
    }

    private fun resolveBaseDir(context: Context): java.io.File {
        val external = context.getExternalFilesDir(DIR_NAME)
        val base = external ?: context.filesDir.resolve(DIR_NAME)
        if (!base.exists()) {
            base.mkdirs()
        }
        return base
    }

    private fun minutesToMillis(raw: String?): Long {
        val minutes = raw?.toLongOrNull() ?: DEFAULT_INTERVAL_MINUTES
        return minutes.coerceAtLeast(1L) * 60_000L
    }

    companion object {
        const val KEY_EXPORT_ENABLED = "export_enabled"
        const val KEY_EXPORT_INTERVAL = "export_interval"
        const val KEY_EXPORT_SECTIONS = "export_sections"
        const val KEY_EXPORT_PATH = "export_path"
        const val KEY_IMPORT_ENABLED = "import_enabled"
        const val KEY_IMPORT_INTERVAL = "import_interval"
        const val KEY_IMPORT_SECTIONS = "import_sections"
        const val KEY_IMPORT_PATH = "import_path"

        private const val DIR_NAME = "data-sync"
        private const val EXPORT_FILE_NAME = "fcitx5-data-sync-export.zip"
        private const val IMPORT_FILE_NAME = "fcitx5-data-sync-import.zip"
        private const val DEFAULT_EXPORT_INTERVAL = "60"
        private const val DEFAULT_IMPORT_INTERVAL = "720"
        private const val DEFAULT_INTERVAL_MINUTES = 60L

        private val DEFAULT_SECTIONS = setOf(
            UserDataSection.SHARED_PREFS,
            UserDataSection.DATABASES,
            UserDataSection.EXTERNAL
        )
    }
}
