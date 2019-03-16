package parichay.adefault.dialerapp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static parichay.adefault.dialerapp.MainActivity.hasPermissions;

public class splash extends AppCompatActivity {

    String[] PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
    };

    private String imei;
    int flag=0;
    int start=0;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final Intent i = new Intent(this, instruction.class);
        //imei = getDeviceId(getApplicationContext());
        imei = "000000000000000";
        sp = this.getSharedPreferences("parichay.adefault.dialerapp", Context.MODE_PRIVATE);
        sp.edit().putString("imei",imei).apply();

        FirebaseDatabase.getInstance().getReference().child("IMEI").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                    {
                        if(imei.equals(dataSnapshot1.getKey().toString()))
                        {
                            sp.edit().putString("company",dataSnapshot1.getValue().toString()).apply();
                            flag=1;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(i);
                                    finish();
                                }
                            }, 500);
                        }
                    }

                    if (flag == 0 ) {
                        Toast.makeText(splash.this, "ACCESS DENIED", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }


    public String getDeviceId(Context context) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
            }
        }
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }
}


