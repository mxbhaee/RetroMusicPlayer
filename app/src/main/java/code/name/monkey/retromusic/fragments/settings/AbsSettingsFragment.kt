/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package code.name.monkey.retromusic.fragments.settings

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat
import code.name.monkey.retromusic.activities.OnThemeChangedListener
import code.name.monkey.retromusic.extensions.rootView
import code.name.monkey.retromusic.extensions.safeGetBottomInsets
import code.name.monkey.retromusic.preferences.*
import code.name.monkey.retromusic.util.NavigationUtil

/**
 * @author Hemanth S (h4h13).
 */

abstract class AbsSettingsFragment : ATEPreferenceFragmentCompat() {

    internal fun showProToastAndNavigate(message: String) {
        Toast.makeText(requireContext(), "$message is Pro version feature.", Toast.LENGTH_SHORT)
            .show()
        NavigationUtil.goToProVersion(requireActivity())
    }

    internal fun setSummary(preference: Preference, value: Any?) {
        val stringValue = value.toString()
        if (preference is ListPreference) {
            val index = preference.findIndexOfValue(stringValue)
            preference.setSummary(if (index >= 0) preference.entries[index] else null)
        } else {
            preference.summary = stringValue
        }
    }

    abstract fun invalidateSettings()

    protected fun setSummary(preference: Preference?) {
        preference?.let {
            setSummary(
                it, PreferenceManager
                    .getDefaultSharedPreferences(it.context)
                    .getString(it.key, "")
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDivider(ColorDrawable(Color.TRANSPARENT))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            listView.overScrollMode = View.OVER_SCROLL_NEVER
        }

        // CollapsingToolbarLayout consumes insets and insets are not passed to child views
        // So we get insets from decor view
        // https://github.com/material-components/material-components-android/issues/1310
        ViewCompat.setOnApplyWindowInsetsListener(
            requireActivity().rootView
        ) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            listView.updatePadding(
                left = insets.left,
                bottom = insets.bottom,
                right = insets.right,
            )
            windowInsets
        }
        invalidateSettings()
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        when (preference) {
            is LibraryPreference -> {
                val fragment = LibraryPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }
            is NowPlayingScreenPreference -> {
                val fragment = NowPlayingScreenPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }
            is AlbumCoverStylePreference -> {
                val fragment = AlbumCoverStylePreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }
            is BlacklistPreference -> {
                val fragment = BlacklistPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }
            is DurationPreference -> {
                val fragment = DurationPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

    fun restartActivity() {
        if (activity is OnThemeChangedListener) {
            (activity as OnThemeChangedListener).onThemeValuesChanged()
        } else {
            activity?.recreate()
        }
    }
}
