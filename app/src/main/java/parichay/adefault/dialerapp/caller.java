package parichay.adefault.dialerapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class caller extends BroadcastReceiver {

    private static final String TAG = "PhoneStateBroadcast";
    Context mContext;
    String incoming_number;
    private int prev_state;
    Intent newintent;
    SharedPreferences sp;
    String dur;
    int flag=0;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        String phoneNr = bundle.getString("incoming_number");
        Log.v(TAG, "phoneNr: "+phoneNr);
        mContext = context;
        newintent = new Intent();
        newintent.setClass(mContext,response.class);
        newintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(
                TelephonyManager.EXTRA_STATE_RINGING)) {

            // Phone number
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            // Ringing state
            // This code will execute when the phone has an incoming call
        } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(
                TelephonyManager.EXTRA_STATE_IDLE)
                ) {

            if(MainActivity.app_call==1) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getCallDetails();
                        System.out.print(Integer.parseInt(dur));
                        if (Integer.parseInt(dur) >= 5) {
                            MainActivity.app_call = 0;
                            mContext.startActivity(newintent);
                        } else {
                            Toast toast = Toast.makeText(mContext, "Call not successful\nAtleast 5sec required", Toast.LENGTH_SHORT);
                            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                            if (v != null) v.setGravity(Gravity.CENTER);
                            toast.show();
                            MainActivity.lock2 = 0;
                            MainActivity.app_call = 0;
                            MainActivity.successful = 0;
                        }
                    }

                }, 500);
            }
            // This code will execute when the call is answered or disconnected
        }

    }

   /* public class CustomPhoneStateListener extends PhoneStateListener {

        private static final String TAG = "CustomPhoneState";

        @Override
        public void onCallStateChanged(int state, String incomingNumber){

            if( incomingNumber != null && incomingNumber.length() > 0 )
                incoming_number = incomingNumber;

            switch(state){
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "CALL_STATE_RINGING");
                    prev_state=state;
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "CALL_STATE_OFFHOOK");
                    prev_state=state;
                    break;

                case TelephonyManager.CALL_STATE_IDLE:

                    Log.d(TAG, "CALL_STATE_IDLE==>"+incoming_number);

                    if((prev_state == TelephonyManager.CALL_STATE_OFFHOOK)){

                             prev_state=state;

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getCallDetails();
                                System.out.print(Integer.parseInt(dur));
                                if (Integer.parseInt(dur) >= 5) {
                                    mContext.startActivity(newintent);
                                } else {
                                    Toast toast = Toast.makeText(mContext, "Call not successful\nAtleast 5sec required", Toast.LENGTH_SHORT);
                                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                                    if (v != null) v.setGravity(Gravity.CENTER);
                                    toast.show();
                                    MainActivity.lock2 = 0;
                                    MainActivity.app_call = 0;
                                    MainActivity.successful = 0;
                                }
                            }

                        }, 500);
                        //Answered Call which is ended
                    }
                    if((prev_state == TelephonyManager.CALL_STATE_RINGING)){
                        prev_state=state;
                        //Rejected or Missed call
                    }
                    break;
            }
        }
    }
*/
    private void getCallDetails() {

        try {
            Uri contacts = CallLog.Calls.CONTENT_URI;
            if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.READ_CALL_LOG)
                    != PackageManager.PERMISSION_GRANTED) {
            }

            Cursor managedCursor = mContext.getContentResolver().query(contacts, null, null, null, null);
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

                }

            }
            managedCursor.close();
        }
        catch(Exception e)
        {
            dur="0";
        }

    }

}