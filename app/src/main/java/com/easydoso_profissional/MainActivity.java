package com.easydoso_profissional;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    TextView fullName, email, phone, verfyMessage, yourCPF, yourService, yourLong,yourLat,yourBirth;
    Button verifyButton, changeProfileBtn, resetPassBtn, editProfileBtn;
    ImageView profileImage;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;

    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phone = findViewById(R.id.YourPhone);
        email = findViewById(R.id.YourEmail);
        fullName = findViewById(R.id.YourName);
        resetPassBtn = findViewById(R.id.resetPassBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        yourCPF = findViewById(R.id.yourCPF);
        yourService = findViewById(R.id.yourService);
        yourLong = findViewById(R.id.yourLong);
        yourLat= findViewById(R.id.yourLat);
        yourBirth=findViewById(R.id.yourBirth);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        StorageReference profileRef = storageReference.child("users/" + fAuth.getCurrentUser().getUid() + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        });

        verfyMessage = findViewById(R.id.verifyMessage);
        verifyButton = findViewById(R.id.verifyButton);

        profileImage = findViewById(R.id.profileImage);
        changeProfileBtn = findViewById(R.id.editPictureBtn);

        userID = fAuth.getCurrentUser().getUid();
        FirebaseUser user = fAuth.getCurrentUser();

        //to check if the email is verified or not
        if (!user.isEmailVerified()) {
            verifyButton.setVisibility(View.VISIBLE);
            verfyMessage.setVisibility(View.VISIBLE);

            verifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //to send Verificiation link

                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(v.getContext(), "Verification Email has been sent.", Toast.LENGTH_SHORT);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(v.getContext(), "Error : EMAIL NOT SENT.", Toast.LENGTH_SHORT);
                            Log.d("ERROR", "EMAIL NOT SENT" + e.getMessage());
                        }
                    });
                }
            });

        }


        resetPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser fuser = fAuth.getCurrentUser();
                EditText resetPassword = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter Your New Password (more than 6 character)");
                passwordResetDialog.setView(resetPassword);

                passwordResetDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String newPassword = resetPassword.getText().toString();
                        fuser.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this, "Password changed successfully.", Toast.LENGTH_SHORT);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Failed.", Toast.LENGTH_SHORT);
                            }
                        });

                    }
                });

                passwordResetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close the Dialog
                    }
                });

                passwordResetDialog.create().show();
            }
        });


        //retrieve data from firebase data store
        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                phone.setText(documentSnapshot.getString("phone"));
                fullName.setText(documentSnapshot.getString("fullName"));
                email.setText(documentSnapshot.getString("email"));
                yourCPF.setText(documentSnapshot.getString("cpf"));
                yourService.setText(documentSnapshot.getString("services"));
                yourLong.setText(documentSnapshot.getString("location_longitude"));
                yourLat.setText(documentSnapshot.getString("location_latitude"));
                yourBirth.setText(documentSnapshot.getString("birthDate"));
            }
        });


        changeProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open Gallery
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);

            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open EditProfile Activity
                Intent editProfileIntent = new Intent(v.getContext(), EditProfile.class);


                //passing data to another activity using intent
//                editProfileIntent.putExtra("fullName", fullName.getText().toString());
//                editProfileIntent.putExtra("email", email.getText().toString());
//                editProfileIntent.putExtra("phone", phone.getText().toString());

                startActivity(editProfileIntent);

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = data.getData();
                //profileImage.setImageURI(imageUri);

                //upload image to firebase storage
                upluadImageToFireBase(imageUri);
            }
        }
    }

    private void upluadImageToFireBase(Uri imageUri) {
        //upload image to firebase storage
        StorageReference fileRef = storageReference.child("users/" + fAuth.getCurrentUser().getUid() + "/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImage);
                    }
                });
                //Toast.makeText(MainActivity.this,"Image Uploaded.",Toast.LENGTH_SHORT);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to Image Upload.", Toast.LENGTH_SHORT);
            }
        });
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Login.class));
//        finish();
    }
}