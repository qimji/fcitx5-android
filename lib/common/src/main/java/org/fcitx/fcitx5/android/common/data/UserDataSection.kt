/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.common.data

/**
 * Represents top-level directories of user data that can be synchronised via plugins.
 */
enum class UserDataSection(val id: String) {
    SHARED_PREFS("shared_prefs"),
    DATABASES("databases"),
    EXTERNAL("external"),
    RECENTLY_USED("recently_used");

    companion object {
        fun fromId(id: String): UserDataSection? = entries.firstOrNull { it.id == id }
    }
}

fun Collection<UserDataSection>.ids(): Set<String> = mapTo(mutableSetOf()) { it.id }
