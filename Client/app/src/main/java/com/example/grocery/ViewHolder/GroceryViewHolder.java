package com.example.grocery.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.grocery.Interface.ItemClickListener;
import com.example.grocery.R;

public class GroceryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView grocery_name;
    public ImageView grocery_image,share_image,fav_image;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    private ItemClickListener itemClickListener;

    public GroceryViewHolder(@NonNull View itemView) {
        super(itemView);

        grocery_name=itemView.findViewById(R.id.grocery_name);
        grocery_image=itemView.findViewById(R.id.grocery_image);
        fav_image=itemView.findViewById(R.id.fav);
        share_image=(ImageView)itemView.findViewById(R.id.btnShare);

        itemView.setOnClickListener((View.OnClickListener) this);
    }

    @Override
    public void onClick(View view) {

        itemClickListener.onClick(view,getAdapterPosition(),false);

    }
}
