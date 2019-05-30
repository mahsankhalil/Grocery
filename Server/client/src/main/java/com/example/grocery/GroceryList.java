package com.example.grocery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.grocery.Common.Common;
import com.example.grocery.Database.Database;
import com.example.grocery.Interface.ItemClickListener;
import com.example.grocery.Model.Grocery;
import com.example.grocery.ViewHolder.GroceryViewHolder;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.util.ArrayList;
import java.util.List;

public class GroceryList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference groceryList;

    String categoryId="";
    SwipeRefreshLayout swipeRefreshLayout;

    FirebaseRecyclerAdapter<Grocery,GroceryViewHolder> adapter;
    // search functionality

    FirebaseRecyclerAdapter<Grocery,GroceryViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;
    //Favourites
    Database localDB;

    //Facebook Share
    CallbackManager callbackManager;
    ShareDialog shareDialog;



    Target target= new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //Create Photo from Bitmap
            SharePhoto photo=new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class))
            {
                SharePhotoContent content=new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);
        //Init Facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        //Local DB
        localDB=new Database(this);

        //Firebase

        database = FirebaseDatabase.getInstance();
        groceryList=database.getReference("Grocery");
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Get Intent here
                if(getIntent()!=null)
                    categoryId=getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId!=null)
                {
                    if (Common.isConnectedToInternet(getBaseContext()))
                        loadListGrocery(categoryId);
                    else
                    {
                        Toast.makeText(GroceryList.this,"Please Check Your Connection !!",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                //Get Intent here
                if(getIntent()!=null)
                    categoryId=getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId!=null)
                {
                    if (Common.isConnectedToInternet(getBaseContext()))
                        loadListGrocery(categoryId);
                    else
                    {
                        Toast.makeText(GroceryList.this,"Please Check Your Connection !!",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        recyclerView=(RecyclerView) findViewById(R.id.recycler_grocery);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //search
        materialSearchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter Your Product");
        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // when user types their test ,we will change suggest list
                List<String> suggest = new ArrayList<String>();
                for(String search:suggestList)
                {
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                // when search bar is close
                // restore original adapter
                if(!enabled)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                // when search finishes
                // show reasult of search adapter
                startSearch(text);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });



    }
    private void startSearch(CharSequence text){
        searchAdapter = new FirebaseRecyclerAdapter<Grocery, GroceryViewHolder>(
                Grocery.class,
                R.layout.grocery_item,
                GroceryViewHolder.class,
                groceryList.orderByChild("Name").equalTo(text.toString()) // compare name

        ){
            @Override
            protected void populateViewHolder(GroceryViewHolder viewHolder, Grocery model, int position) {
                viewHolder.grocery_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.grocery_image);

                final Grocery local=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Toast.makeText(GroceryList.this,""+local.getName(),Toast.LENGTH_SHORT).show();
                        //Start New Activity

                        Intent groceryDetail=new Intent(GroceryList.this,GroceryDetails.class);
                        groceryDetail.putExtra("GroceryId",searchAdapter.getRef(position).getKey());  //send grocery id to new activity
                        startActivity(groceryDetail);
                    }
                });

            }
        };
        recyclerView.setAdapter(searchAdapter); // set adapter for recycler view is search results
    }

    private void loadSuggest() {

        groceryList.orderByChild("MenuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            Grocery item = postSnapshot.getValue(Grocery.class);
                            suggestList.add(item.getName());   // add name of product to suggest list
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void loadListGrocery(String categoryId) {
        adapter=new FirebaseRecyclerAdapter<Grocery, GroceryViewHolder>(Grocery.class,
                R.layout.grocery_item,GroceryViewHolder.class,
                groceryList.orderByChild("MenuId").equalTo(categoryId) //like Select * from Grocery where MenuId=
        ) {
            @Override
            protected void populateViewHolder(final GroceryViewHolder viewHolder,final Grocery model,final int position) {
                viewHolder.grocery_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.grocery_image);


                //Add favourites
                if (localDB.isFavorite(adapter.getRef(position).getKey()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);

                //Click to change state of favorites
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!localDB.isFavorite(adapter.getRef(position).getKey())) {
                            localDB.addToFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(GroceryList.this, "" + model.getName() + " was added to Favorites", Toast.LENGTH_SHORT).show();
                        } else {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(GroceryList.this, "" + model.getName() + " was removed from Favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//
//                //Click To Share
                viewHolder.share_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.with(getApplicationContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });


                final Grocery local=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Toast.makeText(GroceryList.this,""+local.getName(),Toast.LENGTH_SHORT).show();
                        //Start New Activity

                        Intent groceryDetail=new Intent(GroceryList.this,GroceryDetails.class);
                        groceryDetail.putExtra("GroceryId",adapter.getRef(position).getKey());  //send grocery id to new activity
                        startActivity(groceryDetail);
                    }
                });


            }
        };
        //set Adapter

        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

    }
}
