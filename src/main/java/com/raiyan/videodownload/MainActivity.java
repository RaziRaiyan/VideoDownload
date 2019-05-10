package com.raiyan.videodownload;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;
import com.tonyodev.fetch2okhttp.OkHttpDownloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Fetch mFetch;
    private FetchListener fetchListener;

    private static final int REQUEST_PERMISSION_CODE = 1;

    private static String[] mediaUrls = new String[]{
            "v1557209324/four_xqjdws.mp4",
            "v1557209325/one_prhun8.mp4",
            "v1557209326/three_vmicvt.mp4",
            "v1557209315/two_yhwshb.mp4",
            "v1557384148/video_1557383485365__1_ra3nvs.mp4"
    };

    private RecyclerView mRecyclerView;
    private ProgressRecyclerViewAdapter mProgressRecyclerViewAdapter;

    //Video RecyclerView
    private CustomExoRecyclerView mVideoRecyclerView;
    private VideoRecyclerViewAdapter mVideoRecyclerViewAdapter;

    private Button downloadButton;

//    WriteFileTask[] mWriteFileTasks = new WriteFileTask[mediaUrls.length];

    private ArrayList<DownloadProgressViewModel> downloadProgressViewModels;
    private HashMap<Integer, DownloadProgressViewModel> mViewModelMap = new HashMap<>();
    private static int last = -1;
    private static int first = -1;

    private String dirPath = Environment.getExternalStorageDirectory() + "/fetchDownload";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG, "onCreate: called");

        downloadButton = findViewById(R.id.btn_start_download);

        DownloadProgressViewModel downloadProgressViewModel1 = new DownloadProgressViewModel(mediaUrls[0]);
        DownloadProgressViewModel downloadProgressViewModel2 = new DownloadProgressViewModel(mediaUrls[1]);
        DownloadProgressViewModel downloadProgressViewModel3 = new DownloadProgressViewModel(mediaUrls[2]);
        DownloadProgressViewModel downloadProgressViewModel4 = new DownloadProgressViewModel(mediaUrls[3]);

        downloadProgressViewModels = new ArrayList<>();
        downloadProgressViewModels.add(downloadProgressViewModel1);
        downloadProgressViewModels.add(downloadProgressViewModel2);
        downloadProgressViewModels.add(downloadProgressViewModel3);
        downloadProgressViewModels.add(downloadProgressViewModel4);

        for(DownloadProgressViewModel viewModel:downloadProgressViewModels){
            mViewModelMap.put(viewModel.getDownloadId(),viewModel);
        }

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mProgressRecyclerViewAdapter = new ProgressRecyclerViewAdapter(this, downloadProgressViewModels);
        mRecyclerView.setAdapter(mProgressRecyclerViewAdapter);

        mVideoRecyclerView = findViewById(R.id.video_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mVideoRecyclerView.setLayoutManager(linearLayoutManager);
        mVideoRecyclerViewAdapter = new VideoRecyclerViewAdapter(this, downloadProgressViewModels);
        mVideoRecyclerView.setAdapter(mVideoRecyclerViewAdapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mVideoRecyclerView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mVideoRecyclerView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                first = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                if (first != -1) {
                    Log.d(TAG, "onScrollChange: completely visible first item position = " + first);
                    if (mFetch != null && last != first) {
                        mFetch.resume(downloadProgressViewModels.get(first).getRequest().getId());
                        if (last != -1) {
                            mFetch.pause(downloadProgressViewModels.get(last).getRequest().getId());
                        }
                    }
                    last = first;
                }
            });
        }

        mRecyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int i, int i1) {
                return false;
            }
        });

        requestPermission();

//        PRDownloader.initialize(getApplicationContext());
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(4)
                .setHttpDownloader(new OkHttpDownloader(okHttpClient))
                .build();
        mFetch = Fetch.Impl.getInstance(fetchConfiguration);
        
        mFetch.close();
        mFetch = Fetch.Impl.getInstance(fetchConfiguration);

        File file = new File(Environment.getExternalStorageDirectory() + "/fetch_download");
        if (file.exists()) {
            Log.d(TAG, "onCreate: file directory exists");
            try {

                File canonicalFile = file.getCanonicalFile();
                String canonicalFilePath = canonicalFile.toString();
                Log.d(TAG, "onCreate: canonical file path : "+canonicalFilePath);
                if (file.exists()) {
                    boolean b = getApplicationContext().deleteFile(file.getName());
                    if(b){
                        Log.d(TAG, "onCreate: File successfully deleted");
                    }else {
                        Log.d(TAG, "onCreate: unable to delete existing file");
                    }
                }
            }catch (IOException e){
                Log.d(TAG, "onCreate: something wrong happen while deleting file");
            }
        }
        downloadButton.setEnabled(false);
        downloadButton.setOnClickListener(v -> {
            downloadFileUsingFetch();
//                enqueueDownloadPR(i);
            downloadButton.setEnabled(false);
        });
    }

    private void downloadFileUsingFetch() {
        fetchListener = new FetchListener() {
            @Override
            public void onAdded(@NotNull Download download) {
                Toast.makeText(MainActivity.this, "Download Added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onQueued(@NotNull Download download, boolean b) {

            }

            @Override
            public void onWaitingNetwork(@NotNull Download download) {

            }

            @Override
            public void onCompleted(@NotNull Download download) {
                Log.d(TAG, "onCompleted: " + download.getFileUri());
                mFetch.remove(download.getId());
                if(download.getProgress() != 100){
                    mFetch.retry(download.getId());
                    Log.d(TAG, "onCompleted: retrying");
                    return;
                }
                if(mViewModelMap.get(download.getId()) != null)
                    mViewModelMap.get(download.getId()).setDownloadState(DownloadProgressViewModel.DownloadState.COMPLETED);
            }

            @Override
            public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                Toast.makeText(MainActivity.this, "Download Error", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Download Error!!: " + error);
                if(mViewModelMap.get(download.getId()) != null)
                    mViewModelMap.get(download.getId()).setDownloadState(DownloadProgressViewModel.DownloadState.ERROR);
//                if (download.getId() == downloadProgressViewModels.get(0).getRequest().getId()) {
//                    downloadProgressViewModels.get(0).setDownloadState(DownloadProgressViewModel.DownloadState.ERROR);
//                } else if (download.getId() == downloadProgressViewModels.get(1).getRequest().getId()) {
//                    downloadProgressViewModels.get(1).setDownloadState(DownloadProgressViewModel.DownloadState.ERROR);
//                } else if (download.getId() == downloadProgressViewModels.get(2).getRequest().getId()) {
//                    downloadProgressViewModels.get(2).setDownloadState(DownloadProgressViewModel.DownloadState.ERROR);
//                } else if (download.getId() == downloadProgressViewModels.get(3).getRequest().getId()) {
//                    downloadProgressViewModels.get(3).setDownloadState(DownloadProgressViewModel.DownloadState.ERROR);
//                }
                mFetch.retry(download.getId());
            }

            @Override
            public void onDownloadBlockUpdated(@NotNull Download download, @NotNull DownloadBlock downloadBlock, int i) {

            }

            @Override
            public void onStarted(@NotNull Download download, @NotNull List<? extends DownloadBlock> list, int i) {
                Toast.makeText(MainActivity.this, "Download started", Toast.LENGTH_SHORT).show();
                if(mViewModelMap.get(download.getId()) != null)
                    mViewModelMap.get(download.getId()).setDownloadState(DownloadProgressViewModel.DownloadState.DOWNLOADING);
//
//                if (download.getId() == downloadProgressViewModels.get(0).getRequest().getId()) {
//                    downloadProgressViewModels.get(0).setDownloadState(DownloadProgressViewModel.DownloadState.DOWNLOADING);
//                } else if (download.getId() == downloadProgressViewModels.get(1).getRequest().getId()) {
//                    downloadProgressViewModels.get(1).setDownloadState(DownloadProgressViewModel.DownloadState.DOWNLOADING);
//                } else if (download.getId() == downloadProgressViewModels.get(2).getRequest().getId()) {
//                    downloadProgressViewModels.get(2).setDownloadState(DownloadProgressViewModel.DownloadState.DOWNLOADING);
//                } else if (download.getId() == downloadProgressViewModels.get(3).getRequest().getId()) {
//                    downloadProgressViewModels.get(3).setDownloadState(DownloadProgressViewModel.DownloadState.DOWNLOADING);
//                }
            }

            @Override
            public void onProgress(@NotNull Download download, long l, long l1) {

                if(first!= -1){
                    if (download.getProgress() >= 30 && downloadProgressViewModels.get(first).getRequest().getId() != download.getId()) {
                        mFetch.pause(download.getId());
                    }
                }
                if(mViewModelMap.get(download.getId()) != null)
                    mViewModelMap.get(download.getId()).setProgress(download.getProgress());

//                if (download.getId() == downloadProgressViewModels.get(0).getRequest().getId()) {
//                    downloadProgressViewModels.get(0).setProgress(download.getProgress());
//                } else if (download.getId() == downloadProgressViewModels.get(1).getRequest().getId()) {
//                    downloadProgressViewModels.get(1).setProgress(download.getProgress());
//                } else if (download.getId() == downloadProgressViewModels.get(2).getRequest().getId()) {
//                    downloadProgressViewModels.get(2).setProgress(download.getProgress());
//                } else if (download.getId() == downloadProgressViewModels.get(3).getRequest().getId()) {
//                    downloadProgressViewModels.get(3).setProgress(download.getProgress());
//                }
            }

            @Override
            public void onPaused(@NotNull Download download) {
                Log.d(TAG, "onPaused: download paused for " + download.getId() + " at" + download.getProgress() + "%");
                if(mViewModelMap.get(download.getId()) != null)
                    mViewModelMap.get(download.getId()).setDownloadState(DownloadProgressViewModel.DownloadState.PAUSED);
//                if (download.getId() == downloadProgressViewModels.get(0).getRequest().getId()) {
//                    downloadProgressViewModels.get(0).setDownloadState(DownloadProgressViewModel.DownloadState.PAUSED);
//                } else if (download.getId() == downloadProgressViewModels.get(1).getRequest().getId()) {
//                    downloadProgressViewModels.get(1).setDownloadState(DownloadProgressViewModel.DownloadState.PAUSED);
//                } else if (download.getId() == downloadProgressViewModels.get(2).getRequest().getId()) {
//                    downloadProgressViewModels.get(2).setDownloadState(DownloadProgressViewModel.DownloadState.PAUSED);
//                } else if (download.getId() == downloadProgressViewModels.get(3).getRequest().getId()) {
//                    downloadProgressViewModels.get(3).setDownloadState(DownloadProgressViewModel.DownloadState.PAUSED);
//                }
            }

            @Override
            public void onResumed(@NotNull Download download) {

                if(mViewModelMap.get(download.getId()) != null)
                    mViewModelMap.get(download.getId()).setDownloadState(DownloadProgressViewModel.DownloadState.DOWNLOADING);
//                if (download.getId() == downloadProgressViewModels.get(0).getRequest().getId()) {
//                    downloadProgressViewModels.get(0).setDownloadState(DownloadProgressViewModel.DownloadState.DOWNLOADING);
//                } else if (download.getId() == downloadProgressViewModels.get(1).getRequest().getId()) {
//                    downloadProgressViewModels.get(1).setDownloadState(DownloadProgressViewModel.DownloadState.DOWNLOADING);
//                } else if (download.getId() == downloadProgressViewModels.get(2).getRequest().getId()) {
//                    downloadProgressViewModels.get(2).setDownloadState(DownloadProgressViewModel.DownloadState.DOWNLOADING);
//                } else if (download.getId() == downloadProgressViewModels.get(3).getRequest().getId()) {
//                    downloadProgressViewModels.get(3).setDownloadState(DownloadProgressViewModel.DownloadState.DOWNLOADING);
//                }

            }

            @Override
            public void onCancelled(@NotNull Download download) {

            }

            @Override
            public void onRemoved(@NotNull Download download) {

            }

            @Override
            public void onDeleted(@NotNull Download download) {

            }
        };
        mFetch.addListener(fetchListener);

        mFetch.enqueue(downloadProgressViewModels.get(0).getRequest(),
                request -> Log.d(TAG, "call: requested " + request.getId()),
                error -> Log.d(TAG, "call: Error!! " + error));

        mFetch.enqueue(downloadProgressViewModels.get(1).getRequest(),
                request -> Log.d(TAG, "call: requested" + request.getId()),
                error -> Log.d(TAG, "call: Error!! " + error));

        mFetch.enqueue(downloadProgressViewModels.get(2).getRequest(),
                request -> Log.d(TAG, "call: requested" + request.getId()),
                error -> Log.d(TAG, "call: Error!! " + error));

        mFetch.enqueue(downloadProgressViewModels.get(3).getRequest(),
                request -> Log.d(TAG, "call: requested" + request.getId()),
                error -> Log.d(TAG, "call Error!! " + error));


    }


    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this,"you have already granted perimission",Toast.LENGTH_SHORT).show();
        } else {
            requestStoragePermission();
        }
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Permission needed to read data")
                    .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss()).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void downloadFile(String relativeUrl, final int index) {

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://d1qddl39u3tib7.cloudfront.net/");

        Retrofit retrofit = builder.build();
        FileDownloadClient fileDownloadClient = retrofit.create(FileDownloadClient.class);
        Call<ResponseBody> call = fileDownloadClient.downloadFileStream(relativeUrl);
        call.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                Log.d(TAG, "server contacted and has file");
                Toast.makeText(MainActivity.this, "Server connection successful", Toast.LENGTH_SHORT).show();
                if (response.isSuccessful()) {
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            writeFileToDisk(response.body(), 0);
                            return null;
                        }
                    }.execute();
                } else {
                    Log.d(TAG, "Server contact failed");
                    Toast.makeText(MainActivity.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

//    private class WriteFileTask extends AsyncTask<ResponseBody, Pair<Integer,Long>,String>{
//        int index;
//        ProgressBar mProgressBar;
//        TextView mTextView;
//
//        public WriteFileTask(int index) {
//            this.index = index;
//            if(index == 0){
//                mProgressBar = mProgressBar1;
//                mTextView = mTextView1;
//            }else {
//                mProgressBar = mProgressBar2;
//                mTextView = mTextView2;
//            }
//            mProgressBar.setMax(100);
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(ResponseBody... responseBodies) {
//            boolean writtenToFile = writeFileToDisk(responseBodies[0],
//
//                    index);
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(Pair<Integer, Long>... progress) {
//            Log.d(TAG, "onProgressUpdate: "+progress[0].second+" ");
//            if(progress[0].first == 100){
//                Toast.makeText(getApplicationContext(),"File Downloaded successfully",Toast.LENGTH_SHORT).show();
//            }
//
//            if(progress[0].second > 0){
//                int currentProgress = (int)((double) progress[0].first / (double)progress[0].second*100);
//                mProgressBar.setProgress(currentProgress);
//                mTextView.setText("Progress "+currentProgress+"%" );
//            }
//
//            if(progress[0].first == -1){
//                Log.d(TAG, "onProgressUpdate: Download Failed");
//            }
//        }
//
//        public void doProgress(Pair<Integer,Long> progressDetails){
//            publishProgress(progressDetails);
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//        }
//    }

    private boolean writeFileToDisk(ResponseBody body, int index) {
        try {
            Date date = new Date();
            long time = date.getTime();
            File folder = new File(Environment.getExternalStorageDirectory() + "/dummy_download");
            if (!folder.exists()) {
                folder.mkdir();
            }

            File downloadFile = new File(folder, "video_" + time + ".mp4");
            Log.d(TAG, "downloaded file path: " + downloadFile.getAbsolutePath());
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                int fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(downloadFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    Log.d(TAG, "file downloaded: " + fileSizeDownloaded + " of " + fileSize);
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;

            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart: called");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop: called");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart: called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: called");
        if (mFetch != null) {
            mFetch.removeListener(fetchListener);
            mFetch.close();

            Log.d(TAG, "onDestroy: Fetch is not null");


        } else {
            Log.d(TAG, "onDestroy: Fetch is null");
        }
        if (downloadProgressViewModels != null) {

        }
    }


}
