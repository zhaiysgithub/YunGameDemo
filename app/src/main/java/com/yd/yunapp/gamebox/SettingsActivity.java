package com.yd.yunapp.gamebox;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.kuaipan.game.demo.R;

import kptech.game.kit.BuildConfig;
import kptech.game.kit.env.Env;

public class SettingsActivity extends AppCompatActivity {

    private String corpKey = null;
    SharedPreferences mSp = null;
    boolean envChanged = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        mSp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enableInputCorpKey = mSp.getBoolean("inputCorpKey",false);
        if (enableInputCorpKey){
            corpKey = mSp.getString("editCorpKey","");
        }else {
            corpKey = mSp.getString("corpKey", "");
        }

        mSp.edit().putBoolean("env", Env.isTestEnv()).commit();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public void finish() {
        if (envChanged){
            setResult(102);
        }
        if (mSp != null){
            String key;
            boolean enableInputCorpKey = mSp.getBoolean("inputCorpKey",false);
            if (enableInputCorpKey){
                key = mSp.getString("editCorpKey","");
            }else {
                key = mSp.getString("corpKey", "");
            }
           if (!key.equals(corpKey)){
               setResult(102);
           }
        }

        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            ListPreference listPreference = findPreference("corpKey");
            if (listPreference == null){
                return;
            }
            boolean useSDK2 = BuildConfig.useSDK2;
            if (useSDK2){
                if (Env.isTestEnv()){
                    listPreference.setEntries(R.array.debug_corpkey_entries);
                    listPreference.setEntryValues(R.array.debug_corpkey_values);
                }else {
                    listPreference.setEntries(R.array.release_corpkey_entries);
                    listPreference.setEntryValues(R.array.release_corpkey_values);
                }
            }else {
                if (Env.isTestEnv()){
                    listPreference.setEntries(R.array.corpkey_pass3_key_debug);
                    listPreference.setEntryValues(R.array.corpkey_pass3_value_debug);
                }else {
                    listPreference.setEntries(R.array.corpkey_pass3_key_release);
                    listPreference.setEntryValues(R.array.corpkey_pass3_value_release);
                }

            }

        }

        @Override
        public void onConfigurationChanged(@NonNull Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if(preference.getKey().equals("env") && preference instanceof SwitchPreferenceCompat){
                SwitchPreferenceCompat compat = (SwitchPreferenceCompat)preference;
                if (Env.isTestEnv()){
                    Env.setEnv(getContext(), Env.ENV_RELEASE);
                }else {
                    Env.setEnv(getContext(), Env.ENV_DEBUG);
                }
                ((SettingsActivity)getActivity()).envChanged = true;
            }
            if (preference.getKey().equals("inputCorpKey") && preference instanceof SwitchPreferenceCompat){
                SwitchPreferenceCompat enableInput = (SwitchPreferenceCompat)preference;

                ((SettingsActivity)getActivity()).envChanged = true;
                boolean isChecked = enableInput.isChecked();
                Toast.makeText(getContext(),isChecked ? "您已打开手动输入corpKey" : "您已关闭手动输入corpKey",Toast.LENGTH_SHORT).show();
            }
            return super.onPreferenceTreeClick(preference);
        }

    }
}