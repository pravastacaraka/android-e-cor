package com.edm.ecor;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MonitoringActivity extends AppCompatActivity {

    EditText yValue;
    Button btnInsert;
    ImageView btnHome, btnLimit, btnHistory, btnSetting;
    TextView tvDateNow, tvTimeNow, tvUsage, tvPrice;

    FirebaseDatabase database;
    DatabaseReference myRef;

    SimpleDateFormat dateToday = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    SimpleDateFormat timeToday = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);

        transparentStatusAndNavigation();

        tvDateNow = findViewById(R.id.tv_date);
        tvTimeNow = findViewById(R.id.tv_time);
        tvUsage = findViewById(R.id.tv_kwh);
        tvPrice = findViewById(R.id.tv_price);
//        btnInsert = findViewById(R.id.bt_insert);
//        yValue = findViewById(R.id.et_yValue);

        // Initialize firebase database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("daya");

        // When insert button clicked
//        setListeners();

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

        tvDateNow.setText(dateToday.format(new Date().getTime()));
        tvTimeNow.setText(timeToday.format(new Date().getTime()));

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double value = dataSnapshot.getValue(Double.class);
                Double price = value * 0.00127;

                value = value / 1000;

                tvUsage.setText(String.format("%.3f", value) + " kWh");
                tvPrice.setText("Rp" + String.format("%.3f", price) + ",00");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(MonitoringActivity.this, HomeActivity.class);
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
