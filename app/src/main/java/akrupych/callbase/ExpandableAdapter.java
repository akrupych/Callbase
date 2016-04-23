package akrupych.callbase;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class ExpandableAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T>
        implements View.OnClickListener {

    protected int expandedPosition = -1;

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

    public int getExpandedPosition() {
        return expandedPosition;
    }
}
