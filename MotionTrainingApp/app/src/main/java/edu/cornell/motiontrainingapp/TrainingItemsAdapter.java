package edu.cornell.motiontrainingapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * An adapter for the list of values
 */
public class TrainingItemsAdapter extends RecyclerView.Adapter<TrainingItemsAdapter.MyViewHolder> {

    /**
     * The list of values
     */
    private List<InputValue> valuesList;

    public TrainingItemsAdapter(List<InputValue> valuesList) {
        this.valuesList = valuesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.training_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        InputValue value = valuesList.get(position);
        holder.value.setText(value.getStepDescription());
    }

    @Override
    public int getItemCount() {
        return valuesList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView value;

        public MyViewHolder(View view) {
            super(view);
            value = (TextView) view.findViewById(R.id.value);
        }
    }
}
