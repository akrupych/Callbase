package akrupych.callbase;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import akrupych.callbase.calllog.CallLogAdapter;
import akrupych.callbase.calllog.CallLogController;
import akrupych.callbase.calllog.ContractedCallLogAdapter;
import akrupych.callbase.search.SearchAdapter;
import akrupych.callbase.search.SearchController;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ActionHandler,
        LoaderManager.LoaderCallbacks<Cursor> {

    private enum Mode {
        CALL_LOG,
        SEARCH
    }

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.call_log) RecyclerView recyclerView;

    private CallLogController callLogController = new CallLogController(this);
    private CallLogAdapter callLogAdapter = new CallLogAdapter(this, this);

    private SearchController searchController = new SearchController(this);
    private SearchAdapter searchAdapter = new SearchAdapter(this, this);

    private Mode currentMode = Mode.CALL_LOG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        ButterKnife.bind(this);
        loadCallLog();
    }

    private void loadCallLog() {
        callLogAdapter = App.getInstance().getSettings().showFullCallLog() ?
                new CallLogAdapter(this, this) : new ContractedCallLogAdapter(this, this);
        setMode(Mode.CALL_LOG);
        load(Constants.LOADER_CALL_LOG, null);
    }

    private void setMode(Mode newMode) {
        currentMode = newMode;
        switch (currentMode) {
            case CALL_LOG:
                recyclerView.setAdapter(callLogAdapter);
                break;
            case SEARCH:
                recyclerView.setAdapter(searchAdapter);
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case Constants.LOADER_CALL_LOG:
                return callLogController.getCallLogLoader();
            case Constants.LOADER_SEARCH_CALL_LOG:
                return searchController.getSearchCallLogLoader(args);
            case Constants.LOADER_SEARCH_CONTACTS:
                return searchController.getSearchContactsLoader(args);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case Constants.LOADER_CALL_LOG:
                callLogAdapter.setData(data);
                break;
            case Constants.LOADER_SEARCH_CALL_LOG:
                searchController.addResult(Constants.LOADER_SEARCH_CALL_LOG, data);
                trySetSearchData();
                break;
            case Constants.LOADER_SEARCH_CONTACTS:
                searchController.addResult(Constants.LOADER_SEARCH_CONTACTS, data);
                trySetSearchData();
                break;
        }
    }

    private void trySetSearchData() {
        if (searchController.resultsReady()) {
            searchAdapter.setData(
                    searchController.getResult(Constants.LOADER_SEARCH_CALL_LOG),
                    searchController.getResult(Constants.LOADER_SEARCH_CONTACTS)
            );
            searchController.clearResults();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case Constants.LOADER_CALL_LOG:
                callLogAdapter.setData(null);
                break;
            case Constants.LOADER_SEARCH_CALL_LOG:
                searchAdapter.clear();
                break;
            case Constants.LOADER_SEARCH_CONTACTS:
                searchAdapter.clear();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchMenuItem = menu.findItem( R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d(TAG, "onMenuItemActionCollapse " + item.getTitle());
                setMode(Mode.CALL_LOG);
                return true;
            }
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d(TAG, "onMenuItemActionExpand " + item.getTitle());
                setMode(Mode.SEARCH);
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit " + query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "onQueryTextChange " + s);
                if (TextUtils.isEmpty(s)) {
                    searchAdapter.clear();
                } else {
                    load(Constants.LOADER_SEARCH_CALL_LOG, s);
                    load(Constants.LOADER_SEARCH_CONTACTS, s);
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSettings() {
        startActivityForResult(new Intent(this, SettingsActivity.class), Constants.REQUEST_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_SETTINGS:
                if (resultCode == Activity.RESULT_OK && currentMode == Mode.CALL_LOG) {
                    loadCallLog();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_READ_CALL_LOG:
                load(Constants.LOADER_CALL_LOG, null);
                break;
        }
    }

    @Override
    public void call(String number) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.CALL_PHONE},
                    Constants.REQUEST_CALL_PHONE);
        } else {
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number)));
        }
    }

    @Override
    public void sms(String number) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number)));
    }

    @Override
    public void openContact(String number) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_CONTACTS},
                    Constants.REQUEST_READ_CONTACTS);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_URI, String.valueOf(getContactId(number)))));
        }
    }

    private long getContactId(String number) {
        Uri contactLookup = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor contactLookupCursor = getContentResolver().query(contactLookup,
                new String[]{ContactsContract.PhoneLookup._ID}, null, null, null);
        assert contactLookupCursor != null;
        if (contactLookupCursor.moveToNext()) {
            long ret = contactLookupCursor.getLong(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            contactLookupCursor.close();
            return ret;
        }
        contactLookupCursor.close();
        return 0;
    }

    private void load(int loaderId, @Nullable String query) {
        Bundle args = null; {
            if (query != null) {
                args = new Bundle();
                args.putString(Constants.ARG_SEARCH_QUERY, query);
            }
        }
        getSupportLoaderManager().restartLoader(loaderId, args, this);
    }
}
