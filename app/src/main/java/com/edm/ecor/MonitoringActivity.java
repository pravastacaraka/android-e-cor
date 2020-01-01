package com.edm.ecor;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MonitoringActivity extends AppCompatActivity {

    EditText yValue;
    Button btnInsert;
    ImageView btnHome, btnLimit, btnHistory, btnSetting;

    FirebaseDatabase database;
    DatabaseReference myRef;

    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
    GraphView graphView;
    LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);

        transparentStatusAndNavigation();

        btnHome = findViewById(R.id.iv_home);
//        btnInsert = findViewById(R.id.bt_insert);
//        yValue = findViewById(R.id.et_yValue);
        graphView = findViewById(R.id.gv_graph_monitoring);

        // Initialize firebase database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("dataTable");

        // Initialize GraphView and LineGraphSeries
        graphView.setVisibility(View.VISIBLE);
        series = new LineGraphSeries<>();
        graphView.addSeries(series);

        // When insert button clicked
//        setListeners();

        // Render x-axis label
        graphView.getGridLabelRenderer().setNumHorizontalLabels(3);
        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if(isValueX) {
                    return sdf.format(new Date((long) value));
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        btnHome = findViewById(R.id.iv_home);
        btnLimit = findViewById(R.id.iv_limit);
        btnHistory = findViewById(R.id.iv_history);
        btnSetting = findViewById(R.id.iv_setting);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MonitoringActivity.this, HomeActivity.class);
                startActivity(i);
                finish();
            }
        });
        btnLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MonitoringActivity.this, LimitActivity.class);
                startActivity(i);
                finish();
            }
        });
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MonitoringActivity.this, HistoryActivity.class);
                startActivity(i);
                finish();
            }
        });
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MonitoringActivity.this, SettingActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void setListeners() {
        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = myRef.push().getKey();
                long x = new Date().getTime();
                int y = Integer.parseInt(yValue.getText().toString());

                PointValue pointValue = new PointValue(x, y);

                myRef.child(id).setValue(pointValue);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataPoint[] dp = new DataPoint[(int)dataSnapshot.getChildrenCount()];
                int index = 0;

                for (DataSnapshot myDataSnapshot : dataSnapshot.getChildren()){
                    PointValue pointValue = myDataSnapshot.getValue(PointValue.class);
                    dp[index] = new DataPoint(pointValue.getxValue(), pointValue.getyValue());
                    index++;
                }

                series.resetData(dp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MonitoringActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void transparentStatusAndNavigation() {

        // Make full transparent status bar
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }

        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void setWindowFlag(final int bits, boolean on) {

        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();

        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }

        win.setAttributes(winParams);
    }
}
