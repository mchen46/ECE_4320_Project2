package edu.cornell.motiontrainingapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import au.com.bytecode.opencsv.CSVReader;

public class MainActivity extends AppCompatActivity implements SensorEventListener,
        OnChartValueSelectedListener {

    /**
     * The list of input values
     */
    public static ArrayList<InputValue> inputValues = new ArrayList<>();
    /**
     * File code used when loading the file
     */
    private static int FILE_CODE = 2;
    /**
     * The current value working with
     */
    private InputValue currentValue;
    /**
     * Used to decide when to start the check for the training sequence and when to stop
     */
    private boolean startCheck = false;
    /**
     * The total time the condition is met
     */
    private long totalTimeConditionIsMet = 0;
    /**
     * The last time the condition was met
     */
    private long lastTimeConditionWasMet = 0;
    /**
     * Condition is met value
     */
    private boolean conditionMet = false;
    /**
     * Calendar used to get time values
     */
    private Calendar calendar;
    /**
     * The Ax chart
     */
    private LineChart chartAx;
    /**
     * The Ay chart
     */
    private LineChart chartAy;
    /**
     * The Az chart
     */
    private LineChart chartAz;
    /**
     * The Wx chart
     */
    private LineChart chartWx;
    /**
     * The Wy chart
     */
    private LineChart chartWy;
    /**
     * The Wz chart
     */
    private LineChart chartWz;
    /**
     * The start button
     */
    private Button startButton;
    /**
     * The stop button
     */
    private Button stopButton;
    /**
     * The add file button
     */
    private Button addFileButton;
    /**
     * The training sequence button
     */
    private Button trainingSequence;
    /**
     * Sensor manager used to read values from the device
     */
    private SensorManager mSensorManager;
    /**
     * Accelerometer sensor
     */
    private Sensor mAccelerometerSensor;
    /**
     * Gyroscope sensor
     */
    private Sensor mGyroscopeSensor;
    /**
     * TextView with the file path and name
     */
    private TextView fileTextName;
    /**
     * TextView with the instruction for the current step
     */
    private TextView instruction;
    /**
     * Gravity values array
     */
    private float[] gravity = new float[3];
    /**
     * Acceleration values array
     */
    private float[] linear_acceleration = new float[3];
    /**
     * Used to stop the process
     */
    private boolean stopProcess = false;
    /**
     * The total accelerometer time
     */
    private float totalAccelerometerTime = 0;
    /**
     * The total gyroscope time
     */
    private float totalGyroscopeTime = 0;
    /**
     * The file location
     */
    private String fileLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creating references to the UI elements like texts and buttons
        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);
        trainingSequence = (Button) findViewById(R.id.training);
        addFileButton = (Button) findViewById(R.id.add_file_button);
        fileTextName = (TextView) findViewById(R.id.file_name_text);
        instruction = (TextView) findViewById(R.id.instruction);

        //Set click listener for the start button
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
        //Set click listener for the stop button
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopProcess = true;
                startCheck = false;
            }
        });
        //Set click listener for the AddFileButton
        addFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create an intent to open a file manager to get the file
                Intent i = new Intent(MainActivity.this, FilePickerActivity.class);
                // Set these depending on your use case. These are the defaults.
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                // Configure initial directory by specifying a String.
                // You could specify a String like "/storage/emulated/0/", but that can
                // dangerous. Always use Android's API calls to get paths to the SD-card or
                // internal memory.
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                startActivityForResult(i, FILE_CODE);
            }
        });
        //Set click listener for the training sequence button
        trainingSequence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Stop the process first and then create an intent to start that activity
                stopProcess = true;
                Intent i = new Intent(MainActivity.this, TrainingSequence.class);
                startActivity(i);
            }
        });
        //Create the sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //Set the file location path
        fileLocation = getFileLocationFromPreferences();
        if (fileLocation != null) {
            //If it's not null then we already loaded a file before so we load it again
            fileTextName.setText(fileLocation);
            loadValuesFromFile(fileLocation);
        }
        //Creating UI references to the charts
        chartAx = (LineChart) findViewById(R.id.chartAx);
        chartAy = (LineChart) findViewById(R.id.chartAy);
        chartAz = (LineChart) findViewById(R.id.chartAz);
        chartWx = (LineChart) findViewById(R.id.chartWx);
        chartWy = (LineChart) findViewById(R.id.chartWy);
        chartWz = (LineChart) findViewById(R.id.chartWz);
        createCharts();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //The sensor event is of Acceleration type so we read that value
            getAccelerometer(event);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            //The sensor event is of Gyroscope type so we read that value
            getGyroscope(event);
        }
    }

    /**
     * Add a new entry in a chart
     *
     * @param chart       The chart
     * @param value       The Y value in the chart
     * @param time        The X value in the chart in seconds
     * @param description The description for the dataset
     */
    private void addEntry(LineChart chart, float value, float time, String description) {
        LineData data = chart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet(description);
                data.addDataSet(set);
            }

            data.addEntry(new Entry(time, value), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(10);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }

    }

    /**
     * Get the accelerometer values from an event
     *
     * @param event
     */
    private void getAccelerometer(SensorEvent event) {
        final float alpha = 0.8f;
        //Calculate the gravity for every sensor value
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
        //Get the aceleration from the event
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];
        // Create x,y,z values to be more readable
        float x = linear_acceleration[0];
        float y = linear_acceleration[1];
        float z = linear_acceleration[2];

        if (!startCheck) {
            //it's not yet time to check the values so we just exit the function
            return;
        }
        Log.d("Acceleration", "x: " + x + ", y: " + y + ", z: " + z);
        //Get a new instance for the calendar
        calendar = Calendar.getInstance();
        if (totalAccelerometerTime % 3 == 0) {
            //Add the values to their respective chart
            addEntry(chartAx, x, totalAccelerometerTime / 1000, "Ax");
            addEntry(chartAy, y, totalAccelerometerTime / 1000, "Ay");
            addEntry(chartAz, z, totalAccelerometerTime / 1000, "Az");
        }

        //if we have a condition for the value Ax
        if (currentValue.getAxCondition() != 0) {
            //if the condition is smaller than 0
            if (currentValue.getAxCondition() < 0) {
                //verify if the current read value is smaller than 0
                if (x < currentValue.getAx()) {
                    //if yes we check when was the last time the condition was met
                    //if it's 0 then it was never met or was reset
                    if (lastTimeConditionWasMet == 0) {
                        //so we save the time when the condition was met
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    } else {
                        //if it's not 0 then we add the time between now and the last time the
                        //condition was met
                        //and set the last time the condition was met to now
                        totalTimeConditionIsMet += calendar.getTimeInMillis() - lastTimeConditionWasMet;
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    }
                } else {
                    //if it's not then we reset the last time condition was met
                    lastTimeConditionWasMet = 0;
                }
            } else {
                //it's bigger than 0 so we verify if current x is bigger than the condition
                if (x > currentValue.getAx()) {
                    if (lastTimeConditionWasMet == 0) {
                        //so we save the time when the condition was met
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    } else {
                        //if it's not 0 then we add the time between now and the last time the
                        //condition was met
                        //and set the last time the condition was met to now
                        totalTimeConditionIsMet += calendar.getTimeInMillis() - lastTimeConditionWasMet;
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    }
                } else {
                    //if it's not then we reset the last time condition was met
                    lastTimeConditionWasMet = 0;
                }
            }
        }

        //if we have a condition for the value Ax
        if (currentValue.getAyCondition() != 0) {
            //if the condition is smaller than 0
            if (currentValue.getAyCondition() < 0) {
                //verify if the current read value is smaller than 0
                if (y < currentValue.getAy()) {
                    //if yes we check when was the last time the condition was met
                    //if it's 0 then it was never met or was reset
                    if (lastTimeConditionWasMet == 0) {
                        //so we save the time when the condition was met
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    } else {
                        //if it's not 0 then we add the time between now and the last time the
                        //condition was met
                        //and set the last time the condition was met to now
                        totalTimeConditionIsMet += calendar.getTimeInMillis() - lastTimeConditionWasMet;
                        lastTimeConditionWasMet = calendar.getTimeInMillis();

                    }
                } else {
                    //if it's not then we reset the last time condition was met
                    lastTimeConditionWasMet = 0;
                }
            } else {
                //it's bigger than 0 so we verify if current x is bigger than the condition
                if (y > currentValue.getAy()) {
                    if (lastTimeConditionWasMet == 0) {
                        //so we save the time when the condition was met
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    } else {
                        //if it's not 0 then we add the time between now and the last time the
                        //condition was met
                        //and set the last time the condition was met to now
                        totalTimeConditionIsMet += calendar.getTimeInMillis() - lastTimeConditionWasMet;
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    }
                } else {
                    //if it's not then we reset the last time condition was met
                    lastTimeConditionWasMet = 0;
                }
            }
        }

        //if we have a condition for the value Ax
        if (currentValue.getAzCondition() != 0) {
            //if the condition is smaller than 0
            if (currentValue.getAzCondition() < 0) {
                //verify if the current read value is smaller than 0
                if (z < currentValue.getAz()) {
                    //if yes we check when was the last time the condition was met
                    //if it's 0 then it was never met or was reset
                    if (lastTimeConditionWasMet == 0) {
                        //so we save the time when the condition was met
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    } else {
                        //if it's not 0 then we add the time between now and the last time the
                        //condition was met
                        //and set the last time the condition was met to now
                        totalTimeConditionIsMet += calendar.getTimeInMillis() - lastTimeConditionWasMet;
                        lastTimeConditionWasMet = calendar.getTimeInMillis();

                    }
                } else {
                    //if it's not then we reset the last time condition was met
                    lastTimeConditionWasMet = 0;
                }
            } else {
                //it's bigger than 0 so we verify if current x is bigger than the condition
                if (z > currentValue.getAz()) {
                    if (lastTimeConditionWasMet == 0) {
                        //so we save the time when the condition was met
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    } else {
                        //if it's not 0 then we add the time between now and the last time the
                        //condition was met
                        //and set the last time the condition was met to now
                        totalTimeConditionIsMet += calendar.getTimeInMillis() - lastTimeConditionWasMet;
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    }
                } else {
                    //if it's not then we reset the last time condition was met
                    lastTimeConditionWasMet = 0;
                }
            }
        }
    }

    /**
     * Create a chart
     *
     * @param chart
     */
    private void createChart(LineChart chart) {

        chart.setOnChartValueSelectedListener(this);

        // disable description text
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        // set an alternative background color
        chart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        chart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(2f);
        leftAxis.setAxisMinimum(-2f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    /**
     * Create a new set for a chart
     *
     * @param description
     * @return
     */
    private LineDataSet createSet(String description) {

        LineDataSet set = new LineDataSet(null, description);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(1f);
        set.setCircleRadius(1f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    /**
     * Get the gyroscope values from an event
     *
     * @param event
     */
    private void getGyroscope(SensorEvent event) {
        //get the values from the event
        float[] values = event.values;
        //Create x,y,z values to be more readable
        float x = values[0];
        float y = values[1];
        float z = values[2];
        Log.d("MotionApp", "x: " + x + ", y: " + y + ", z: " + z);
        //Get a new instance for the calendar
        calendar = Calendar.getInstance();

        if (!startCheck) {
            //it's not yet time to check the values so we just exit the function
            return;
        }
        if (totalGyroscopeTime % 3 == 0) {
            //Add the values to their respective chart
            addEntry(chartWx, x, totalGyroscopeTime / 1000, "Wx");
            addEntry(chartWy, y, totalGyroscopeTime / 1000, "Wy");
            addEntry(chartWz, z, totalGyroscopeTime / 1000, "Wz");
        }

        Log.d("Gyroscope", "x: " + x + ", y: " + y + ", z: " + z);

        //if we have a condition for the value Ax
        if (currentValue.getWxCondition() != 0) {
            //if the condition is smaller than 0
            if (currentValue.getWxCondition() < 0) {
                //verify if the current read value is smaller than 0
                if (x < currentValue.getWx()) {
                    //if yes we check when was the last time the condition was met
                    //if it's 0 then it was never met or was reset
                    if (lastTimeConditionWasMet == 0) {
                        //so we save the time when the condition was met
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    } else {
                        //if it's not 0 then we add the time between now and the last time the
                        //condition was met
                        //and set the last time the condition was met to now
                        totalTimeConditionIsMet += calendar.getTimeInMillis() - lastTimeConditionWasMet;
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    }
                } else {
                    //if it's not then we reset the last time condition was met
                    lastTimeConditionWasMet = 0;
                }
            } else {
                //it's bigger than 0 so we verify if current x is bigger than the condition
                if (x > currentValue.getWx()) {
                    if (lastTimeConditionWasMet == 0) {
                        //so we save the time when the condition was met
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    } else {
                        //if it's not 0 then we add the time between now and the last time the
                        //condition was met
                        //and set the last time the condition was met to now
                        totalTimeConditionIsMet += calendar.getTimeInMillis() - lastTimeConditionWasMet;
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    }
                } else {
                    //if it's not then we reset the last time condition was met
                    lastTimeConditionWasMet = 0;
                }
            }
        }

        //if we have a condition for the value Ax
        if (currentValue.getWyCondition() != 0) {
            //if the condition is smaller than 0
            if (currentValue.getWyCondition() < 0) {
                //verify if the current read value is smaller than 0
                if (y < currentValue.getWy()) {
                    //if yes we check when was the last time the condition was met
                    //if it's 0 then it was never met or was reset
                    if (lastTimeConditionWasMet == 0) {
                        //so we save the time when the condition was met
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    } else {
                        //if it's not 0 then we add the time between now and the last time the
                        //condition was met
                        //and set the last time the condition was met to now
                        totalTimeConditionIsMet += calendar.getTimeInMillis() - lastTimeConditionWasMet;
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    }
                } else {
                    //if it's not then we reset the last time condition was met
                    lastTimeConditionWasMet = 0;
                }
            } else {
                //it's bigger than 0 so we verify if current x is bigger than the condition
                if (y > currentValue.getWy()) {
                    if (lastTimeConditionWasMet == 0) {
                        //so we save the time when the condition was met
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    } else {
                        //if it's not 0 then we add the time between now and the last time the
                        //condition was met
                        //and set the last time the condition was met to now
                        totalTimeConditionIsMet += calendar.getTimeInMillis() - lastTimeConditionWasMet;
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    }
                } else {
                    //if it's not then we reset the last time condition was met
                    lastTimeConditionWasMet = 0;
                }
            }
        }

        //if we have a condition for the value Ax
        if (currentValue.getWzCondition() != 0) {
            //if the condition is smaller than 0
            if (currentValue.getWzCondition() < 0) {
                //verify if the current read value is smaller than 0
                if (z < currentValue.getWz()) {
                    //if yes we check when was the last time the condition was met
                    //if it's 0 then it was never met or was reset
                    if (lastTimeConditionWasMet == 0) {
                        //so we save the time when the condition was met
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    } else {
                        //if it's not 0 then we add the time between now and the last time the
                        //condition was met
                        //and set the last time the condition was met to now
                        totalTimeConditionIsMet += calendar.getTimeInMillis() - lastTimeConditionWasMet;
                        lastTimeConditionWasMet = calendar.getTimeInMillis();

                    }
                } else {
                    //if it's not then we reset the last time condition was met
                    lastTimeConditionWasMet = 0;
                }
            } else {
                //it's bigger than 0 so we verify if current x is bigger than the condition
                if (z > currentValue.getWz()) {
                    if (lastTimeConditionWasMet == 0) {
                        //so we save the time when the condition was met
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    } else {
                        //if it's not 0 then we add the time between now and the last time the
                        //condition was met
                        //and set the last time the condition was met to now
                        totalTimeConditionIsMet += calendar.getTimeInMillis() - lastTimeConditionWasMet;
                        lastTimeConditionWasMet = calendar.getTimeInMillis();
                    }
                } else {
                    //if it's not then we reset the last time condition was met
                    lastTimeConditionWasMet = 0;
                }
            }
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            //We got the result code we needed(2) and got result ok so it means
            //that we managed to to load the file we wanted
            if (!data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // The URI will now be something like content://PACKAGE-NAME/root/path/to/file
                Uri uri = data.getData();
                //We split the URI
                String fileNameArray[] = uri.toString().split("\\.");
                //Get the file extension, which is the last value from the split array
                String extension = fileNameArray[fileNameArray.length - 1];
                if (extension.equals("csv")) {
                    // A utility method is provided to transform the URI to a File object
                    File file = com.nononsenseapps.filepicker.Utils.getFileForUri(uri);
                    // If you want a URI which matches the old return value, you can do
                    Uri fileUri = Uri.fromFile(file);
                    // Do something with the result
                    //Show the path in the app
                    fileTextName.setText(fileUri.getPath());
                    //Save the file location
                    saveFileLocation(fileUri.getPath());
                    //Load the values from the file
                    loadValuesFromFile(fileUri.getPath());
                } else {
                    Toast.makeText(MainActivity.this, "That's not a csv file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Load the values from the csv file
     *
     * @param path
     */
    private void loadValuesFromFile(String path) {
        //Create a CSVReader object
        CSVReader csvReader = null;
        try {
            //We use try and catch here because we might get an exception when trying to load the
            //file so we catch it
            csvReader = new CSVReader(new FileReader(path));
            String[] nextLine;
            InputValue inputValue;
            inputValues = new ArrayList<>();
            try {
                while ((nextLine = csvReader.readNext()) != null) {
                    //While we still have lines to read from the csv file we keep reading the next
                    //line and work with it
                    inputValue = new InputValue();
                    try {
                        //nextLine[] is an array of values from the line
                        //We read all the values from that line
                        inputValue.setAx(Double.valueOf(nextLine[0].trim()));
                        inputValue.setAy(Double.valueOf(nextLine[1].trim()));
                        inputValue.setAz(Double.valueOf(nextLine[2].trim()));
                        inputValue.setWx(Double.valueOf(nextLine[3].trim()));
                        inputValue.setWy(Double.valueOf(nextLine[4].trim()));
                        inputValue.setWz(Double.valueOf(nextLine[5].trim()));
                        inputValue.setAxCondition(Integer.valueOf(nextLine[6].trim()));
                        inputValue.setAyCondition(Integer.valueOf(nextLine[7].trim()));
                        inputValue.setAzCondition(Integer.valueOf(nextLine[8].trim()));
                        inputValue.setWxCondition(Integer.valueOf(nextLine[9].trim()));
                        inputValue.setWyCondition(Integer.valueOf(nextLine[10].trim()));
                        inputValue.setWzCondition(Integer.valueOf(nextLine[11].trim()));
                        inputValue.setTime(Double.valueOf(nextLine[12].trim()));
                        inputValue.setStepDescription(nextLine[13]);
                        //Add the input value to the list of values
                        inputValues.add(inputValue);
                        Log.d("InputValues", inputValue.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the process
     */
    private void start() {
        refreshCharts();
        createCharts();
        stopProcess = false;
        //Do the work
        DoWork doWork = new DoWork();
        doWork.execute();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    /**
     * Beep sound
     */
    private void beep() {
        //Use the correct.mp3 file from the raw folder and play it
        MediaPlayer.create(this, R.raw.correct).start();
    }

    /**
     * Beep for process start
     */
    private void beepStart() {
        //Use a basic tone from the Android device
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 200);
    }

    /**
     * Beep for wrong step
     */
    private void beepWrongStep() {
        //Use the incorrect.mp3 file from the raw folder and play it
        MediaPlayer.create(this, R.raw.incorrect).start();

    }

    /**
     * Save the file location in SharedPreferences for future use
     *
     * @param fileLocation
     */
    private void saveFileLocation(String fileLocation) {

        SharedPreferences.Editor editor = getSharedPreferences("MotionTrainingApp", MODE_PRIVATE).edit();
        editor.putString("fileLocation", fileLocation);
        editor.commit();

    }

    /**
     * Get the file location from SharedPreferences
     *
     * @return
     */
    private String getFileLocationFromPreferences() {

        SharedPreferences prefs = getSharedPreferences("MotionTrainingApp", MODE_PRIVATE);
        String fileLocation = prefs.getString("fileLocation", null);

        return fileLocation;
    }

    /**
     * Show a dialog windows with the countdown and the current step values
     *
     * @param value
     */
    private void showDialog(final InputValue value) {
        //Create an Alert Dialog
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).show();
        //Set our custom xml layout for this dialog
        alertDialog.setContentView(R.layout.dialog_window);
        //Create references to the two text views in teh layout
        final TextView timer = (TextView) alertDialog.findViewById(R.id.timer);
        TextView csvLine = (TextView) alertDialog.findViewById(R.id.csvLine);
        csvLine.setText(value.getCSVLine());
        //Start a coundown for 5 seconds, each tick duration being 1 second
        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                if (seconds == 1) {
                    //Show Go if we are at the last step
                    timer.setText("GO!");
                } else {
                    //Show the countdown current number
                    timer.setText("" + (seconds - 1));
                }
            }

            public void onFinish() {
                //Dismiss the dialog when the countdown is done
                alertDialog.dismiss();
            }
        }.start();
    }

    /**
     * AsyncTask used to do all the background work for the app
     */
    private class DoWork extends AsyncTask<String, Object, String> {


        @Override
        protected String doInBackground(String... result) {
            //We beep the start of the process
            beepStart();
            //Reset the values to 0
            totalAccelerometerTime = 0;
            totalGyroscopeTime = 0;
            int totalVerifyTime;
            //Read every value from the array
            for (int i = 0; i < inputValues.size(); i++) {
                //If we must top the process we stop it
                if (stopProcess) {
                    startCheck = false;
                    return null;
                }
                //If not then we get the current value

                final InputValue value = inputValues.get(i);

                final InputValue finalValue = value;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        instruction.setText(finalValue.getStepDescription());
                    }
                });
                //For each value we reset these values
                currentValue = value;
                totalVerifyTime = 0;
                startCheck = true;
                totalTimeConditionIsMet = 0;
                lastTimeConditionWasMet = 0;
                conditionMet = false;
                //While the total time of 5 seconds is not yet done
                while (totalVerifyTime < 5100) {
                    try {
                        //If we need to stop the process we exit the AsyncTask and stop it
                        if (stopProcess) {
                            startCheck = false;
                            return null;
                        }
                        //Pause the current task for 0.1 seconds
                        Thread.sleep(100);
                        //Add the passed time to these values
                        totalAccelerometerTime += 100;
                        totalGyroscopeTime += 100;
                        //If the total time the condition is met is bigger than what we had in the file
                        //we go to the next step
                        if (totalTimeConditionIsMet > (currentValue.getTime() * 1000)) {
                            //We got the necessary time so we beep for sucess
                            beep();
                            //The condition is met
                            conditionMet = true;
                            Log.d("MotionApp", "Condition met on step " + i + ", moving to next step");
                            final int finalStep = i;
                            //We use runOnUiThread because we can't do UI stuff from an AsyncTask
                            //since this task runs on a separate thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Condition met on step " + finalStep + ", moving to next step", Toast.LENGTH_SHORT).show();
                                }
                            });
                            //And we exit this while so we can go to the next value
                            break;
                        }
                        //Add the 0.1 pause time to the total verify time
                        totalVerifyTime += 100;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!conditionMet && totalVerifyTime >= 5000) {
                        //If we passed the total verify time and the condition is still not met
                        //We beep for not doing a wring step
                        beepWrongStep();
                        Log.d("MotionApp", "Condition not met in the 5 sec timeframe!");
                        //We use runOnUiThread because we can't do UI stuff from an AsyncTask
                        //since this task runs on a separate thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Display a message and the dialog window with the countdown
                                Toast.makeText(MainActivity.this, "Condition not met in the 5 sec timeframe!", Toast.LENGTH_SHORT).show();
                                showDialog(value);
                            }
                        });
                        //Reset these values
                        startCheck = false;
                        totalTimeConditionIsMet = 0;
                        lastTimeConditionWasMet = 0;
                        totalVerifyTime = 0;
                        try {
                            //Sleep for 5 seconds, the time the dialog will be displayed
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        startCheck = true;
                    }
                }


            }

            startCheck = false;
            return null;
        }
    }

    /**
     * Create the charts
     */
    private void createCharts() {
        //Creating the custom charts
        createChart(chartAx);
        createChart(chartAy);
        createChart(chartAz);
        createChart(chartWx);
        createChart(chartWy);
        createChart(chartWz);

    }

    /**
     * Reset the charts
     */
    private void refreshCharts() {
        chartAx.clear();
        chartAy.clear();
        chartAz.clear();
        chartWx.clear();
        chartWy.clear();
        chartWz.clear();
        createCharts();
    }
}