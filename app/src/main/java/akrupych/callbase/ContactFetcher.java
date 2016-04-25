package akrupych.callbase;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.IOException;
import java.io.InputStream;

public class ContactFetcher {

    public static long getContactId(Context context, String number) {
        Uri contactLookup = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor contactLookupCursor = context.getContentResolver().query(contactLookup,
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

    public static Uri getContactUri(Context context, String number) {
        long contactId = getContactId(context, number);
        return ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
    }

    public static Bitmap retrieveContactPhoto(Context context, String number) {
        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(
                context.getContentResolver(), getContactUri(context, number));
        Bitmap contactPhoto = null;
        if (inputStream != null) {
            contactPhoto = BitmapFactory.decodeStream(inputStream);
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (contactPhoto == null) {
            contactPhoto = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        }
        return contactPhoto;
    }
}
