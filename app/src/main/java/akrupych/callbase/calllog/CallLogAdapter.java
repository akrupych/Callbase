package akrupych.callbase.calllog;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.util.Locale;

import akrupych.callbase.ActionHandler;
import akrupych.callbase.ExpandableAdapter;
import akrupych.callbase.R;
import akrupych.callbase.model.CallLogEntry;
import butterknife.Bind;
import butterknife.ButterKnife;

public class CallLogAdapter extends ExpandableAdapter<CallLogAdapter.CallLogViewHolder> {

    private final Context context;
    private final ActionHandler actionHandler;
    private Cursor callLogCursor;
    private int expandedPosition = -1;

    public CallLogAdapter(Context context, ActionHandler actionHandler) {
        this.context = context;
        this.actionHandler= actionHandler;
    }

    public void setData(Cursor cursor) {
        callLogCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public CallLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.call_log_item, parent, false);
        return super.onCreateViewHolder(new CallLogViewHolder(view));
    }

    @Override
    public void onBindViewHolder(CallLogViewHolder holder, int position) {
        callLogCursor.moveToPosition(position);
        holder.bind(CallLogEntry.from(callLogCursor), position == expandedPosition);
        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return callLogCursor == null ? 0 : callLogCursor.getCount();
    }

    public class CallLogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.name)
        TextView nameTextView;
        @Bind(R.id.number)
        TextView numberTextView;
        @Bind(R.id.date)
        TextView dateTextView;
        @Bind(R.id.duration)
        TextView durationTextView;
        @Bind(R.id.type)
        TextView typeTextView;
        @Bind(R.id.is_new)
        TextView isNewTextView;
        @Bind(R.id.actions_panel)
        View actionsPanel;
        @Bind(R.id.call_button)
        View callButton;
        @Bind(R.id.sms_button)
        View smsButton;
        @Bind(R.id.contact_button)
        View contactButton;

        public CallLogViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            callButton.setOnClickListener(this);
            smsButton.setOnClickListener(this);
            contactButton.setOnClickListener(this);
        }

        public void bind(CallLogEntry item, boolean expanded) {
            setTextOrHide(nameTextView, item.getName());
            setTextOrHide(numberTextView, item.getNumber());
            setTextOrHide(dateTextView, item.getDateFormatted());
            setTextOrHide(durationTextView, item.getDuration() == 0 ? null :
                    PeriodFormat.wordBased(Locale.getDefault()).print(Period.seconds(item.getDuration())));
            setTextOrHide(typeTextView, item.getTypeString());
            setTextOrHide(isNewTextView, item.isNew() ? "new" : null);
            actionsPanel.setVisibility(expanded ? View.VISIBLE : View.GONE);
            callButton.setTag(item.getNumber());
            smsButton.setTag(item.getNumber());
            contactButton.setTag(item.getNumber());
        }

        private void setTextOrHide(TextView textView, String text) {
            textView.setText(text);
            textView.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onClick(View v) {
            String number = (String) v.getTag();
            switch (v.getId()) {
                case R.id.call_button:
                    actionHandler.call(number);
                    break;
                case R.id.sms_button:
                    actionHandler.sms(number);
                    break;
                case R.id.contact_button:
                    actionHandler.openContact(number);
                    break;
            }
        }
    }
}
