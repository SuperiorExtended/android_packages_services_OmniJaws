/*
 * Copyright (C) 2012 The CyanogenMod Project (DvTonder)
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

package org.omnirom.omnijaws;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.CallSuper;
import androidx.preference.EditTextPreference;
import androidx.preference.EditTextPreferenceDialogFragment;

public class CustomLocationPreference extends EditTextPreference implements WeatherLocationTask.Callback {
    private CustomLocationPreferenceDialogFragment mFragment;

    public CustomLocationPreference(Context context) {
        super(context);
    }
    public CustomLocationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CustomLocationPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public CustomLocationPreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        String location = Config.getLocationName(getContext());
        if (location != null) {
            setSummary(location);
        } else {
            setSummary(R.string.weather_custom_location_missing);
        }
    }

    public EditText getEditText() {
        if (mFragment != null) {
            final Dialog dialog = mFragment.getDialog();
            if (dialog != null) {
                return (EditText) dialog.findViewById(android.R.id.edit);
            }
        }
        return null;
    }

    public boolean isDialogOpen() {
        return getDialog() != null && getDialog().isShowing();
    }

    public Dialog getDialog() {
        return mFragment != null ? mFragment.getDialog() : null;
    }

    protected void onPrepareDialogBuilder(AlertDialog.Builder builder,
            DialogInterface.OnClickListener listener) {
    }

    protected void onDialogClosed(boolean positiveResult) {
    }

    protected void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (getEditText().getText().toString().length() > 0) {
                new WeatherLocationTask(getContext(), getEditText().getText().toString(),
                        CustomLocationPreference.this).execute();
            } else {
                Config.setLocationId(getContext(), null);
                Config.setLocationName(getContext(), null);
                setSummary("");
                setText("");
            }
        }
    }

    @CallSuper
    protected void onBindDialogView(View view) {
        final EditText editText = view.findViewById(android.R.id.edit);
        if (editText != null) {
            editText.setInputType(TYPE_CLASS_TEXT);
            editText.requestFocus();

            String location = Config.getLocationName(getContext());
            if (location != null) {
                editText.setText(location);
                editText.setSelection(location.length());
            }
        }
    }

    private void setFragment(CustomLocationPreferenceDialogFragment fragment) {
        mFragment = fragment;
    }

    @Override
    public void applyLocation(WeatherInfo.WeatherLocation result) {
        Config.setLocationId(getContext(), result.id);
        Config.setLocationName(getContext(), result.city);
        setText(result.city);
        setSummary(result.city);
        WeatherUpdateService.scheduleUpdateNow(getContext());
    }

    public static class CustomLocationPreferenceDialogFragment extends EditTextPreferenceDialogFragment {

        public static CustomLocationPreferenceDialogFragment newInstance(String key) {
            final CustomLocationPreferenceDialogFragment fragment = new CustomLocationPreferenceDialogFragment();
            final Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            fragment.setArguments(b);
            return fragment;
        }

        private CustomLocationPreference getCustomizablePreference() {
            return (CustomLocationPreference) getPreference();
        }

        @Override
        protected void onBindDialogView(View view) {
            super.onBindDialogView(view);
            getCustomizablePreference().onBindDialogView(view);
        }

        @Override
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            super.onPrepareDialogBuilder(builder);
            getCustomizablePreference().setFragment(this);
            getCustomizablePreference().onPrepareDialogBuilder(builder, this);
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            super.onDialogClosed(positiveResult);
            getCustomizablePreference().onDialogClosed(positiveResult);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            super.onClick(dialog, which);
            getCustomizablePreference().onClick(dialog, which);
        }
    }
}
