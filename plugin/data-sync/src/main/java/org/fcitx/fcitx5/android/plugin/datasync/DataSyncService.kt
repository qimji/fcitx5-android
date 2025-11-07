/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.plugin.datasync

import android.content.SharedPreferences
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.fcitx.fcitx5.android.common.FcitxPluginService
import org.fcitx.fcitx5.android.common.data.ids
import org.fcitx.fcitx5.android.common.ipc.FcitxRemoteConnection
import org.fcitx.fcitx5.android.common.ipc.IFcitxRemoteService
import org.fcitx.fcitx5.android.common.ipc.bindFcitxRemoteService
import java.io.File

class DataSyncService : FcitxPluginService(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val preferences by lazy { SyncPreferences(applicationContext) }
    private var connection: FcitxRemoteConnection? = null
    private var remoteService: IFcitxRemoteService? = null
    private var scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var exportJob: Job? = null
    private var importJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        preferences.register(this)
    }

    override fun onDestroy() {
        preferences.unregister(this)
        super.onDestroy()
    }

    override fun start() {
        scope.cancel()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        connection = bindFcitxRemoteService(
            BuildConfig.MAIN_APPLICATION_ID,
            onDisconnect = {
                remoteService = null
                cancelJobs()
            }
        ) { service ->
            remoteService = service
            reschedule()
        }
    }

    override fun stop() {
        cancelJobs()
        scope.cancel()
        connection?.let { unbindService(it) }
        connection = null
        remoteService = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        reschedule()
    }

    private fun reschedule() {
        scheduleExport()
        scheduleImport()
    }

    private fun cancelJobs() {
        exportJob?.cancel()
        importJob?.cancel()
        exportJob = null
        importJob = null
    }

    private fun scheduleExport() {
        exportJob?.cancel()
        if (!preferences.exportEnabled || remoteService == null) return
        exportJob = scope.launch {
            while (isActive) {
                val service = remoteService ?: break
                val result = runCatching { performExport(service) }
                result.onSuccess { file ->
                    Log.i(TAG, getString(R.string.log_export_success, file.absolutePath))
                }.onFailure { throwable ->
                    Log.e(TAG, getString(R.string.log_error_format, throwable.message ?: throwable::class.java.simpleName), throwable)
                }
                val delayMillis = preferences.exportIntervalMillis
                delay(delayMillis)
            }
        }
    }

    private fun scheduleImport() {
        importJob?.cancel()
        if (!preferences.importEnabled || remoteService == null) return
        importJob = scope.launch {
            while (isActive) {
                val service = remoteService ?: break
                val result = runCatching { performImport(service) }
                result.onSuccess { file ->
                    if (file != null) {
                        Log.i(TAG, getString(R.string.log_import_success, file.absolutePath))
                    }
                }.onFailure { throwable ->
                    Log.e(TAG, getString(R.string.log_error_format, throwable.message ?: throwable::class.java.simpleName), throwable)
                }
                val delayMillis = preferences.importIntervalMillis
                delay(delayMillis)
            }
        }
    }

    private fun performExport(remote: IFcitxRemoteService): File {
        val file = preferences.exportFile(applicationContext)
        file.parentFile?.mkdirs()
        val descriptor = ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_CREATE or
                ParcelFileDescriptor.MODE_TRUNCATE or
                ParcelFileDescriptor.MODE_WRITE_ONLY
        )
        descriptor.use {
            remote.exportUserData(it, preferences.exportSections().ids().toTypedArray())
        }
        return file
    }

    private fun performImport(remote: IFcitxRemoteService): File? {
        val file = preferences.importFile(applicationContext)
        if (!file.exists()) {
            Log.i(TAG, getString(R.string.log_skipped_missing, file.absolutePath))
            return null
        }
        val descriptor = ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY
        )
        descriptor.use {
            remote.importUserData(it, preferences.importSections().ids().toTypedArray())
        }
        return file
    }

    private companion object {
        private const val TAG = "FcitxDataSync"
    }
}
