package akrupych.callbase.calllog;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import akrupych.callbase.Constants;

public class CallLogFetcher {

    private Activity activity;

    public CallLogFetcher(Activity activity) {
        this.activity = activity;
    }

    public Loader<Cursor> getCallLogLoader() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALL_LOG) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_CALL_LOG},
                    Constants.REQUEST_READ_CALL_LOG);
            return null;
        } else {
            return new CursorLoader(
                    activity,
                    CallLog.Calls.CONTENT_URI,
                    new String[] {
                            CallLog.Calls.CACHED_NAME,
                            CallLog.Calls.NUMBER,
                            CallLog.Calls.DATE,
                            CallLog.Calls.NEW,
                            CallLog.Calls.DURATION,
                            CallLog.Calls.TYPE,
                    },
                    null,
                    null,
                    CallLog.Calls.DATE + " DESC");
        }
    }

    public void clearNewCalls() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALL_LOG) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_CALL_LOG},
                    Constants.REQUEST_WRITE_CALL_LOG);
        } else {
            ContentValues update = new ContentValues();
            update.put(CallLog.Calls.NEW, 0);
            activity.getContentResolver().update(CallLog.Calls.CONTENT_URI, update, null, null);
        }
    }
}
