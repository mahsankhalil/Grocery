package com.example.grocery;

import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.grocery.Common.Common;
import com.example.grocery.Database.Database;
import com.example.grocery.Model.Grocery;
import com.example.grocery.Model.Order;
import com.example.grocery.Model.Rating;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Queue;

public class GroceryDetails extends AppCompatActivity implements RatingDialogListener {

    TextView grocery_name,grocery_price,grocery_description;
    ImageView grocery_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart, btnRating;
    ElegantNumberButton numberButton;

    RatingBar ratingBar;
    String groceryId="";

    FirebaseDatabase database;
    DatabaseReference grocery;
    DatabaseReference ratingTbl;

    Grocery currentGrocery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_details);


        //Firebase
        database = FirebaseDatabase.getInstance();
        grocery= database.getReference("Grocery");
        ratingTbl=database.getReference("Rating");

        //Init view
        numberButton = (ElegantNumberButton)findViewById(R.id.number_button);
        btnCart=(FloatingActionButton)findViewById(R.id.btnCart);
        btnRating=(FloatingActionButton)findViewById(R.id.btn_rating);
        ratingBar=(RatingBar)findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Order(
                        groceryId,
                        currentGrocery.getName(),
                        numberButton.getNumber(),
                        currentGrocery.getPrice(),
                        currentGrocery.getDiscount()
                        ));

                Toast.makeText(GroceryDetails.this, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });

        grocery_description=(TextView)findViewById(R.id.grocery_description);
        grocery_name=(TextView)findViewById(R.id.grocery_name);
        grocery_price=(TextView)findViewById(R.id.grocery_price);
        grocery_image=(ImageView)findViewById(R.id.img_grocery);

        collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CollapsedAppbar);

        //Get Food Id from intent

        if(getIntent()!= null)
            groceryId=getIntent().getStringExtra("GroceryId");
        if(!groceryId.isEmpty())
        {
            if(Common.isConnectedToInternet(this))

            {
                getDetailGrocery(groceryId);
                getRatingGrocery(groceryId);
            }
            else
            {
                Toast.makeText(GroceryDetails.this,"Please Check Your Connection !!",Toast.LENGTH_SHORT).show();
                return;
            }
        }



    }

    private void getRatingGrocery(String groceryId) {
        com.google.firebase.database.Query groceryRating=ratingTbl.orderByChild("groceryId").equalTo(groceryId);
        groceryRating.addValueEventListener(new ValueEventListener() {
            int count=0,sum=0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    Rating item=postSnapShot.getValue(Rating.class);
                    sum+=Integer.parseInt(item.getRateValue());
                    count++;
                }
                if(count!=0)
                {
                    float average=sum/count;
                    ratingBar.setRating(average);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Farag Tareen","Farag","Bs Theek thi","Behtr","Behtreen"))
                .setDefaultRating(1)
                .setTitle("Rate this Food")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here...")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(GroceryDetails.this)
                .show();

    }

    private void getDetailGrocery(final String groceryId) {
        grocery.child(groceryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentGrocery=dataSnapshot.getValue(Grocery.class);

                //Set Image
                Picasso.with(getBaseContext()).load(currentGrocery.getImage())
                        .into(grocery_image);

                collapsingToolbarLayout.setTitle(currentGrocery.getName());

                grocery_price.setText(currentGrocery.getPrice());

                grocery_name.setText(currentGrocery.getName());

                grocery_description.setText(currentGrocery.getDescription());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, @NotNull String comments) {
        //Get Rating and upload to firebase

        final Rating rating=new Rating(Common.currentUser.getPhone(),
                groceryId,
                String.valueOf(value),
                comments);
        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(Common.currentUser.getPhone()).exists())
                {
                    //Remove Old Value (you can delete or let it be - useless function :3
                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();
                    //Update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }
                else
                {
                    //Update new value
                    ratingTbl.child(Common.currentUser .getPhone()).setValue(rating);
                }
                Toast.makeText(GroceryDetails.this,"Thank you for submit rating!!!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
