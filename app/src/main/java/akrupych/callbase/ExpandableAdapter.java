package akrupych.callbase;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class ExpandableAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T>
        implements View.OnClickListener {

    protected Context context;
    protected ActionHandler actionHandler;
    protected int expandedPosition = -1;

    public ExpandableAdapter(Context context, ActionHandler actionHandler) {
        this.context = context;
        this.actionHandler = actionHandler;
    }

    public T onCreateViewHolder(T holder) {
        holder.itemView.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(T holder, int position) {
        holder.itemView.setTag(position);
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        expandItem(position);
        actionHandler.itemClick(position);
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

    public int getExpandedPosition() {
        return expandedPosition;
    }
}
