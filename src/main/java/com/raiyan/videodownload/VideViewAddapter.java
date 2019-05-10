package com.raiyan.videodownload;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class VideViewAddapter extends  RecyclerView.Adapter<VideViewAddapter.VideoViewHolder>{

    private static final String TAG = "VideoAdapter";
    private Context mContext;
    private ArrayList<DownloadProgressViewModel> videoNumbers;
    private static int i = 0;

    public VideViewAddapter(Context context, ArrayList<DownloadProgressViewModel> videoNumbers) {
        mContext = context;
        this.videoNumbers = videoNumbers;
    }

    public Context getContext() {
        return mContext;
    }

    public ArrayList<DownloadProgressViewModel> getVideoNumbers() {
        return videoNumbers;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.player_item,viewGroup,false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder videoViewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return videoNumbers == null?0:videoNumbers.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder{
        private Instant_VideoImage mVideoImage;
        private int position = 0;
        private String videoUrl;
        private boolean isPaused = false;
        private boolean isLooping = true;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            // ToDo initialize this if using this adapter
//            mVideoImage = itemView.findViewById(R.id.video_view);
        }

        public void playVideo() {
            Log.d(TAG, "playVideo: check called");
            this.mVideoImage.getCustomVideoView().setPaused(false);
            this.mVideoImage.getCustomVideoView().startVideo();
        }


        public void initVideoView(String url, Activity _act) {
            Log.d(TAG, "initVideoView: check called");
            if(this.mVideoImage.getCustomVideoView() == null){
                Log.d(TAG, "initVideoView: Alas check");
            }
            this.mVideoImage.getCustomVideoView().setVisibility(View.VISIBLE);
            Log.d(TAG, "initVideoView: check visible");
            Uri uri = Uri.parse(url);
            this.mVideoImage.getCustomVideoView().setSource(uri);
            Log.d(TAG, "initVideoView: check uri set");
            this.mVideoImage.getCustomVideoView().setLooping(isLooping);
            Log.d(TAG, "initVideoView: check looping set");
            this.mVideoImage.getCustomVideoView().set_act(_act);
            Log.d(TAG, "initVideoView: check initiated");
        }

        public void releaseVideoView(){
            Log.d(TAG, "releaseVideoView: check called");
            mVideoImage.getCustomVideoView().releaseMediaPlayer();
        }

        public void pauseVideo() {
            this.mVideoImage.getCustomVideoView().pauseVideo();
            this.mVideoImage.getCustomVideoView().setPaused(true);
        }

        public void setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
        }

        public String getVideoUrl() {
            return videoUrl + "";
        }

        public boolean isPaused() {
            return isPaused;
        }

//
//        public void startVideo(String uri){
//            Log.d(TAG, "startVideo: called");
//            Uri fileUri = Uri.parse(uri);
//            mVideoView.setVideoURI(fileUri);
//            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    mVideoView.seekTo(position);
//                    if(position == 0){
//                        mVideoView.start();
//                    }
//                }
//            });
//            mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    mVideoView.seekTo(0);
//                }
//            });
//            isStarted = true;
//        }
//
//        public boolean isPaused() {
//            return isPaused;
//        }
//
//        public boolean isStarted() {
//            return isStarted;
//        }
//
//        public void pauseVideo(){
//            if(!isPaused){
//                mVideoView.pause();
//                isStarted = false;
//                isPaused = true;
//            }
//        }

//        public void playVideo(){
//            if(isPaused){
//                mVideoView.start();
//            }
//        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VideoViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Log.d(TAG, "check onViewAttachedToWindow: called");
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VideoViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.pauseVideo();
        holder.releaseVideoView();
        Log.d(TAG, "check onViewDetachedFromWindow: called");
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Log.d(TAG, "check onAttachedToRecyclerView: called");
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        Log.d(TAG, "check onDetachedFromRecyclerView: called");
    }
}
