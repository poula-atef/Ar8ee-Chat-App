package com.example.ar8ee.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.example.ar8ee.UI.InnerActivity;
import com.example.ar8ee.R;

public class settingFragment extends PreferenceFragmentCompat implements SharedPreferences .OnSharedPreferenceChangeListener{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.setting_fragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        SwitchPreference switchPreference = findPreference("mode_key");

        changeSwitchPreferenceIcon(switchPreference);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if(preference instanceof SwitchPreference){
            changeSwitchPreferenceIcon(preference);
            startActivity(new Intent(getContext(), InnerActivity.class));
            getActivity().finish();
        }
    }

    private void changeSwitchPreferenceIcon(Preference preference) {
        SwitchPreference switchPreference = (SwitchPreference) preference;
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        boolean mode = sharedPreferences.getBoolean(switchPreference.getKey(),false);
        if(mode){
            switchPreference.setIcon(R.drawable.night_dark_mod);
//            switchPreference.
        }
        else
        {
            switchPreference.setIcon(R.drawable.night_light_mode);
        }
    }


}
