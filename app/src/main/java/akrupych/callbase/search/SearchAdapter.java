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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import akrupych.callbase.ActionHandler;
import akrupych.callbase.ExpandableAdapter;
import akrupych.callbase.R;
import akrupych.callbase.model.CallLogEntry;
import akrupych.callbase.model.ContactEntry;
import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class SearchAdapter extends ExpandableAdapter<SearchAdapter.SearchViewHolder> {

    private final Context context;
    private final ActionHandler actionHandler;
    private List<CallLogEntry> callLogResults = new ArrayList<>();
    private List<ContactEntry> contactsResults = new ArrayList<>();

    public SearchAdapter(Context context, ActionHandler actionHandler) {
        this.context = context;
        this.actionHandler= actionHandler;
    }

    public void setData(Cursor callLogCursor, Cursor contactsCursor) {
        Observable.concat(setCallLogData(callLogCursor), setContactsData(contactsCursor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                // do nothing
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        },
                        new Action0() {
                            @Override
                            public void call() {
                                notifyDataSetChanged();
                            }
                        }
                );
    }

    private Observable<?> setCallLogData(Cursor cursor) {
        callLogResults.clear();
        return cursor == null ? Observable.empty() : Observable.just(cursor)
                .observeOn(Schedulers.io())
                .flatMap(new Func1<Cursor, Observable<CallLogEntry>>() {
                    @Override
                    public Observable<CallLogEntry> call(Cursor cursor) {
                        Set<CallLogEntry> uniqueCalls = new LinkedHashSet<>();
                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                            uniqueCalls.add(CallLogEntry.from(cursor));
                        }
                        return Observable.from(uniqueCalls);
                    }
                })
                .toSortedList(new Func2<CallLogEntry, CallLogEntry, Integer>() {
                    @Override
                    public Integer call(CallLogEntry first, CallLogEntry second) {
                        return first.getName().compareTo(second.getName());
                    }
                })
                .map(new Func1<List<CallLogEntry>, List<CallLogEntry>>() {
                    @Override
                    public List<CallLogEntry> call(List<CallLogEntry> callLogEntries) {
                        callLogResults = callLogEntries;
                        return callLogEntries;
                    }
                });
    }

    private Observable<?> setContactsData(Cursor cursor) {
        contactsResults.clear();
        return cursor == null ? Observable.empty() : Observable.just(cursor)
                .observeOn(Schedulers.io())
                .flatMap(new Func1<Cursor, Observable<ContactEntry>>() {
                    @Override
                    public Observable<ContactEntry> call(Cursor cursor) {
                        Set<ContactEntry> uniqueContacts = new LinkedHashSet<>();
                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                            uniqueContacts.add(ContactEntry.from(cursor));
                        }
                        return Observable.from(uniqueContacts);
                    }
                })
                .filter(new Func1<ContactEntry, Boolean>() {
                    @Override
                    public Boolean call(ContactEntry contactEntry) {
                        for (CallLogEntry callLogEntry : callLogResults) {
                            if (contactEntry.getNumber().equals(callLogEntry.getNumber())) {
                                return false;
                            }
                        }
                        return true;
                    }
                })
                .toSortedList(new Func2<ContactEntry, ContactEntry, Integer>() {
                    @Override
                    public Integer call(ContactEntry first, ContactEntry second) {
                        return first.getName().compareTo(second.getName());
                    }
                })
                .map(new Func1<List<ContactEntry>, List<ContactEntry>>() {
                    @Override
                    public List<ContactEntry> call(List<ContactEntry> contactEntries) {
                        contactsResults = contactEntries;
                        return contactEntries;
                    }
                });
    }

    public void clear() {
        callLogResults.clear();
        contactsResults.clear();
        notifyDataSetChanged();
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.call_log_item, parent, false);
        return super.onCreateViewHolder(new SearchViewHolder(view));
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        if (position < callLogResults.size()) {
            holder.bind(callLogResults.get(position), position == getExpandedPosition());
        } else {
            holder.bind(contactsResults.get(position - callLogResults.size()), position == getExpandedPosition());
        }
        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return callLogResults.size() + contactsResults.size();
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

        public void bind(ContactEntry item, boolean expanded) {
            setTextOrHide(nameTextView, item.getName());
            setTextOrHide(numberTextView, item.getNumber());
            setTextOrHide(dateTextView, null);
            setTextOrHide(durationTextView, null);
            setTextOrHide(typeTextView, null);
            setTextOrHide(isNewTextView, null);
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
