/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.plugin.datasync

import android.os.Bundle
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SyncSettingsFragment : PreferenceFragmentCompat() {

    private lateinit var syncPreferences: SyncPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        syncPreferences = SyncPreferences(requireContext())
        findPreference<MultiSelectListPreference>(SyncPreferences.KEY_EXPORT_SECTIONS)?.apply {
            summaryProvider = MultiSelectListPreference.SimpleSummaryProvider.getInstance()
        }
        findPreference<MultiSelectListPreference>(SyncPreferences.KEY_IMPORT_SECTIONS)?.apply {
            summaryProvider = MultiSelectListPreference.SimpleSummaryProvider.getInstance()
        }
        updatePathSummaries()
    }

    override fun onResume() {
        super.onResume()
        updatePathSummaries()
    }

    private fun updatePathSummaries() {
        val context = requireContext()
        findPreference<Preference>(SyncPreferences.KEY_EXPORT_PATH)?.summary =
            getString(
                R.string.pref_export_path_summary,
                syncPreferences.exportFile(context).absolutePath
            )
        findPreference<Preference>(SyncPreferences.KEY_IMPORT_PATH)?.summary =
            getString(
                R.string.pref_import_path_summary,
                syncPreferences.importFile(context).absolutePath
            )
    }
}
