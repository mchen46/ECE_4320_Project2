package edu.cornell.motiontrainingapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Activity with the list of steps for the training sequence
 */
public class TrainingSequence extends AppCompatActivity {

    /**
     * The list of values
     */
    private List<InputValue> valuesList = MainActivity.inputValues;
    /**
     * The list displayed in the activity
     */
    private RecyclerView list;
    /**
     * An adapter for the list of values
     */
    private TrainingItemsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_sequence);

        list = (RecyclerView) findViewById(R.id.training_sequence_list);
        mAdapter = new TrainingItemsAdapter(valuesList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        list.setLayoutManager(mLayoutManager);
        list.setItemAnimator(new DefaultItemAnimator());
        list.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }
}
