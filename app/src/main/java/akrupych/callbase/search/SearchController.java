package akrupych.callbase.search;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import java.util.HashMap;
import java.util.Map;

import akrupych.callbase.Constants;

public class SearchController {

    private static final int SEARCH_SOURCES_NUMBER = 2;

    private final Activity activity;
    private Map<Integer, Cursor> searchResults = new HashMap<>();

    public SearchController(Activity activity) {
        this.activity = activity;
    }

    public void clearResults() {
        searchResults.clear();
    }

    public void addResult(int dataKey, Cursor data) {
        searchResults.put(dataKey, data);
    }

    public Cursor getResult(int dataKey) {
        return searchResults.get(dataKey);
    }

    public boolean resultsReady() {
        return searchResults.size() == SEARCH_SOURCES_NUMBER;
    }

    public Loader<Cursor> getSearchCallLogLoader(Bundle args) {
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
                    new String[] {
                            CallLog.Calls.CACHED_NAME,
                            CallLog.Calls.NUMBER,
                            CallLog.Calls.DATE,
                            CallLog.Calls.NEW,
                            CallLog.Calls.DURATION,
                            CallLog.Calls.TYPE,
                    },
                    CallLog.Calls.CACHED_NAME + " LIKE ?",
                    new String[] {"%" + query + "%"},
                    CallLog.Calls.DATE + " DESC");
        }
    }

    public Loader<Cursor> getSearchContactsLoader(Bundle args) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    Constants.REQUEST_READ_CONTACTS);
            return null;
        } else {
            String query = args == null ? "" : args.getString(Constants.ARG_SEARCH_QUERY);
            return new CursorLoader(
                    activity,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[] {
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    },
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?",
                    new String[] {"%" + query + "%"},
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        }
    }
}
