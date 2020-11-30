package com.easydoso_profissional;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {
    EditText phoneEdit, fullNameEdit, emailEdit, birthDateEdit;
    Button saveBtn, cancelBtn;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fUser = fAuth.getCurrentUser();
        saveBtn = findViewById(R.id.saveEdit);
        cancelBtn = findViewById(R.id.cancelEdit);

        Intent data = getIntent();
        String fullName = data.getStringExtra("fullName");
        String phone = data.getStringExtra("phone");
        String email = data.getStringExtra("email");

        phoneEdit = findViewById(R.id.phoneEdit);
        fullNameEdit = findViewById(R.id.fullnameEdit);
        emailEdit = findViewById(R.id.emailEdit);
        birthDateEdit = findViewById(R.id.birthDateEdit);

        phoneEdit.setText(phone);
        emailEdit.setText(email);
        fullNameEdit.setText(fullName);

        Log.d("=================", phone + " " + fullName + " " + email);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((fullNameEdit.getText().toString().isEmpty() || phoneEdit.getText().toString().isEmpty() || emailEdit.getText().toString().isEmpty())) {
                    Toast.makeText(EditProfile.this, "one of the fields is empty.", Toast.LENGTH_SHORT);
                }

                //updating email on firestore
                fUser.updateEmail(emailEdit.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference documentReference = fStore.collection("users").document(fUser.getUid());
                        Map<String, Object> edited = new HashMap<>();
                        edited.put("email", emailEdit.getText().toString());
                        edited.put("phone", phoneEdit.getText().toString());
                        edited.put("fullName", fullNameEdit.getText().toString());

                        documentReference.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditProfile.this, "Profile Updated.", Toast.LENGTH_SHORT);
                                //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditProfile.this, "Error - Profile not Updated."+e.getMessage(), Toast.LENGTH_SHORT);
                            }
                        });

                        Log.d("Edit", "Email changed successfully.");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Edit", "Eror - Email not changer.");
                    }
                });

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}