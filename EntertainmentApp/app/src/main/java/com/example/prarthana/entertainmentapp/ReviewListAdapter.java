package com.example.prarthana.entertainmentapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ReviewHolder> {
    List<Review> data= Collections.emptyList();
    private LayoutInflater inflater;
    private ClickListener clickListener;

    public ReviewListAdapter(Context context,List<Review> data){
        inflater=LayoutInflater.from(context);
        this.data=data;
    }

    @NonNull
    @Override
    public ReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.review_row,parent,false);
        ReviewHolder holder=new ReviewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewHolder holder, int position) {
        Review current=data.get(position);
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        holder.authorName.setText(current.author_name);
        float rate= current.rating;
        holder.authorRating.setRating(rate);
        holder.authorTime.setText(format.format(current.date));
        holder.authorText.setText(current.text);
        if(current.profile_photo_url.length()>0)
            Picasso.get().load(current.profile_photo_url).into(holder.authorProfile);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setClickListener(ClickListener clickListener){
        this.clickListener=clickListener;
    }

    class ReviewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView authorName;
        RatingBar authorRating;
        TextView authorTime;
        TextView authorText;
        ImageView authorProfile;
        public ReviewHolder(View itemView) {
            super(itemView);
            authorName=(TextView)itemView.findViewById(R.id.authorName);
            authorRating=(RatingBar)itemView.findViewById(R.id.authorRating);
            authorTime=(TextView)itemView.findViewById(R.id.authorTime);
            authorText=(TextView)itemView.findViewById(R.id.authorText);
            authorProfile=(ImageView)itemView.findViewById(R.id.authorProfilePhoto);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position=getLayoutPosition();
            clickListener.itemClicked(v,data.get(position).author_url);
        }
    }
    public interface ClickListener{
        public void itemClicked(View view,String url);
    }
}
