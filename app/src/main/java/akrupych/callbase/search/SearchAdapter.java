package akrupych.callbase.search;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import akrupych.callbase.ActionHandler;
import akrupych.callbase.ExpandableAdapter;
import akrupych.callbase.R;
import akrupych.callbase.calllog.CallLogEntry;
import butterknife.Bind;
import butterknife.ButterKnife;

public class SearchAdapter extends ExpandableAdapter<SearchAdapter.SearchViewHolder> {

    private final Context context;
    private final ActionHandler actionHandler;
    private List<CallLogEntry> callLogResults;

    public SearchAdapter(Context context, ActionHandler actionHandler) {
        this.context = context;
        this.actionHandler= actionHandler;
    }

    public void setCallLogData(Cursor cursor) {
        if (cursor != null) {
            callLogResults = new ArrayList<>();
            Set<CallLogEntry> uniqueCalls = new LinkedHashSet<>();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                uniqueCalls.add(CallLogEntry.from(cursor));
            }
            callLogResults = new ArrayList<>(uniqueCalls);
            Collections.sort(callLogResults, new Comparator<CallLogEntry>() {
                @Override
                public int compare(CallLogEntry lhs, CallLogEntry rhs) {
                    if (lhs == null || rhs == null) {
                        return 0;
                    } else {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                }
            });
        }
        notifyDataSetChanged();
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.call_log_item, parent, false);
        return super.onCreateViewHolder(new SearchViewHolder(view));
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        holder.bind(callLogResults.get(position), position == getExpandedPosition());
        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return callLogResults == null ? 0 : callLogResults.size();
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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

        public SearchViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            callButton.setOnClickListener(this);
            smsButton.setOnClickListener(this);
            contactButton.setOnClickListener(this);
        }

        public void bind(CallLogEntry item, boolean expanded) {
            bind(nameTextView, item.getName());
            bind(numberTextView, item.getNumber());
            bind(dateTextView, item.getDate().toString());
            bind(durationTextView, item.getDuration() == 0 ? null :
                    PeriodFormat.wordBased(Locale.getDefault()).print(Period.seconds(item.getDuration())));
            bind(typeTextView, item.getTypeString());
            bind(isNewTextView, item.isNew() ? "new" : null);
            actionsPanel.setVisibility(expanded ? View.VISIBLE : View.GONE);
            callButton.setTag(item.getNumber());
            smsButton.setTag(item.getNumber());
            contactButton.setTag(item.getNumber());
        }

        private void bind(TextView textView, String text) {
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
