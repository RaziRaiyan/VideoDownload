package com.raiyan.videodownload;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;

public class CustomRecyclerView extends RecyclerView {

    private static final String TAG = CustomViewHolder.class.getSimpleName();

    VideViewAddapter mVideViewAddapter;

    private static int firstItem = -1;
    private static int lastItem = -1;

    public CustomRecyclerView(@NonNull Context context) {
        super(context);
    }

    public CustomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);
        mVideViewAddapter = (VideViewAddapter) adapter;
        playFirstVideo();
        addCustomOnScrollListener();
    }

    private void playFirstVideo() {
        Log.d(TAG, "playFirstVideo: called");
        mVideViewAddapter.getVideoNumbers().get(0).getDownloadState().observe((MainActivity)mVideViewAddapter.getContext(), downloadState -> {
            if((downloadState == DownloadProgressViewModel.DownloadState.DOWNLOADING)|| (downloadState == DownloadProgressViewModel.DownloadState.PAUSED)
            ||(downloadState == DownloadProgressViewModel.DownloadState.COMPLETED)){
                int firstPosition = 0;
                final RecyclerView.ViewHolder holder = findViewHolderForAdapterPosition(0);
                VideViewAddapter.VideoViewHolder videoViewHolder = (VideViewAddapter.VideoViewHolder) holder;
                if(videoViewHolder!= null)
                    setupMediaPlayer(videoViewHolder,0);
            }
        });
    }



    private void addCustomOnScrollListener(){
        this.addOnScrollListener(new RecyclerView.OnScrollListener(){

            int lastVisiblePosition = ((LinearLayoutManager)getLayoutManager()).findLastCompletelyVisibleItemPosition();

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                firstItem = ((LinearLayoutManager)getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                if (firstItem != -1) {
                    Log.d(TAG, "onScrollChange: completely visible first item position = " + firstItem);
                    if (lastItem != firstItem) {
                        playAvailableVideo(newState);
                        if (lastItem != -1) {
                            removePlayer();
                        }
                    }
                    lastItem = firstItem;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void removePlayer(){
        if(lastItem != -1){
            final RecyclerView.ViewHolder holder = findViewHolderForAdapterPosition(lastItem);
            VideViewAddapter.VideoViewHolder videoViewHolder = (VideViewAddapter.VideoViewHolder) holder;
            if(videoViewHolder!= null){
                Log.d(TAG, "removePlayer: check called");
                videoViewHolder.releaseVideoView();
            }
        }

    }

    private void playAvailableVideo(int newState){

        if(newState == 0){
            int firstVisiblePosition = ((LinearLayoutManager)getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            int lastVisiblePosition = ((LinearLayoutManager)getLayoutManager()).findLastCompletelyVisibleItemPosition();

            if(firstVisiblePosition != -1 ){
                final RecyclerView.ViewHolder holder = findViewHolderForAdapterPosition(firstVisiblePosition);
                try{
                    VideViewAddapter.VideoViewHolder videoViewHolder = (VideViewAddapter.VideoViewHolder) holder;
//                    setupExoPlayer(videoViewHolder,firstVisiblePosition);
//                    videoViewHolder.startVideo(mVideViewAddapter.getVideoNumbers().get(firstVisiblePosition).getFile());
                    setupMediaPlayer(videoViewHolder,firstVisiblePosition);
                }catch (Exception e){

                }
            }
        }
    }

    private void setupMediaPlayer(VideViewAddapter.VideoViewHolder videoViewHolder,int position){
        Log.d(TAG, "setupMediaPlayer: called");
        DownloadProgressViewModel.DownloadState downloadState = mVideViewAddapter.getVideoNumbers().get(position).getDownloadState().getValue();
        videoViewHolder.initVideoView(mVideViewAddapter.getVideoNumbers().get(position).getFile(),(MainActivity)mVideViewAddapter.getContext());

        mVideViewAddapter.getVideoNumbers().get(position).getProgress().observe((MainActivity)mVideViewAddapter.getContext(), integer -> {
            if(integer == null){
                return;
            }
            Log.d(TAG, "setupMediaPlayer: check observer attached");
            if(integer>=30
//                && ((downloadState == DownloadProgressViewModel.DownloadState.DOWNLOADING)
//            ||(downloadState == DownloadProgressViewModel.DownloadState.PAUSED)
//            || (downloadState == DownloadProgressViewModel.DownloadState.COMPLETED))
                    && !videoViewHolder.isPaused()
            ){
                Log.d(TAG, "setupMediaPlayer: all set check calling playVideo");
                videoViewHolder.playVideo();
            }
        });

        Integer integer = mVideViewAddapter.getVideoNumbers().get(position).getProgress().getValue();
        if(integer == null){
            return;
        }
        Log.d(TAG, "setupMediaPlayer: check progress = "+integer+"isPaused : "+videoViewHolder.isPaused());

        if(integer>=30
//                && ((downloadState == DownloadProgressViewModel.DownloadState.DOWNLOADING)
//            ||(downloadState == DownloadProgressViewModel.DownloadState.PAUSED)
//            || (downloadState == DownloadProgressViewModel.DownloadState.COMPLETED))
                && !videoViewHolder.isPaused()
        ){
            Log.d(TAG, "setupMediaPlayer: all set check calling playVideo");
            videoViewHolder.playVideo();
        }


    }


    private MediaSource buildMediaSource(String fileName){

        String playerInfo = Util.getUserAgent(mVideViewAddapter.getContext(), "ExoPlayerInfo");
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                mVideViewAddapter.getContext(), playerInfo
        );
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .setExtractorsFactory(new DefaultExtractorsFactory())
                .createMediaSource(Uri.parse(fileName));

        return mediaSource;
    }

    private MediaSource buildMediaSourceFromFile(String filename){

        DataSpec dataSpec = new DataSpec(Uri.parse(filename));
        final FileDataSource fileDataSource = new FileDataSource();
        try{
            fileDataSource.open(dataSpec);
        }catch (FileDataSource.FileDataSourceException e){
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };

        MediaSource mediaSource = new ExtractorMediaSource.Factory(factory)
                .setExtractorsFactory(new DefaultExtractorsFactory())
                .createMediaSource(Uri.parse(filename));

        return mediaSource;
    }

}























