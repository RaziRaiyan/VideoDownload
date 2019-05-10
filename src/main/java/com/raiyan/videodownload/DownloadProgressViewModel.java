package com.raiyan.videodownload;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;

import java.util.Date;

public class DownloadProgressViewModel extends ViewModel {

    public enum DownloadState{
        PAUSED,STOPPED,DOWNLOADING,IDLE,COMPLETED,ERROR
    }

    private static final String TAG = "DownloadProgressViewModel";

    private static final String baseUrl = "https://res.cloudinary.com/raiyanrazi/video/upload/";

    private Request mRequest;
    private String file;
    private String relativeUrl;
    private int downloadId;

    private MutableLiveData<Integer> mProgress = new MutableLiveData<>();
    private MutableLiveData<DownloadState> downloadState = new MutableLiveData<>();

    public DownloadProgressViewModel(@NonNull String relativeUrl) {
        Date date = new Date();
        String suffix = String.valueOf(date.getTime()% 10000);
        file = Environment.getExternalStorageDirectory()+"/fetch_download/vid"+suffix+".mp4";
        mRequest = new Request(baseUrl+relativeUrl,file);
        this.relativeUrl = relativeUrl;
        mRequest.setPriority(Priority.HIGH);
        mRequest.setNetworkType(NetworkType.ALL);
    }

    public int getDownloadId() {
        return mRequest.getId();
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }

    public String getDownloadUrl() {
        return baseUrl+relativeUrl;
    }

    public String getRelativeUrl(){
        return relativeUrl;
    }

    public String getFile() {
        return file;
    }

    public Request getRequest() {
        return mRequest;
    }

    public LiveData<Integer> getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress.setValue(progress);
    }

    public LiveData<DownloadState> getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(DownloadState downloadState) {
        this.downloadState.setValue(downloadState);
    }
}
