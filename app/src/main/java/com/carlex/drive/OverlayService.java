package com.carlex.drive;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class OverlayService extends Service {

    private WindowManager mWindowManager;
    private ImageView mFloatingIcon;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFloatingIcon = new ImageView(this);
        mFloatingIcon.setImageResource(R.drawable.ico);
        
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        mWindowManager.addView(mFloatingIcon, params);

        moveFloatingIcon();
    }

    private void moveFloatingIcon() {
        final DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        final int screenWidth = metrics.widthPixels;
        final int screenHeight = metrics.heightPixels;

        final Handler handler = new Handler();

        handler.post(new Runnable() {
            float x = 0;
            float y = 100;
            int directionX = 1;
            int directionY = 1;
            int speed = 5;

            @Override
            public void run() {
                if (System.currentTimeMillis() % 2000 == 0) {
                    directionX = (int) (Math.random() * 3) - 1;
                    directionY = (int) (Math.random() * 3) - 1;
                }

                if (System.currentTimeMillis() % 1000 == 0) {
                    speed = (int) (Math.random() * 10) + 1;
                }

                x += speed * directionX;
                y += speed * directionY;

                if (x >= screenWidth - mFloatingIcon.getWidth() || x <= 0) {
                    directionX = -directionX;
                }
                if (y >= screenHeight - mFloatingIcon.getHeight() || y <= 0) {
                    directionY = -directionY;
                }

                double angle = calculateAngle(x, y, x + directionX, y + directionY);
                mFloatingIcon.setRotation((float) angle);

                WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFloatingIcon.getLayoutParams();
                params.x = (int) x;
                params.y = (int) y;
                mWindowManager.updateViewLayout(mFloatingIcon, params);

                handler.postDelayed(this, 10);
            }
        });
    }

    private double calculateAngle(float x1, float y1, float x2, float y2) {
        double angle = Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
        return angle < 0 ? angle + 360 : angle;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingIcon != null) {
            mWindowManager.removeView(mFloatingIcon);
        }
    }
}

