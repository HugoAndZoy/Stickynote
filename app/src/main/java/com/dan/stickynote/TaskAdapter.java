package com.dan.stickynote;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<Task> mTaskList;



    static class ViewHolder extends RecyclerView.ViewHolder{
        View taskView;
        ImageView taskImage;
        TextView taskName;

        public ViewHolder(View view){
            super(view);
            taskView = view;
            taskImage = (ImageView) view.findViewById(R.id.fruit_image);
            taskName = (TextView) view.findViewById(R.id.fruit_name);
        }
    }

    public TaskAdapter(List<Task> taskList) {
        mTaskList = taskList;
    }


    @Override
    public TaskAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fruit_item, parent, false);

        final ViewHolder holder = new ViewHolder(view);
        holder.taskView.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                Task task = mTaskList.get(position);

                return true;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(TaskAdapter.ViewHolder holder, int position) {
        Task task = mTaskList.get(position);
        holder.taskImage.setImageResource(task.getImageId());
        holder.taskName.setText(task.getName());
    }

    @Override
    public int getItemCount() {
       return mTaskList.size();
    }


}