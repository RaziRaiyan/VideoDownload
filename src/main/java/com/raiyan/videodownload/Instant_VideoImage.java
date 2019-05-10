package com.raiyan.videodownload;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;

import static android.support.constraint.Constraints.TAG;

public class Instant_VideoImage extends FrameLayout {

    private Instant_CustomVideoView cvv;

    public Instant_VideoImage(Context context) {
        super(context);
        init();
    }

    public Instant_VideoImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Instant_VideoImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Instant_VideoImage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    public Instant_CustomVideoView getCustomVideoView() {
        Log.d(TAG, "getCustomVideoView: check called");
        return cvv;
    }


    private void init() {
        this.setTag("aah_vi");
        cvv = new Instant_CustomVideoView(getContext());
        this.addView(cvv);
    }

}
