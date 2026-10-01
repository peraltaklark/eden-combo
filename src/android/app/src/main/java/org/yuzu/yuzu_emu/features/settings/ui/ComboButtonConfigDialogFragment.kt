// SPDX-FileCopyrightText: Copyright 2026 Eden Emulator Project
// SPDX-License-Identifier: GPL-3.0-or-later

package org.yuzu.yuzu_emu.features.settings.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import androidx.fragment.app.DialogFragment
import org.yuzu.yuzu_emu.R
import org.yuzu.yuzu_emu.features.input.model.NativeButton
import org.yuzu.yuzu_emu.utils.ComboHelper

class ComboButtonConfigDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val ctx = requireContext()
        val root = LayoutInflater.from(ctx).inflate(R.layout.dialog_combo_buttons, null)

        val switchIds = intArrayOf(
            R.id.switch_combo_1, R.id.switch_combo_2, R.id.switch_combo_3,
            R.id.switch_combo_4, R.id.switch_combo_5
        )
        val buttonIds = intArrayOf(
            R.id.btn_combo_1, R.id.btn_combo_2, R.id.btn_combo_3,
            R.id.btn_combo_4, R.id.btn_combo_5
        )

        val switches = switchIds.map { root.findViewById<MaterialSwitch>(it) }
        val buttons = buttonIds.map { root.findViewById<MaterialButton>(it) }

        // Local working copy so Cancel doesn't save
        val workingButtons: Array<MutableList<NativeButton>> = Array(ComboHelper.COMBO_COUNT) { i ->
            ComboHelper.getButtons(ctx, i).toMutableList()
        }

        for (i in 0 until ComboHelper.COMBO_COUNT) {
            switches[i].isChecked = ComboHelper.isEnabled(ctx, i)
            buttons[i].text = ComboHelper.describeButtons(workingButtons[i])
            val index = i
            buttons[i].setOnClickListener {
                showMultiChoicePicker(ctx, index, workingButtons[index]) {
                    buttons[index].text = ComboHelper.describeButtons(workingButtons[index])
                }
            }
        }

        return MaterialAlertDialogBuilder(ctx)
            .setTitle(R.string.combo_button_settings)
            .setView(root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                for (i in 0 until ComboHelper.COMBO_COUNT) {
                    ComboHelper.setEnabled(ctx, i, switches[i].isChecked)
                    ComboHelper.setButtons(ctx, i, workingButtons[i])
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.reset_to_default) { _, _ ->
                for (i in 0 until ComboHelper.COMBO_COUNT) {
                    ComboHelper.setEnabled(ctx, i, true)
                }
                // Wipe the stored button lists so next read reseeds from defaults.
                // Do this via setButtons with an empty list then re-read.
                for (i in 0 until ComboHelper.COMBO_COUNT) {
                    ComboHelper.setButtons(ctx, i, emptyList())
                }
            }
            .create()
    }

    /**
     * Nested multi-choice dialog. Shows every selectable button with a checkbox.
     * On OK, writes the picked list back into [target] and calls [onUpdate].
     */
    private fun showMultiChoicePicker(
        ctx: Context,
        comboIndex: Int,
        target: MutableList<NativeButton>,
        onUpdate: () -> Unit
    ) {
        val entries = ComboHelper.SELECTABLE_BUTTONS.map { it.second }.toTypedArray()
        val checked = BooleanArray(entries.size) { i ->
            target.contains(ComboHelper.SELECTABLE_BUTTONS[i].first)
        }

        MaterialAlertDialogBuilder(ctx)
            .setTitle(getString(R.string.combo_button_n, comboIndex + 1))
            .setMultiChoiceItems(entries, checked) { _, which, isChecked ->
                val btn = ComboHelper.SELECTABLE_BUTTONS[which].first
                if (isChecked) {
                    if (!target.contains(btn)) target.add(btn)
                } else {
                    target.remove(btn)
                }
            }
            .setPositiveButton(android.R.string.ok) { _, _ -> onUpdate() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        const val TAG = "ComboButtonConfigDialogFragment"
    }
}
