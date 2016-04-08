package akrupych.callbase;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.call_log)
    RecyclerView callLogRecyclerView;
    private Cursor callLogCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        readCallLog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_READ_CALL_LOG:
                readCallLog();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (callLogCursor != null) {
            callLogCursor.close();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void readCallLog() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALL_LOG},
                    Constants.REQUEST_READ_CALL_LOG);
        } else {
            callLogCursor = getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    CallLogEntry.getProjection(),
                    null,
                    null,
                    CallLogEntry.getOrderBy());
            if (callLogCursor != null) {
                showCallLog();
            }
        }
    }

    private void showCallLog() {
        callLogRecyclerView.setAdapter(new CallLogAdapter(callLogCursor, this));
    }

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

    public void typeSms(String number) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number)));
    }

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
}
