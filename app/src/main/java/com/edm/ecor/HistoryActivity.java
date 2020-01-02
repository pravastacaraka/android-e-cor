package com.edm.ecor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class HistoryActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ImageView btnMonitoring, btnHome, btnLimit, btnSetting;
    TextView tvAvgUsage, tvTotalCost;

    FirebaseDatabase database;
    DatabaseReference myRefUsage, myRefTime, myPar;

    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());

    GraphView graphView;
    LineGraphSeries<DataPoint> series;

    String[] range = { "Daily", "Weekly", "Monthly" };
    String[] monthly = {"Jan 1-Jan 31", "Feb 1-Feb 29", "Mar 1-Mar 30", "Apr 1-Apr 31", "May 1-May 30",
            "June 1-June 31", "Jul 1-Jul 30", "Aug 1-Aug 31", "Sep 1-Sep 30", "Oct 1-Oct 31","Nov 1-Nov 30", "Dec 1-Dec 31"};
    int totalData = 1;
    Double powerUsage;
    Long dateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        transparentStatusAndNavigation();

        btnHome = findViewById(R.id.iv_home);
        btnMonitoring = findViewById(R.id.iv_monitoring);
        btnLimit = findViewById(R.id.iv_limit);
        btnSetting = findViewById(R.id.iv_setting);
        graphView = findViewById(R.id.gv_graph_monitoring);
        tvAvgUsage = findViewById(R.id.tv_kwh);
        tvTotalCost = findViewById(R.id.tv_price);

        // Initialize firebase database
        database = FirebaseDatabase.getInstance();
        myRefUsage = database.getReference("daya");
        myRefTime = database.getReference("dateTime");
        myPar = myRefUsage.getParent();

        // Initialize GraphView and LineGraphSeries
        graphView.setVisibility(View.VISIBLE);
        series = new LineGraphSeries<>();
        graphView.addSeries(series);

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

        // Spinner range
        Spinner spin = findViewById(R.id.spinner_monthly);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, range);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setOnItemSelectedListener(this);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HistoryActivity.this, HomeActivity.class);
                startActivity(i);
                finish();
            }
        });
        btnMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HistoryActivity.this, MonitoringActivity.class);
                startActivity(i);
                finish();
            }
        });
        btnLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HistoryActivity.this, LimitActivity.class);
                startActivity(i);
                finish();
            }
        });
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HistoryActivity.this, SettingActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position,long id) {
        Toast.makeText(getApplicationContext(), "Selected: " + range[position] ,Toast.LENGTH_SHORT).show();

        if(position == 2) {
            // Spinner range
            Spinner spin1 = findViewById(R.id.spinner_range_month);
            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, monthly);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin1.setAdapter(adapter1);
//            spin1.setOnItemSelectedListener(this);
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Read from daya child database
        myRefUsage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double avgUsage, priceTotal;
                powerUsage = dataSnapshot.getValue(Double.class);
                long x = new Date().getTime();

                powerUsage = powerUsage / 1000;
                powerUsage += powerUsage;
                totalData++;

                priceTotal = powerUsage * 0.00127;
                avgUsage = powerUsage / totalData;

                // Send data to database
                myPar.child("dateTime").setValue(x);
                myPar.child("avgPower").setValue(avgUsage);
                myPar.child("totalCost").setValue(priceTotal);

                // Change TextView when database changed
                tvAvgUsage.setText(String.format("%.3f", avgUsage) + " kWh");
                tvTotalCost.setText("Rp" + String.format("%.3f", priceTotal) + ",00");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Read from parent database
        myRefTime.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataPoint[] dp = new DataPoint[2];
                int index = 0;

                for (DataSnapshot myDataSnapshot : dataSnapshot.getChildren()){
                    dateTime = dataSnapshot.getValue(Long.class);

                    dp[index] = new DataPoint(dateTime, powerUsage);
                    index++;
                }

//                series.resetData(dp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(HistoryActivity.this, HomeActivity.class);
        startActivity(i);
        finish();
    }

    private void transparentStatusAndNavigation() {

        // Make full transparent status bar
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, true);
        }

        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false);
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
