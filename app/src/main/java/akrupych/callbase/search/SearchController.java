package akrupych.callbase.search;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import akrupych.callbase.Constants;
import akrupych.callbase.calllog.CallLogEntry;

public class SearchController {

    public static Loader<Cursor> getSearchCallLogLoader(Activity activity, Bundle args) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALL_LOG) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_CALL_LOG},
                    Constants.REQUEST_READ_CALL_LOG);
            return null;
        } else {
            String query = args == null ? "" : args.getString(Constants.ARG_SEARCH_QUERY);
            return new CursorLoader(
                    activity,
                    CallLog.Calls.CONTENT_URI,
                    CallLogEntry.getProjection(),
                    CallLog.Calls.CACHED_NAME + " LIKE ?",
                    new String[] {"%" + query + "%"},
                    CallLogEntry.getOrderBy());
        }
    }
}
