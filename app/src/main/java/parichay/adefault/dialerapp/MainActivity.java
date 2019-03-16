package parichay.adefault.dialerapp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aykuttasil.callrecord.CallRecord;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    ArrayList callnum = new ArrayList<String>();
    ArrayList callname = new ArrayList<String>();
    SharedPreferences sp;
    CallRecord callRecord;
    TextView num;
    TextView nam;
    TextView comp;
    Intent callIntent = new Intent(Intent.ACTION_CALL);
    int lock=0; //Unlocked
    static int lock2=1;//Locked
    int first=1;
    int audioperm=1,externalstorperm=1;
    static int successful = 1;// Successful
    static int app_call=0;
    TextToSpeech tts;


    String company;
    String imei;
    static MediaRecorder recorder = new MediaRecorder();

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app_call=0;
        lock2 = getIntent().getIntExtra("lock2",1);
        successful = getIntent().getIntExtra("success",1);

        /*
        if(successful==0)
        {
            Toast toast = Toast.makeText(this, "Call not successful\nAtleast 5sec required", Toast.LENGTH_SHORT);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            if( v != null) v.setGravity(Gravity.CENTER);
            toast.show();
        }
        */

        sp = this.getSharedPreferences("parichay.adefault.dialerapp", Context.MODE_PRIVATE);

        company = sp.getString("company","");
        imei = sp.getString("imei","");

        try {

            lock = getIntent().getIntExtra("lock", 0);
            first = getIntent().getIntExtra("first", 1);
        } catch (Exception e) {
        }

        num = (TextView) findViewById(R.id.textView2);
        nam = (TextView) findViewById(R.id.textView7);
        comp = (TextView) findViewById(R.id.textView8);

        FirebaseDatabase.getInstance().getReference().child("companies").child(company).child("credentials").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    comp.setText(dataSnapshot.getValue().toString());
                }
                catch (Exception e){
                    comp.setText("Company Name");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(first==1) {
            num.setText("LOADING...");
            nam.setText("LOADING...");

        }
        else
        {
            num.setText(sp.getString("number","LOADING..."));
            nam.setText(sp.getString("name","LOADING..."));

        }


        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }



            FirebaseDatabase.getInstance().getReference().child("callers").child(imei).child("customers").child("name").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    try {
                        String replace = dataSnapshot.getValue().toString().replaceAll("^\\[|]$", "").trim();
                        replace = replace.replaceAll("\\s+", "");
                        callname = new ArrayList<String>(Arrays.asList(replace.split(",")));
                        nam.setText(callname.get(0).toString());
                    } catch (Exception e) {
                        nam.setText("LIST EMPTY");
                    }


                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



            FirebaseDatabase.getInstance().getReference().child("callers").child(imei).child("customers").child("phone").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        String replace1 = dataSnapshot.getValue().toString().replaceAll("^\\[|]$", "").trim();
                        replace1 = replace1.replaceAll("\\s+", "");
                        callnum = new ArrayList<String>(Arrays.asList(replace1.split(",")));
                        num.setText(callnum.get(0).toString());

                    } catch (Exception e) {
                        num.setText("");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



        }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            if(num.getText().equals(""))
            {
                Toast.makeText(this, "CONTACT ADMIN TO REFILL LIST", Toast.LENGTH_SHORT).show();
            }
            else if(lock2==0)
            {
            changedata();
            lock=0;
            lock2=1;
            }
            else
            {
                Toast.makeText(this, "Any number cannot be skipped", Toast.LENGTH_SHORT).show();
            }
            return true;

        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

            if(num.getText().equals(""))
            {
                Toast.makeText(this, "CONTACT ADMIN TO REFILL LIST", Toast.LENGTH_SHORT).show();
            }
            else if(lock==0 && audioperm==1 && externalstorperm==1) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                } else {

                    if (!hasPermissions(this, PERMISSIONS)) {
                        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                    }

                    try {
                        callIntent.setData(Uri.parse("tel:" + callnum.get(0).toString()));
                        app_call=1;
                        sp.edit().putString("name", callname.get(0).toString()).apply();
                        sp.edit().putString("number", callnum.get(0).toString()).apply();
                        startActivity(callIntent);
                    }
                    catch (Exception e){
                        Toast.makeText(this, "LOADING...", Toast.LENGTH_SHORT).show();
                    }

                }
            }
            else if(lock==1 && audioperm==1 && externalstorperm==1)
            {
                Toast.makeText(this, "Go to Next Number ", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if (!hasPermissions(this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                }
            }

            return true;
        }

        else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0&& grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {

                    return false;
                }
            }
        }
        return true;
    }
    private void changedata() {
        String name,num1;
        name = callname.get(0).toString().trim();
        num1 = callnum.get(0).toString().trim();
        callnum.remove(0);
        callname.remove(0);
        if(successful==0) {
            callname.add(name);
            callnum.add(num1);
        }
        FirebaseDatabase.getInstance().getReference().child("callers").child(imei).child("customers").child("name").setValue(callname);
        FirebaseDatabase.getInstance().getReference().child("callers").child(imei).child("customers").child("phone").setValue(callnum);
    }

    public void name(View view)
    {
        tts=new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(new Locale("hi", "IN"));
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    }
                    else{
                        ConvertTextToSpeech();
                    }
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });
    }


    public void onTaskRemoved(Intent rootIntent)
    {
    }

    public void onBackPressed() {
        Intent setIntent = new Intent(this,instruction.class);
        setIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(setIntent);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        if(tts != null){

            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    public void onClick(View v){

        ConvertTextToSpeech();

    }

    private void ConvertTextToSpeech() {
        // TODO Auto-generated method stub
        String text = nam.getText().toString();
        if(text==null||"".equals(text))
        {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


}
