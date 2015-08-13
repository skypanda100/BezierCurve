package com.example.bezierCurve;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.InputStream;

public class MyActivity extends Activity {
    private Pager pager;
    private PagerFactory pagerFactory;
    private Bitmap currentBitmap, mCurPageBitmap, mNextPageBitmap;
    private Canvas mCurPageCanvas, mNextPageCanvas;
    private static final String[] pages = {"one", "two", "three"};
    private int screenWidth;
    private int screenHeight;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initView();
    }

    private void initView() {

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        pager = new Pager(this, screenWidth, screenHeight);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(pager, layoutParams);

        mCurPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        mNextPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);

        mCurPageCanvas = new Canvas(mCurPageBitmap);
        mNextPageCanvas = new Canvas(mNextPageBitmap);
        pagerFactory = new PagerFactory(getApplicationContext());

        pager.setBitmaps(mCurPageBitmap, mCurPageBitmap);
        loadImage(mCurPageCanvas, 0);

        pager.setOnTouchListener(new View.OnTouchListener() {

            private int count = pages.length;
            private int currentIndex = 0;
            private int lastIndex = 0;
            private Bitmap lastBitmap = null;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                boolean ret = false;
                if (v == pager) {
                    if (e.getAction() == MotionEvent.ACTION_DOWN) {
                        pager.calcCornerXY(e.getX(), e.getY());

                        lastBitmap = currentBitmap;
                        lastIndex = currentIndex;

                        pagerFactory.onDraw(mCurPageCanvas, currentBitmap);
                        if (pager.DragToRight()) {    // 向右滑动，显示前一页
                            if (currentIndex == 0) return false;
                            pager.abortAnimation();
                            currentIndex--;
                            loadImage(mNextPageCanvas, currentIndex);
                        } else {        // 向左滑动，显示后一页
                            if (currentIndex + 1 == count) return false;
                            pager.abortAnimation();
                            currentIndex++;
                            loadImage(mNextPageCanvas, currentIndex);
                        }
                    } else if (e.getAction() == MotionEvent.ACTION_MOVE) {

                    } else if (e.getAction() == MotionEvent.ACTION_UP) {
                        if (!pager.canDragOver()) {
                            currentIndex = lastIndex;
                            currentBitmap = lastBitmap;
                        }
                    }

                    ret = pager.doTouchEvent(e);
                    return ret;
                }
                return false;
            }
        });
    }

    private void loadImage(final Canvas canvas, int index) {
        Bitmap bitmap = getBitmap(pages[index]);
        currentBitmap = bitmap;
        pagerFactory.onDraw(canvas, bitmap);
        pager.setBitmaps(mCurPageBitmap, mNextPageBitmap);
        pager.postInvalidate();
    }

    private Bitmap getBitmap(String name) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        ApplicationInfo appInfo = getApplicationInfo();
        int resID = getResources().getIdentifier(name, "drawable", appInfo.packageName);
        InputStream is = getResources().openRawResource(resID);
        Bitmap tempBitmap = BitmapFactory.decodeStream(is, null, opt);
        int width = tempBitmap.getWidth();
        int height = tempBitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(((float)screenWidth)/width, ((float)screenHeight)/height);
        Bitmap bitmap = Bitmap.createBitmap(tempBitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }
}
