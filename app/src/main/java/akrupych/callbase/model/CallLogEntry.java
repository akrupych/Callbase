package akrupych.callbase.model;

import android.database.Cursor;
import android.provider.CallLog;
import android.text.format.DateUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Locale;

public class CallLogEntry {

    private String name;
    private String number;
    private DateTime date;
    private boolean isNew;
    private int duration;
    private int type;

    public static CallLogEntry from(Cursor cursor) {
        CallLogEntry result = new CallLogEntry();
        result.name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
        try {
            result.number = PhoneNumberUtil.getInstance().format(
                    PhoneNumberUtil.getInstance().parse(cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)), "UA"),
                    PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        result.date = new DateTime(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
        result.isNew = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.NEW)) != 0;
        result.duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));
        result.type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
        return result;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public DateTime getDate() {
        return date;
    }

    public String getDateFormatted() {
        if (date.isAfter(DateTime.now().minusDays(1).withTimeAtStartOfDay())) {
            // today or yesterday
            return DateUtils.getRelativeTimeSpanString(date.getMillis(), System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString() + ", " +
                    DateTimeFormat.shortTime().print(date);
        } else {
            return DateTimeFormat.forPattern("EEEE, " + DateTimeFormat.patternForStyle("SS", Locale.getDefault()))
                    .withLocale(Locale.getDefault()).print(date);
        }
    }

    public boolean isNew() {
        return isNew;
    }

    public int getDuration() {
        return duration;
    }

    public int getType() {
        return type;
    }

    public String getTypeString() {
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE:
                return "INCOMING_TYPE";
            case CallLog.Calls.OUTGOING_TYPE:
                return "OUTGOING_TYPE";
            case CallLog.Calls.MISSED_TYPE:
                return "MISSED_TYPE";
            case CallLog.Calls.VOICEMAIL_TYPE:
                return "VOICEMAIL_TYPE";
            default:
                return "UNKNOWN_TYPE";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CallLogEntry)) return false;

        CallLogEntry that = (CallLogEntry) o;

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
