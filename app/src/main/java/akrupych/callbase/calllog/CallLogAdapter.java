package akrupych.callbase.calllog;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.util.Locale;

import akrupych.callbase.ActionHandler;
import akrupych.callbase.ContactsGlideLoader;
import akrupych.callbase.ExpandableAdapter;
import akrupych.callbase.R;
import akrupych.callbase.model.CallLogEntry;
import butterknife.Bind;
import butterknife.ButterKnife;

public class CallLogAdapter extends ExpandableAdapter<CallLogAdapter.CallLogViewHolder> {

    protected final Context context;
    protected final ActionHandler actionHandler;
    protected Cursor callLogCursor;
    protected String unknownName;

    public CallLogAdapter(Context context, ActionHandler actionHandler) {
        this.context = context;
        this.actionHandler = actionHandler;
        this.unknownName = context.getString(R.string.unknown);
    }

    public void setData(Cursor cursor) {
        callLogCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public CallLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.callable_item, parent, false);
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

        @Bind(R.id.item_host)
        View itemHost;
        @Bind(R.id.photo)
        ImageView photoImageView;
        @Bind(R.id.call_type)
        ImageView callTypeImageView;
        @Bind(R.id.name)
        TextView nameTextView;
        @Bind(R.id.number)
        TextView numberTextView;
        @Bind(R.id.date)
        TextView dateTextView;
        @Bind(R.id.duration)
        TextView durationTextView;
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
            itemHost.setBackgroundColor(ContextCompat.getColor(context, item.isNew() ?
                    R.color.colorPrimaryTranslucent : android.R.color.transparent));
            Glide.with(context).using(new ContactsGlideLoader(context))
                    .load(item.getNumber())
                    .error(R.mipmap.ic_launcher)
                    .into(photoImageView);
            Glide.with(context).load(getDrawableForCallType(item.getType())).into(callTypeImageView);
            setTextOrDefault(nameTextView, item.getName(), unknownName);
            setTextOrHide(numberTextView, item.getNumber());
            setTextOrHide(dateTextView, item.getDateFormatted());
            setTextOrHide(durationTextView, item.getDuration() == 0 ? null :
                    PeriodFormat.wordBased(Locale.getDefault()).print(Period.seconds(item.getDuration())));
            actionsPanel.setVisibility(expanded ? View.VISIBLE : View.GONE);
            callButton.setTag(item.getNumber());
            smsButton.setTag(item.getNumber());
            contactButton.setTag(item.getNumber());
        }

        private int getDrawableForCallType(int type) {
            switch (type) {
                case CallLog.Calls.INCOMING_TYPE:
                    return R.drawable.incoming_call;
                case CallLog.Calls.OUTGOING_TYPE:
                    return R.drawable.outgoing_call;
                case CallLog.Calls.MISSED_TYPE:
                    return R.drawable.missed_call;
                case CallLog.Calls.VOICEMAIL_TYPE:
                    return R.drawable.voicemail_call;
                default:
                    return 0;
            }
        }

        private void setTextOrDefault(TextView textView, String text, String defaultText) {
            textView.setText(TextUtils.isEmpty(text) ? defaultText : text);
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
