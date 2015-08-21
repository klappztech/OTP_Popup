package klappztech.com.otppopup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mahc on 8/7/2015.
 */
public class SmsListener extends BroadcastReceiver {

    private static final String OTP_TEXT = "OTP",OTP_FULL_TEXT = "One time password" ;
    private SharedPreferences preferences;
    PopupWindow popUp;
    LinearLayout layout;
    TextView tv;
    ViewGroup.LayoutParams params;
    LinearLayout mainLayout;
    Button but;
    boolean click = true;
    String OTP;

    private WindowManager windowManager;
    private ImageView chatHead;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            String msg_from;
            if (bundle != null){
                //---retrieve the SMS message received---
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();

                        // OTP regex
                        // \b\d{3,6}\b

                        OTP =extractOTP(msgBody);

                        if(OTP != null) {
                            Intent popup_service = new Intent(context, ChatHeadService.class);
                            popup_service.putExtra("OTP",OTP );
                            context.startService(popup_service);
                        }

                    }
                }catch(Exception e){
//                            Log.d("Exception caught",e.getMessage());
                }
            }
        }
    }

    public String extractOTP(String msgBody) {

        String myDearOTP=null;
        String temp;
        String[] sentences;
        List<String> numbers = new ArrayList<String>();
        List<String> OTPCandidates = new  ArrayList<String>();
        sentences= msgBody.split("\\.");



        for (String sentence : sentences)
        {
            int idxOTP=-1,  idxOTPFull=-1, idxIs=-1,idxNum=-1;
            // get index of our keywords
            idxOTP = sentence.toLowerCase().indexOf(OTP_TEXT.toLowerCase());
            idxOTPFull = sentence.toLowerCase().indexOf(OTP_FULL_TEXT.toLowerCase());
            idxIs = sentence.toLowerCase().indexOf(" is ");


            if(idxOTP != -1  ||  idxOTPFull != -1  ){

                int counter=0;
                // sentence contains OTP
                //extract only numbers
                temp = sentence.replaceAll("[^0-9]", "-");
                numbers = Arrays.asList(temp.split("\\-+"));

                // take only 4 to 8 digit numbers
                for(String number : numbers) {
                    int i;
                    try {
                        i = Integer.parseInt(number);
                    } catch(NumberFormatException e) {
                        i=0;
                    }
                    if(i>=1000 && i<=99999999) {
                        OTPCandidates.add(number);
                    }
                    counter++;
                }

                // if only 1 number? that ur OTP
                if(OTPCandidates.size()>0) {
                    if(OTPCandidates.size() == 1) {
                        // thats ur OTP
                        myDearOTP = OTPCandidates.get(0);

                    } else {
                        // confusion, more numbers, get the nearest one to the "is"
                        if(idxIs != -1) {
                            int distance=0,minDistance=sentence.length(), minDistanceToItem=-1;
                            for(String number : OTPCandidates) {
                                //find the distance to "is"
                                distance = Math.abs(sentence.indexOf(number) - idxIs);
                                if(distance <= minDistance ) {
                                    minDistance=distance;
                                    minDistanceToItem = Integer.parseInt(number);
                                    myDearOTP = number;
                                }
                            }


                        } else {
                            // no otp in this sentence, but "OTP" is there
                        }
                    }
                    OTPCandidates.clear();

                } else {
                    // no otp canditates in this sentence, but "OTP" is there,

                }

            }
        }


        return myDearOTP;
    }




}