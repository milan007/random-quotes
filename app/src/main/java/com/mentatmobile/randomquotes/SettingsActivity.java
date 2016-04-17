package com.mentatmobile.randomquotes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Preference resetDialogPreference = getPreferenceScreen().findPreference("resetDialog");
            resetDialogPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Activity activity = getActivity();
                    Intent startIntent = activity.getIntent();

                    //Both enter and exit animations are set to zero, so no transition animation is applied
                    activity.overridePendingTransition(0, 0);
                    //Call this line, just to make sure that the system doesn't apply an animation
                    getActivity().getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    //Close this Activity
                    activity.finish();
                    //Again, don't set an animation for the transition
                    activity.overridePendingTransition(0, 0);
                    //Start the activity by calling the Intent that have started this same Activity
                    startActivity(startIntent);
                    //Return false, so that nothing happens to the preference values
                    return false;
                }
            });
        }

    }
}
