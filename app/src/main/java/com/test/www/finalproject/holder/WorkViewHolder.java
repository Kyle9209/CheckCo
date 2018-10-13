package com.test.www.finalproject.holder;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.www.finalproject.R;
import com.test.www.finalproject.model.WorkModel;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kkmnb on 2017-09-15.
 */

public class WorkViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.state_iv) ImageView img;
    @BindView(R.id.state_tv) TextView state;
    @BindView(R.id.time_tv) TextView time;

    public WorkViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindToItem(WorkModel workModel){
        state.setText(workModel.getState());
        time.setText(workModel.getTime());
        if(workModel.getState().equals("출근")){
            img.setImageResource(R.drawable.enter);
            state.setTextColor(Color.parseColor("#004e66"));
            time.setTextColor(Color.parseColor("#004e66"));
        } else {
            img.setImageResource(R.drawable.exit);
            state.setTextColor(Color.parseColor("#d24f26"));
            time.setTextColor(Color.parseColor("#d24f26"));
        }
    }
}
