package parichay.adefault.dialerapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class response extends AppCompatActivity {

    Intent i;
    SharedPreferences sp;
    String Date;
    String dur;
    String company;
    String time;
    Calendar now = Calendar.getInstance();
    HashMap info = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getCallDetails();

        sp = this.getSharedPreferences("parichay.adefault.dialerapp", Context.MODE_PRIVATE);
        String company1 = sp.getString("company","");
        company=company1;

        i.putExtra("lock", 1);
        i.putExtra("first",0);
        i.putExtra("lock2",0);
        i.putExtra("success", 1);


        info.put("name", sp.getString("name", ""));
        info.put("duration", dur);
        info.put("time",time);

    }

    public void pos(View view) {
        info.put("response", "positive");
        FirebaseDatabase.getInstance().getReference().child("companies").child(company).child("aftercall").child(Date).child(sp.getString("number", "")).removeValue();
        FirebaseDatabase.getInstance().getReference().child("companies").child(company).child("aftercall").child(Date).child(sp.getString("number", "")).setValue(info);

        startActivity(i);

    }

    public void neg(View view) {
        info.put("response", "negative");
        FirebaseDatabase.getInstance().getReference().child("companies").child(company).child("aftercall").child(Date).child(sp.getString("number", "")).removeValue();
        FirebaseDatabase.getInstance().getReference().child("companies").child(company).child("aftercall").child(Date).child(sp.getString("number", "")).setValue(info);

        startActivity(i);
    }

    private void getCallDetails() {

        try {
            Uri contacts = CallLog.Calls.CONTENT_URI;
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALL_LOG)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, 1);
            }
            Cursor managedCursor = getContentResolver().query(contacts, null, null, null, null);
            int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
            int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
            int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
            int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

            while (managedCursor.moveToNext()) {

                if (managedCursor.moveToLast()) {

                    HashMap rowDataCall = new HashMap<String, String>();

                    String phNumber = managedCursor.getString(number);
                    String callType = managedCursor.getString(type);
                    String callDate = managedCursor.getString(date);
                    String callDayTime = new Date(Long.valueOf(callDate)).toString();
                    // long timestamp = convertDateToTimestamp(callDayTime);
                    String callDuration = managedCursor.getString(duration);
                    String dir = null;
                    int dircode = Integer.parseInt(callType);
                    switch (dircode) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            dir = "OUTGOING";
                            break;

                        case CallLog.Calls.INCOMING_TYPE:
                            dir = "INCOMING";
                            break;

                        case CallLog.Calls.MISSED_TYPE:
                            dir = "MISSED";
                            break;
                    }

                    dur = callDuration;
                    /*
                    if (dur.equals("0")) {
                        i.putExtra("lock2", 0);
                        i.putExtra("success", 0);
                        startActivity(i);
                    }
                    */

                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    String formattedDate = df.format(new Date());

                    try {
                        Date mydate = df.parse(formattedDate);
                        Date newDate = new Date(mydate.getTime());
                        Date = df.format(newDate);
                    } catch (Exception e) {
                    }

                    int time_to_subtract = Integer.parseInt("-" + dur);
                    now.add(Calendar.SECOND, time_to_subtract);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    time = sdf.format(now.getTime());

                }

            }
            managedCursor.close();
        }
        catch(Exception e)
        {
            /*
            i.putExtra("lock2", 0);
            i.putExtra("success", 0);
            startActivity(i);
            */
        }

    }

    public static String getTime(Calendar cal){
        return "" + cal.get(Calendar.HOUR_OF_DAY) +":" +
                (cal.get(Calendar.MINUTE)) + ":" + cal.get(Calendar.SECOND);
    }

}
