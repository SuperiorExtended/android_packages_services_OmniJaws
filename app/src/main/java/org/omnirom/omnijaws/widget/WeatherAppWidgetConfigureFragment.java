/*
 *  Copyright (C) 2017 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.omnirom.omnijaws.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.omnirom.omnijaws.R;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class WeatherAppWidgetConfigureFragment extends PreferenceFragmentCompat implements OnPreferenceChangeListener {

    public static final String KEY_COLOR_THEME = "color_theme";
    public static final int COLOR_THEME_DEFAULT = 1;
    public static final int COLOR_THEME_LIGHT = 3;
    public static final int COLOR_THEME_DARK = 2;
    public static final int COLOR_THEME_SYSTEM = 1;
    public static final int COLOR_THEME_TRANSPARENT = 0;

    private int mAppWidgetId;
    private ListPreference mColorTheme;

    public WeatherAppWidgetConfigureFragment(int appWidgetId) {
        super();
        mAppWidgetId = appWidgetId;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.weather_appwidget_configure);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int value = prefs.getInt(WeatherAppWidgetConfigureFragment.KEY_COLOR_THEME + "_" + mAppWidgetId, WeatherAppWidgetConfigureFragment.COLOR_THEME_DEFAULT);
        mColorTheme = (ListPreference) findPreference(WeatherAppWidgetConfigureFragment.KEY_COLOR_THEME);
        mColorTheme.setValue(String.valueOf(value));
        int idx = mColorTheme.findIndexOfValue(String.valueOf(value));
        mColorTheme.setSummary(mColorTheme.getEntries()[idx]);
        mColorTheme.setOnPreferenceChangeListener(this);
    }

    public static void clearPrefs(Context context, int id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(KEY_COLOR_THEME + "_" + id).commit();
    }

    public static void remapPrefs(Context context, int oldId, int newId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int oldValue = prefs.getInt(KEY_COLOR_THEME + "_" + oldId, COLOR_THEME_DEFAULT);
        prefs.edit().putInt(KEY_COLOR_THEME + "_" + newId, oldValue).commit();
        prefs.edit().remove(KEY_COLOR_THEME + "_" + oldId).commit();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mColorTheme)) {
            String newTheme = (String) newValue;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            prefs.edit().putInt(WeatherAppWidgetConfigureFragment.KEY_COLOR_THEME + "_" + mAppWidgetId, Integer.valueOf(newTheme)).commit();
            int idx = mColorTheme.findIndexOfValue(newTheme);
            mColorTheme.setSummary(mColorTheme.getEntries()[idx]);
            return true;
        }
        return false;
    }
}
