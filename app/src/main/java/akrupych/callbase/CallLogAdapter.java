package akrupych.callbase;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.CallLogViewHolder> {

    private final Cursor callLogCursor;
    private final Context context;

    public CallLogAdapter(Cursor callLogCursor, Context context) {
        this.callLogCursor = callLogCursor;
        this.context = context;
    }

    @Override
    public CallLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout holderView = new LinearLayout(context);
        holderView.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < callLogCursor.getColumnCount(); i++) {
            holderView.addView(new TextView(context));
        }
        return new CallLogViewHolder(holderView);
    }

    @Override
    public void onBindViewHolder(CallLogViewHolder holder, int position) {
        callLogCursor.moveToPosition(position);
        holder.bind(callLogCursor);
    }

    @Override
    public int getItemCount() {
        return callLogCursor.getCount();
    }

    public static class CallLogViewHolder extends RecyclerView.ViewHolder {

        private TextView[] fieldViews;

        public CallLogViewHolder(ViewGroup itemView) {
            super(itemView);
            fieldViews = new TextView[itemView.getChildCount()];
            for (int i = 0; i < fieldViews.length; i++) {
                fieldViews[i] = (TextView) itemView.getChildAt(i);
            }
        }

        public void bind(Cursor cursor) {
            for (int i = 0; i < fieldViews.length; i++) {
                fieldViews[i].setText(String.format("%s: %s", cursor.getColumnName(i), cursor.getString(i)));
            }
        }
    }
}
