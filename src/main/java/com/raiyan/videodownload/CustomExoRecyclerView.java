package com.raiyan.videodownload;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

public class CustomExoRecyclerView extends RecyclerView {
    private static final String TAG = CustomRecyclerView.class.getSimpleName();

    private static int startPosition = 0 ;
    private static int endPosition = 1;

    VideoRecyclerViewAdapter mVideoRecyclerViewAdapter;

    public CustomExoRecyclerView(@NonNull Context context) {
        super(context);
    }

    public CustomExoRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomExoRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);
        mVideoRecyclerViewAdapter = (VideoRecyclerViewAdapter) adapter;
        addCustomScrollListener();
        initiateNextFourVideos();
    }

    private void addCustomScrollListener() {
        this.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                initiateNextFourVideos();
                pauseAllVideo();
                playVideo();
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void pauseAllVideo() {
        for(int i =startPosition;i<endPosition;i++){
            VideoRecyclerViewAdapter.VideoViewHolder videoViewHolder =
                    (VideoRecyclerViewAdapter.VideoViewHolder) findViewHolderForAdapterPosition(i);
            if(videoViewHolder != null && videoViewHolder.isAlreadyInitialized()){
                videoViewHolder.pauseVideo();
            }
        }
    }

    private void playVideo() {
        int position = ((LinearLayoutManager)getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        if(position != -1){
            VideoRecyclerViewAdapter.VideoViewHolder videoViewHolder =  
                    (VideoRecyclerViewAdapter.VideoViewHolder) findViewHolderForAdapterPosition(position);
            if(videoViewHolder != null && videoViewHolder.isAlreadyInitialized()){
                videoViewHolder.mExoPlayer.setPlayWhenReady(true);
            }else {
                Log.d(TAG, "playVideo: check player not yet initialized");
            }
        }
    }

    private void initiateNextFourVideos(){
        int first = ((LinearLayoutManager)getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        if(first != -1){
            startPosition = first;
        }
        endPosition = startPosition+4;
        if(endPosition > mVideoRecyclerViewAdapter.getVideoNumbers().size()){
            endPosition = mVideoRecyclerViewAdapter.getVideoNumbers().size();
        }
        Log.d(TAG, "initiateNextFourVideos: check startPosition = "+startPosition+" endPosition = "+endPosition);
        for(int i = startPosition;i<endPosition;i++){
            ViewHolder holder = findViewHolderForAdapterPosition(i);
            VideoRecyclerViewAdapter.VideoViewHolder videoViewHolder = (VideoRecyclerViewAdapter.VideoViewHolder) holder;
            if(videoViewHolder != null && !videoViewHolder.alreadyInitialized){
                setupExoPlayer(videoViewHolder,i);
//                startNewThreadForVideo(videoViewHolder,i);
            }else {
                Log.d(TAG, "initiateNextFourVideos: check maybe viewholder is null");
            }
        }
    }
    
    private void startNewThreadForVideo(VideoRecyclerViewAdapter.VideoViewHolder videoViewHolder,int position){
        new Thread(() -> {
            videoViewHolder.initializePlayer();
            setupExoPlayer(videoViewHolder,position);
        }).start();
    }


    private void setupExoPlayer(VideoRecyclerViewAdapter.VideoViewHolder videoViewHolder,int position){
        if (!videoViewHolder.alreadyInitialized) {
            Log.d(TAG, "setupExoPlayer: exoplayer initialized and called for viewholder position: "+position);
            videoViewHolder.initializePlayer();
            MediaSource mediaSource = buildMediaSource(mVideoRecyclerViewAdapter.getVideoNumbers().get(position).getDownloadUrl());
            videoViewHolder.mExoPlayer.prepare(mediaSource, true, false);
            videoViewHolder.mExoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            videoViewHolder.mExoPlayer.setPlayWhenReady(false);
            videoViewHolder.mExoPlayer.addListener(new Player.EventListener() {
                @Override
                public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
                    Log.d(TAG, "checkExoListener onTimelineChanged: called");
                }

                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                    Log.d(TAG, "checkExoListener onTracksChanged: called");
                }

                @Override
                public void onLoadingChanged(boolean isLoading) {
                    Log.d(TAG, "checkExoListener onLoadingChanged: called");
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    Log.d(TAG, "checkExoListener onPlayerStateChanged: called");
                }

                @Override
                public void onRepeatModeChanged(int repeatMode) {
                    Log.d(TAG, "checkExoListener onRepeatModeChanged: called");
                }

                @Override
                public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                    Log.d(TAG, "checkExoListener onShuffleModeEnabledChanged: called");
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    Log.d(TAG, "checkExoListener onPlayerError: called");
                }

                @Override
                public void onPositionDiscontinuity(int reason) {
                    Log.d(TAG, "checkExoListener onPositionDiscontinuity: called");
                }

                @Override
                public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                    Log.d(TAG, "checkExoListener onPlaybackParametersChanged: called");
                }

                @Override
                public void onSeekProcessed() {
                    Log.d(TAG, "checkExoListener onSeekProcessed: called");
                    videoViewHolder.mExoPlayer.getCurrentPosition();
                }
            });
        }
    }


    private MediaSource buildMediaSource(String fileUrl){
        Uri uri  = Uri.parse(fileUrl);
        String userAgent = "stareInDigital";
        if(uri.getLastPathSegment().contains("mp4")){
            return new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory(userAgent)).createMediaSource(uri);
        }else {
            return null;
        }

    }

}
