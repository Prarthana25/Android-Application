package com.example.prarthana.entertainmentapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.PlaceViewHolder> {
    List<PlacesResult> data= Collections.emptyList();
    private LayoutInflater inflater;
    private ClickListener clickListener;
    private Context context;
    private View view;
    public FavoriteAdapter(Context context,List<PlacesResult> data,View view){
        this.context=context;
        this.inflater=LayoutInflater.from(context);
        this.data=data;
        this.view=view;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.favorite_row,parent,false);
        PlaceViewHolder holder=new PlaceViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        PlacesResult current=data.get(position);
        holder.placeName.setText(current.place_name);
        holder.placeAddress.setText(current.place_address);
        Picasso.get().load(current.place_category).into(holder.placeCategory);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
    public void setClickListener(ClickListener clickListener){
        this.clickListener=clickListener;
    }
    class PlaceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView placeName;
        TextView placeAddress;
        ImageView placeCategory;
        ImageButton placeFavButton;
        public PlaceViewHolder(View itemView) {
            super(itemView);
            placeName=(TextView)itemView.findViewById(R.id.fav_name);
            placeCategory=(ImageView)itemView.findViewById(R.id.fav_category);
            placeAddress=(TextView)itemView.findViewById(R.id.fav_address);
            placeFavButton=(ImageButton)itemView.findViewById(R.id.favButton1);
            itemView.setOnClickListener(this);
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position=getLayoutPosition();
                    placeFavButton.setImageResource(R.drawable.star_blank);
                    removeFavorite(getLayoutPosition());
                }
            };
            placeFavButton.setOnClickListener(onClickListener);
        }

        public void removeFavorite(int position){
            String message=data.get(position).place_name+" is removed from favorite";
            Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPreferences=context.getSharedPreferences("Favorite",context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            int count=sharedPreferences.getInt("FavCount",0);
            String key=data.get(position).place_id;
            int counter=sharedPreferences.getInt(key,0);
            count--;
            editor.remove("Fav"+counter);
            editor.remove(key);
            editor.putInt("FavCount",count);
            editor.commit();

            data.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,data.size());

            RelativeLayout noFavResult=(RelativeLayout)view.findViewById(R.id.favNoResult);
            if(count==0)
                noFavResult.setVisibility(View.VISIBLE);
            else
                noFavResult.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            int position=getLayoutPosition();
            PlacesResult placesResult=data.get(position);
            clickListener.itemClicked(v,placesResult,placesResult.place_id);
        }
    }
    public interface ClickListener{
        public void itemClicked(View view,PlacesResult placesResult,String place_id);
    }
}
