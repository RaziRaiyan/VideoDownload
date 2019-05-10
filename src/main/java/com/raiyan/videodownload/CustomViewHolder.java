package com.raiyan.videodownload;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.concurrent.Callable;

public class CustomViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = CustomViewHolder.class.getSimpleName();

    private Instant_VideoImage vi;
    private boolean isLooping = true;
    private boolean isPaused = false;

    public CustomViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void playVideo(){
        this.vi.getCustomVideoView().setPaused(false);
        this.vi.getCustomVideoView().startVideo();
    }

    public void initVideoView(String url, Activity _act) {

        Log.d(TAG, "initVideoView: called");

        this.vi.getCustomVideoView().setVisibility(View.VISIBLE);
        Uri uri = Uri.parse(url);
        this.vi.getCustomVideoView().setSource(uri);
        this.vi.getCustomVideoView().setLooping(isLooping);
        this.vi.getCustomVideoView().set_act(_act);
        this.vi.getCustomVideoView().setMyFuncIn(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return null;
            }
        });
    }

    public void setLooping(boolean looping) {
        isLooping = looping;
    }

    public void pauseVideo() {
        this.vi.getCustomVideoView().pauseVideo();
        this.vi.getCustomVideoView().setPaused(true);
    }

    public Instant_VideoImage getAah_vi() {
        return vi;
    }

    public void setAah_vi(Instant_VideoImage aah_vi) {
        this.vi = aah_vi;
    }


    public boolean isPlaying() {
        return this.vi.getCustomVideoView().isPlaying();
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isLooping() {
        return isLooping;
    }
}
