package com.example.ar8ee.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ar8ee.Classes.UserClass;
import com.example.ar8ee.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class addPost extends AppCompatActivity {

    private static final int IMAGE_REQUEST = 30;
    private ImageView newPostImage;
    private TextView addImage;
    private EditText postText;
    private Uri imageUri;
    private StorageTask uploadTask;
    String userImage = "", userName = "";
    private Button postBtn,discardBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        setToolBar();
        newPostImage = findViewById(R.id.new_post_image);
        addImage = findViewById(R.id.add_photo);
        postBtn = findViewById(R.id.post_btn);
        discardBtn = findViewById(R.id.dis_btn);
        postText = findViewById(R.id.post_text);
        setImageAction();
        setAddPostAction();
        setDiscardAction();

    }

    private void setDiscardAction() {
        discardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(addPost.this);
                builder.setTitle("Discard Changes !!");
                builder.setMessage("Are you sure you want to go back ??");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(addPost.this, InnerActivity.class));
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    private void setAddPostAction() {
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    private void setToolBar() {
        Toolbar tool = (Toolbar)findViewById(R.id.add_post_tool_bar);
        setSupportActionBar(tool);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tool.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setImageAction() {
        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            newPostImage.setImageURI(data.getData());
            addImage.setVisibility(View.GONE);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();
        final String postId = System.currentTimeMillis()+"";
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren()) {
                    UserClass userClass = data.getValue(UserClass.class);
                    if(userClass.getId().equals(fuser.getUid())){
                    userImage = userClass.getImageURL();
                    userName = userClass.getUsername();
                    break;
                    }
                }
                if(imageUri == null){
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("posts").child(fuser.getUid())
                            .child(postId);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("userName", ""+userName);
                    map.put("postId", ""+postId);
                    map.put("userId", ""+fuser.getUid());
                    map.put("userImage", ""+userImage);
                    map.put("body", postText.getText().toString());
                    map.put("postImage", "");
                    map.put("likes", "0");
                    reference.setValue(map);

                    reference = FirebaseDatabase.getInstance().getReference("allPosts").child(postId);
                    reference.setValue(map);

                    pd.dismiss();
                    startActivity(new Intent(addPost.this,InnerActivity.class));
                    finish();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (imageUri != null){

            StorageReference storageReference = FirebaseStorage.getInstance().getReference("posts");
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw  task.getException();
                    }

                    return  fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();


                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("posts").child(fuser.getUid())
                                .child(postId);
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("userName", ""+userName);
                        map.put("userId", ""+fuser.getUid());
                        map.put("postId", ""+postId);
                        map.put("userImage", ""+userImage);
                        map.put("body", postText.getText().toString());
                        map.put("postImage", ""+mUri);
                        map.put("likes", "0");
                        reference.setValue(map);

                        reference = FirebaseDatabase.getInstance().getReference("allPosts").child(postId);
                        reference.setValue(map);

                        pd.dismiss();
                        startActivity(new Intent(addPost.this,InnerActivity.class));
                        finish();
                    } else {
                        Toast.makeText(addPost.this, "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(addPost.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {

            Toast.makeText(addPost.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

}