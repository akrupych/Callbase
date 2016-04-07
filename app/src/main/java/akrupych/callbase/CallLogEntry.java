package akrupych.callbase;

import android.database.Cursor;
import android.provider.CallLog;

import org.joda.time.DateTime;

public class CallLogEntry {

    private String name;
    private String number;
    private DateTime date;
    private boolean isNew;
    private int duration;
    private int type;

    public static String[] getProjection() {
        return new String[] {
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.NEW,
                CallLog.Calls.DURATION,
                CallLog.Calls.TYPE,
        };
    }

    public static CallLogEntry from(Cursor cursor) {
        CallLogEntry result = new CallLogEntry();
        result.name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
        result.number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
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
}
