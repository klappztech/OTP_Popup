package klappztech.com.otppopup;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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


public class MainActivity extends ActionBarActivity {

    PopupWindow popUp;
    LinearLayout layout;
    TextView tv;
    ViewGroup.LayoutParams params;
    LinearLayout mainLayout;
    Button but, btnShowPopup;
    boolean click = true;
    private static final String OTP_TEXT = "OTP", OTP_FULL_TEXT = "One time password";

    private WindowManager windowManager;
    private ImageView chatHead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnShowPopup = (Button) findViewById(R.id.button);

        btnShowPopup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                EditText sms = (EditText) findViewById(R.id.editText);

                String temp = extractOTP(sms.getText().toString());

                //load bookmarks
                Intent popup_service = new Intent(getApplicationContext(), ChatHeadService.class);
                popup_service.putExtra("OTP",temp );
                getApplicationContext().startService(popup_service);
            }
        });


    }


    public String extractOTP(String msgBody) {

        String myDearOTP = "NULL";
        String temp;
        String[] sentences;
        List<String> numbers = new ArrayList<String>();
        List<String> OTPCandidates = new ArrayList<String>();
        sentences = msgBody.split("\\.");

        EditText sms_extract = (EditText) findViewById(R.id.editText2);

        for (String sentence : sentences) {
            int idxOTP = -1, idxOTPFull = -1, idxIs = -1, idxNum = -1, idxColon = -1;
            // get index of our keywords
            idxOTP = sentence.toLowerCase().indexOf(OTP_TEXT.toLowerCase());
            idxOTPFull = sentence.toLowerCase().indexOf(OTP_FULL_TEXT.toLowerCase());
            idxIs = sentence.toLowerCase().indexOf(" is ");
            idxColon = sentence.toLowerCase().indexOf(":");


            if (idxOTP != -1 || idxOTPFull != -1) {

                int counter = 0;
                // sentence contains OTP
                //extract only numbers
                temp = sentence.replaceAll("[^0-9]", "-");
                numbers = Arrays.asList(temp.split("\\-+"));

                // take only 4 to 8 digit numbers
                for (String number : numbers) {
                    int i;
                    try {
                        i = Integer.parseInt(number);
                    } catch (NumberFormatException e) {
                        i = 0;
                    }
                    if (i >= 1000 && i <= 99999999) {
                        OTPCandidates.add(number);
                    }
                    counter++;
                }

                // if only 1 number? that ur OTP
                if (OTPCandidates.size() > 0) {
                    if (OTPCandidates.size() == 1) {
                        // thats ur OTP
                        myDearOTP = OTPCandidates.get(0);

                    } else {
                        // confusion, more numbers, get the nearest one to the "is"
                        if (idxIs != -1) {
                            int distance = 0, minDistance = sentence.length();
                            for (String number : OTPCandidates) {
                                //find the distance to "is"
                                distance = findDistance(sentence.indexOf(number), idxIs, number.length());
                                if (distance <= minDistance) {
                                    minDistance = distance;
                                    myDearOTP = number;
                                }
                            }


                        } else if (idxColon != -1) {
                            int distance = 0, minDistance = sentence.length();
                            for (String number : OTPCandidates) {
                                //find the distance to "is"
                                distance = findDistance(sentence.indexOf(number), idxColon, number.length());
                                if (distance <= minDistance) {
                                    minDistance = distance;
                                    myDearOTP = number;
                                }
                            }
                        } else if (idxOTPFull != -1) {
                            int distance = 0, minDistance = sentence.length();
                            for (String number : OTPCandidates) {
                                //find the distance to "is"
                                distance = findDistance(sentence.indexOf(number), idxOTPFull, number.length());
                                if (distance <= minDistance) {
                                    minDistance = distance;
                                    myDearOTP = number;
                                }
                            }
                        } else if (idxOTP != -1) {
                            int distance = 0, minDistance = sentence.length();
                            for (String number : OTPCandidates) {
                                //find the distance to "is"
                                distance = findDistance(sentence.indexOf(number), idxOTP, number.length());
                                if (distance <= minDistance) {
                                    minDistance = distance;
                                    myDearOTP = number;
                                }
                            }
                        }

                        OTPCandidates.clear();

                    }

                }else{
                    // no otp canditates in this sentence, but "OTP" is there,

                }


            }
        }

        sms_extract.setText("kitti: " + myDearOTP);
        return myDearOTP;
    }


    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    private int findDistance(int i, int idxIs, int length) {
        int distance;
        distance = i - idxIs;
        if (distance < 0) {
            distance = -(distance + length);
        } else {
            distance = distance - 1;
        }
        return distance;
    }
}