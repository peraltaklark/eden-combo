// SPDX-FileCopyrightText: Copyright 2026 Eden Emulator Project
// SPDX-License-Identifier: GPL-3.0-or-later

package org.yuzu.yuzu_emu.utils

import android.content.Context
import androidx.preference.PreferenceManager
import org.yuzu.yuzu_emu.features.input.NativeInput
import org.yuzu.yuzu_emu.features.input.model.NativeButton

/**
 * Combo button support. Each combo button, when pressed, fires N
 * onOverlayButtonEvent calls against the same overlay port the normal
 * overlay buttons use, so the game sees all configured buttons go down
 * (and up) in the same frame.
 *
 * Buttons are stored as a comma-separated string of NativeButton ints, e.g.
 * "6,7" means L+R.
 */
object ComboHelper {
    const val COMBO_COUNT = 5

    /** Display order and human-readable labels for the multi-choice picker. */
    val SELECTABLE_BUTTONS: List<Pair<NativeButton, String>> = listOf(
        NativeButton.A to "A",
        NativeButton.B to "B",
        NativeButton.X to "X",
        NativeButton.Y to "Y",
        NativeButton.L to "L",
        NativeButton.R to "R",
        NativeButton.ZL to "ZL",
        NativeButton.ZR to "ZR",
        NativeButton.Plus to "Plus",
        NativeButton.Minus to "Minus",
        NativeButton.Home to "Home",
        NativeButton.Capture to "Capture",
        NativeButton.DUp to "D-Pad Up",
        NativeButton.DDown to "D-Pad Down",
        NativeButton.DLeft to "D-Pad Left",
        NativeButton.DRight to "D-Pad Right",
        NativeButton.LStick to "L Stick",
        NativeButton.RStick to "R Stick"
    )

    private const val KEY_ENABLED = "combo_enabled_"
    private const val KEY_BUTTONS = "combo_buttons_"

    // Defaults: combo 0 = L+R, combo 1 = A+B, combo 2 = X+Y, combo 3 = ZL+ZR, combo 4 = empty
    private val DEFAULT_BUTTONS = arrayOf(
        intArrayOf(NativeButton.L.int, NativeButton.R.int),
        intArrayOf(NativeButton.A.int, NativeButton.B.int),
        intArrayOf(NativeButton.X.int, NativeButton.Y.int),
        intArrayOf(NativeButton.ZL.int, NativeButton.ZR.int),
        intArrayOf()
    )

    fun isEnabled(context: Context, index: Int): Boolean {
        if (index < 0 || index >= COMBO_COUNT) return false
        // Combos 1-3 enabled by default, 4-5 need manual enable
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_ENABLED + index, false)
    }

    fun setEnabled(context: Context, index: Int, enabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putBoolean(KEY_ENABLED + index, enabled).apply()
    }

    /** Returns the list of buttons currently assigned to the given combo. */
    fun getButtons(context: Context, index: Int): List<NativeButton> {
        if (index < 0 || index >= COMBO_COUNT) return emptyList()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val key = KEY_BUTTONS + index
        if (!prefs.contains(key)) {
            // Seed defaults on first read
            val csv = DEFAULT_BUTTONS[index].joinToString(",")
            prefs.edit().putString(key, csv).apply()
            return DEFAULT_BUTTONS[index].map { NativeButton.from(it) }
        }
        val csv = prefs.getString(key, "") ?: ""
        return csv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .map { NativeButton.from(it) }
    }

    fun setButtons(context: Context, index: Int, buttons: List<NativeButton>) {
        if (index < 0 || index >= COMBO_COUNT) return
        val csv = buttons.joinToString(",") { it.int.toString() }
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putString(KEY_BUTTONS + index, csv).apply()
    }

    /** Human-readable summary for a button list, or "None" if empty. */
    fun describeButtons(buttons: List<NativeButton>): String {
        if (buttons.isEmpty()) return "None"
        return buttons.joinToString(" + ") { btn ->
            SELECTABLE_BUTTONS.firstOrNull { it.first == btn }?.second ?: btn.name
        }
    }

    fun comboActivate(context: Context, playerIndex: Int, buttonStatus: Int, comboIndex: Int) {
        for (btn in getButtons(context, comboIndex)) {
            NativeInput.onOverlayButtonEvent(playerIndex, btn, buttonStatus)
        }
    }
}
