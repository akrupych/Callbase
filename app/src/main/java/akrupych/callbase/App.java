package akrupych.callbase;

import android.app.Application;

public class App extends Application {

    private static App instance;

    private Settings settings;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        settings = new Settings(this);
    }

    public Settings getSettings() {
        return settings;
    }
}
