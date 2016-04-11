package akrupych.callbase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import akrupych.callbase.search.SearchAdapter;
import akrupych.callbase.search.SearchController;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ActionHandler,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.call_log)
    RecyclerView recyclerView;

    private CallLogAdapter callLogAdapter = new CallLogAdapter(this, this);
    private SearchAdapter searchAdapter = new SearchAdapter(this, this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        recyclerView.setAdapter(callLogAdapter);
        load(Constants.LOADER_CALL_LOG, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case Constants.LOADER_CALL_LOG:
                return CallLogController.getCallLogLoader(this);
            case Constants.LOADER_SEARCH_CALL_LOG:
                return SearchController.getSearchCallLogLoader(this, args);
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
                searchAdapter.setCallLogData(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case Constants.LOADER_CALL_LOG:
                callLogAdapter.setData(null);
                break;
            case Constants.LOADER_SEARCH_CALL_LOG:
                searchAdapter.setCallLogData(null);
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
                recyclerView.setAdapter(callLogAdapter);
                return true;
            }
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d(TAG, "onMenuItemActionExpand " + item.getTitle());
                recyclerView.setAdapter(searchAdapter);
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
                    searchAdapter.setCallLogData(null);
                } else {
                    load(Constants.LOADER_SEARCH_CALL_LOG, s);
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
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
