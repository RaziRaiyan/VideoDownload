package com.raiyan.videodownload;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;

public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<VideoRecyclerViewAdapter.VideoViewHolder> {

    private static final String TAG = "VideoAdapter";
    private Context mContext;
    private ArrayList<DownloadProgressViewModel> videoNumbers;


    private static int i = 0;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    public VideoRecyclerViewAdapter(Context context, ArrayList<DownloadProgressViewModel> videoNumbers) {
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
    public void onBindViewHolder(@NonNull VideoViewHolder videoViewHolder, int position) {
        Log.d(TAG, "onBindViewHolder: called");
    }

    @Override
    public int getItemCount() {
        return videoNumbers == null?0:videoNumbers.size();
    }

    private MediaSource buildStreamMediaSource(Uri uri){

        String userAgent = "stareInDigital";

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext,
                Util.getUserAgent(mContext,userAgent),BANDWIDTH_METER);
        MediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);

        return  mediaSource;

    }



    class VideoViewHolder extends RecyclerView.ViewHolder{
        ExoPlayer mExoPlayer;
        PlayerView mPlayerView;
        int currentWindow;
        long playBackPosition;
        boolean playWhenReady = false;
        boolean alreadyInitialized = false;


        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            mPlayerView = itemView.findViewById(R.id.video_player);
//            mImageView = itemView.findViewById(R.id.image_view);
//            mTextView = itemView.findViewById(R.id.tv_page_number);
        }

        public boolean isAlreadyInitialized() {
            return alreadyInitialized;
        }

        public void initializePlayer(){
            alreadyInitialized = true;
            Log.d(TAG, "initializePlayer: called");

            if(mExoPlayer == null){
                Log.d(TAG, "check initializePlayer: called "+i++);
                TrackSelection.Factory adaptiveTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);

                mExoPlayer = ExoPlayerFactory.newSimpleInstance(
                        new DefaultRenderersFactory(mContext),
                        new DefaultTrackSelector(adaptiveTrackSelectionFactory),
                        new DefaultLoadControl());

                mPlayerView.setPlayer(mExoPlayer);
                mExoPlayer.setPlayWhenReady(playWhenReady);
                mExoPlayer.seekTo(currentWindow,playBackPosition);
            }
        }

        public void pauseVideo(){
            if(mExoPlayer != null){
                mExoPlayer.setPlayWhenReady(false);
            }
        }

        public void releasePlayer(){
            if(mExoPlayer != null){
                Log.d(TAG, "check releasePlayer: called");
                playBackPosition = mExoPlayer.getCurrentPosition();
                currentWindow = mExoPlayer.getCurrentWindowIndex();
                playWhenReady = mExoPlayer.getPlayWhenReady();
                mExoPlayer.removeListener(null);
                mExoPlayer.release();
                mExoPlayer = null;
            }
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VideoViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Log.d(TAG, "check onViewAttachedToWindow: called");
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VideoViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
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
