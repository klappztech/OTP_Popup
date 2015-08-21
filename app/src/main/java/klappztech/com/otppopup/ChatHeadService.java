package klappztech.com.otppopup;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by mahc on 8/12/2015.
 */
public class ChatHeadService extends Service {

    private static final int OTP_X_OFFSET = 100 ;
    private static final int OTP_Y_OFFSET = 50 ;
    private WindowManager windowManager;
    private TextView otpText,deleteIcon;
    private RelativeLayout layout;
    private String otpExtra;

    @Override public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override public void onCreate() {


        super.onCreate();
    }

    public int onStartCommand (Intent intent, int flags, int startId) {

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //get value from intent
        if(intent != null) {
            otpExtra = (String) intent.getExtras().get("OTP");
            if (otpExtra == null) {
                otpExtra = "INVALID";
            }
        }

        // get screen size

        // prepare layout here
        otpText = new TextView(this);
        otpText.setText(otpExtra);
        otpText.setBackgroundResource(R.drawable.cloud);
        otpText.setGravity(Gravity.CENTER);
        otpText.setTextColor(Color.parseColor("#000000"));
        otpText.setTextSize(32);

        deleteIcon = new TextView(this);
        deleteIcon.setBackgroundResource(R.drawable.delete);
        deleteIcon.setGravity(Gravity.CENTER);
        deleteIcon.setTextColor(Color.parseColor("#000000"));
        deleteIcon.setTextSize(32);



        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 100;
        params.y = 100;

        //params
        final WindowManager.LayoutParams params_delete = params;
        WindowManager.LayoutParams params_otp = params;
            params_delete.x =OTP_X_OFFSET;
            params_delete.y =1000;


        //add views here
        //windowManager.addView(, params);
        windowManager.addView(otpText,params);




        otpText.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        params.gravity = Gravity.TOP | Gravity.LEFT;
                        params.x = 100;
                        params.y = 1000;

                        windowManager.addView(deleteIcon,params);

                        return true;
                    case MotionEvent.ACTION_UP:
                        if (deleteIcon != null) windowManager.removeView(deleteIcon);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(otpText, params);



                        if(event.getRawY()<100) {
                            // not working
                            Toast.makeText(getApplicationContext(), "At right edge!", Toast.LENGTH_LONG).show();
                            if (otpText != null) windowManager.removeView(otpText);
                            if (deleteIcon != null) windowManager.removeView(deleteIcon);
                        }

                        return true;
                }
                return false;
            }
        });

        return START_STICKY;

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (otpText != null) windowManager.removeView(otpText);
    }
}