package com.raiyan.videodownload;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class ProgressRecyclerViewAdapter extends RecyclerView.Adapter<ProgressRecyclerViewAdapter.ProgressViewHolder>{

    private Context mContext;
    private ArrayList<DownloadProgressViewModel> downloadList;

    public ProgressRecyclerViewAdapter(Context context, ArrayList<DownloadProgressViewModel> downloadList) {
        mContext = context;
        this.downloadList = downloadList;
    }

    @NonNull
    @Override
    public ProgressViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.download_item,viewGroup,false);
        return new ProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressViewHolder progressViewHolder, int position) {
        downloadList.get(position).getProgress().observe((MainActivity)mContext, integer -> {
            progressViewHolder.mProgressBar.setProgress(integer);
            progressViewHolder.tv_progress.setText(""+integer+"%");
        });
        String[] file = downloadList.get(position).getFile().split("/");
        String fileName = file[file.length-1];
        progressViewHolder.tv_fileName.setText(fileName);

        progressViewHolder.tv_position.setText(""+position+". ");
        downloadList.get(position).getDownloadState().observe((MainActivity) mContext, downloadState -> {
            switch (downloadState){
                case IDLE:
                    progressViewHolder.tv_download_state.setText("IDLE");
                    break;
                case PAUSED:
                    progressViewHolder.tv_download_state.setText("PAUSED");
                    break;
                case STOPPED:
                    progressViewHolder.tv_download_state.setText("STOPPED");
                    break;
                case DOWNLOADING:
                    progressViewHolder.tv_download_state.setText("DOWNLOADING");
                    break;
                case COMPLETED:
                    progressViewHolder.tv_download_state.setText("COMPLETED");
                    break;
            }
        });
    }

    @Override
    public int getItemCount() {
        return downloadList == null?0:downloadList.size();
    }

    class ProgressViewHolder extends RecyclerView.ViewHolder{


        private ProgressBar mProgressBar;
        private TextView tv_progress;
        private TextView tv_download_state, tv_position, tv_fileName;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mProgressBar = itemView.findViewById(R.id.progressBar);
            mProgressBar.setMax(100);
            this.tv_progress = itemView.findViewById(R.id.tv_progress);
            tv_download_state = itemView.findViewById(R.id.tv_download_state);
            tv_position = itemView.findViewById(R.id.tv_position);
            tv_fileName = itemView.findViewById(R.id.tv_file_name);
        }
    }
}
