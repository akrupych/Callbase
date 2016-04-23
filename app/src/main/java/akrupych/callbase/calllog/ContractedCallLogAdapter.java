package akrupych.callbase.calllog;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import akrupych.callbase.ActionHandler;
import akrupych.callbase.model.CallLogEntry;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ContractedCallLogAdapter extends CallLogAdapter {

    private List<CallLogEntry> items = new ArrayList<>();

    public ContractedCallLogAdapter(Context context, ActionHandler actionHandler) {
        super(context, actionHandler);
    }

    @Override
    public void setData(Cursor cursor) {
        items.clear();
        if (cursor != null) {
            Observable.just(cursor)
                    .subscribeOn(Schedulers.io())
                    .flatMap(new Func1<Cursor, Observable<List<CallLogEntry>>>() {
                        @Override
                        public Observable<List<CallLogEntry>> call(Cursor cursor) {
                            Set<CallLogEntry> uniqueCalls = new LinkedHashSet<>();
                            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                                uniqueCalls.add(CallLogEntry.from(cursor));
                            }
                            List<CallLogEntry> resultList = new ArrayList<>(uniqueCalls);
                            return Observable.just(resultList);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Action1<List<CallLogEntry>>() {
                                @Override
                                public void call(List<CallLogEntry> callLogEntries) {
                                    items.addAll(callLogEntries);
                                    notifyDataSetChanged();
                                }
                            },
                            new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                            }
                    );
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(CallLogViewHolder holder, int position) {
        holder.bind(items.get(position), position == expandedPosition);
        holder.itemView.setTag(position);
    }
}
