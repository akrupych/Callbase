package akrupych.callbase.model;

import android.database.Cursor;
import android.provider.ContactsContract;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class ContactEntry {

    private String name;
    private String number;

    public static ContactEntry from(Cursor cursor) {
        ContactEntry result = new ContactEntry();
        result.name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        try {
            result.number = PhoneNumberUtil.getInstance().format(
                    PhoneNumberUtil.getInstance().parse(
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                            "UA"),
                    PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactEntry)) return false;

        ContactEntry that = (ContactEntry) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return number != null ? number.equals(that.number) : that.number == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (number != null ? number.hashCode() : 0);
        return result;
    }
}
