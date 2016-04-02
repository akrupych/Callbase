package akrupych.callbase;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readCallLog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        readCallLog();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void readCallLog() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CALL_LOG},
                    Constants.REQUEST_READ_CALL_LOG);
        } else {
            Cursor callsCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
            if (callsCursor != null) {
                Log.d(TAG, callsCursor.getCount() + " calls available");
                callsCursor.close();
            }
        }
    }
}
