package akrupych.callbase;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

    private Context context;
    private SharedPreferences preferences;

    public Settings(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean showFullCallLog() {
        return preferences.getBoolean(context.getString(R.string.preference_full_call_log), false);
    }

}
