package akrupych.callbase;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.CallLogViewHolder> implements View.OnClickListener {

    private Cursor callLogCursor;
    private MainActivity activity;
    private int expandedPosition = -1;

    public CallLogAdapter(Cursor callLogCursor, MainActivity activity) {
        this.callLogCursor = callLogCursor;
        this.activity = activity;
    }

    @Override
    public CallLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.call_log_item, parent, false);
        view.setOnClickListener(this);
        return new CallLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CallLogViewHolder holder, int position) {
        callLogCursor.moveToPosition(position);
        holder.itemView.setTag(position);
        holder.bind(CallLogEntry.from(callLogCursor), position == expandedPosition);
    }

    @Override
    public int getItemCount() {
        return callLogCursor.getCount();
    }

    @Override
    public void onClick(View v) {
        expandItem((Integer) v.getTag());
    }

    public void expandItem(int position) {
        if (position == expandedPosition) {
            expandedPosition = -1;
            notifyItemChanged(position);
        } else {
            notifyItemChanged(expandedPosition);
            expandedPosition = position;
            notifyItemChanged(expandedPosition);
        }
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
            nameTextView.setText(item.getName());
            numberTextView.setText(item.getNumber());
            dateTextView.setText(item.getDate().toString());
            durationTextView.setText(PeriodFormat.wordBased(Locale.getDefault())
                    .print(Period.seconds(item.getDuration())));
            typeTextView.setText(item.getTypeString());
            isNewTextView.setText(String.valueOf(item.isNew()));
            actionsPanel.setVisibility(expanded ? View.VISIBLE : View.GONE);
            callButton.setTag(item.getNumber());
            smsButton.setTag(item.getNumber());
            contactButton.setTag(item.getNumber());
        }

        @Override
        public void onClick(View v) {
            String number = (String) v.getTag();
            switch (v.getId()) {
                case R.id.call_button:
                    activity.call(number);
                    break;
                case R.id.sms_button:
                    activity.typeSms(number);
                    break;
                case R.id.contact_button:
                    activity.openContact(number);
                    break;
            }
        }
    }
}
