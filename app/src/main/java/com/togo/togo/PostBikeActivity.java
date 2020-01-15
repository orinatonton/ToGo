package com.togo.togo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class PostBikeActivity extends AppCompatActivity {

    private ImageView displayImage;
    private TextView pickImage;
    private EditText bikeModel, engineSize, hireRate;
    private Uri imageUri;
    private String bike_model, engine_size, hire_rate;
    private DatabaseReference rootDbRef;
    private StorageReference rootStgRef;
    private ProgressDialog loadPostBike;
    private Button submit;
    private String booked;
    private SharedPreferences sharedPreferences;
    private String prefFile = BuildConfig.APPLICATION_ID + ".PREFERENCE_FILE_KEY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_bike);

        displayImage = (ImageView)findViewById(R.id.display_image);
        pickImage = (TextView)findViewById(R.id.pick_image);
        bikeModel = (EditText)findViewById(R.id.bike_model);
        engineSize = (EditText)findViewById(R.id.engine_size);
        hireRate = (EditText)findViewById(R.id.hire_rate);
        submit = (Button)findViewById(R.id.submit);
        loadPostBike = new ProgressDialog(this);
        rootDbRef = FirebaseDatabase.getInstance().getReference();
        rootStgRef = FirebaseStorage.getInstance().getReference();
        sharedPreferences = getSharedPreferences(prefFile, Context.MODE_PRIVATE);

        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickBikeImage();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitBikeImage();
            }
        });

        //update ui
        pickImage.setPaintFlags(pickImage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    private void pickBikeImage() {int read_permission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (read_permission != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "Grant Permissions first", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }
        else
        {
            Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
            pick.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
            startActivityForResult(Intent.createChooser(pick,"Proceed with : "),1);
        }

    }

    //get results after permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            pickBikeImage();
        }
        else
        {
            Toast.makeText(this, "Grant Permissions first", Toast.LENGTH_SHORT).show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //get results after activity for result returns results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null)
        {
            imageUri = data.getData();
            pickImage.setVisibility(View.GONE);
            displayImage.setImageURI(imageUri);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //handle item clicks on options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            finish();
        }

        return  true;
    }
    private void submitBikeImage() {
        if (validate())
        {
            loadPostBike.setMessage("Uploading image...");
            loadPostBike.setCancelable(false);
            loadPostBike.show();

            final String bike_id = getbikeId();
            final String[] image_url = {""};

            final StorageReference imgStrgRef = rootStgRef.child("bikes").child(bike_id).child(imageUri.getLastPathSegment());
            UploadTask uploadTask = imgStrgRef.putFile(imageUri);

            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful())
                    {throw task.getException();}
                    return imgStrgRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                        Uri uri = task.getResult();
                        image_url[0] = uri.toString();
                        postToDatabase(bike_id, image_url[0]);
                    }
                    else
                    {
                        loadPostBike.dismiss();
                        showDialog(bike_id);
                    }
                }
            });

        }
    }
    //continue with an image or not
    private void showDialog(final String bike_id) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Sorry, we couldn't upload your image\nProceed without one and add later?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                postToDatabase(bike_id,"No Image");
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                submitBikeImage();
            }
        }).show();
    }

    private void postToDatabase(String bike_id, String image_url)
    {loadPostBike.setMessage("Uploading Motorbike details...");
        if (!loadPostBike.isShowing())
        {
            loadPostBike.show();
        }

        Map<String,String> data = new HashMap<String,String>();

        data.put("bike_id",bike_id);
        data.put("image_url",image_url);
        data.put("bike_model",bike_model);
        data.put("engine_size",engine_size);
        data.put("hire_rate",hire_rate);
        data.put("booked", booked);

        rootDbRef.child("bikes").child(bike_id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    loadPostBike.dismiss();
                    Toast.makeText(PostBikeActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PostBikeActivity.this,ViewBike.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                }
                else
                {
                    loadPostBike.dismiss();
                    Toast.makeText(PostBikeActivity.this, "Couldn't upload details.\nCheck internet connection and try later", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getbikeId() {
        String key = "";
        key = rootDbRef.child("bikes").push().getKey();
        return key;
    }

    private boolean validate() {
        bike_model = bikeModel.getText().toString();
        engine_size = engineSize.getText().toString();
        hire_rate = hireRate.getText().toString();

        if(imageUri == null)
        {
            Toast.makeText(this, "Pick an image for the car", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (bike_model.isEmpty())
        {
            Toast.makeText(this, "Enter Motorbike Model", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (engine_size.isEmpty())
        {
            Toast.makeText(this, "Enter engine size", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (hire_rate.isEmpty())
        {
            Toast.makeText(this, "Enter hiring rate", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PostBikeActivity.this, CustomerMapActivity.class);
        startActivity(intent);
        finish();
    }
}
