package com.easydoso_profissional;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProfile extends AppCompatActivity {
    EditText phoneEdit, fullNameEdit, emailEdit, editCPF, editBirthDate;
    TextView editServiceText, editLocation_long, editLocation_lat;
    Button saveBtn, cancelBtn, locationBtn, servicesBtn;

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
        locationBtn = findViewById(R.id.locationBtn);
        servicesBtn = findViewById(R.id.servicesBtn);
        editCPF = findViewById(R.id.editCPF);
        editBirthDate = findViewById(R.id.editBirthDate);
        editServiceText = findViewById(R.id.editServiceText);
//        editLocationText = findViewById(R.id.editLocationText);
        phoneEdit = findViewById(R.id.phoneEdit);
        fullNameEdit = findViewById(R.id.fullnameEdit);
        emailEdit = findViewById(R.id.emailEdit);
        editLocation_long = findViewById(R.id.editLocation_long);
        editLocation_lat = findViewById(R.id.editLocation_lat);

//        Intent data = getIntent();
//        String fullName = data.getStringExtra("fullName");
//        String phone = data.getStringExtra("phone");
//        String email = data.getStringExtra("email");
        String cpf = "";


//        phoneEdit.setText(phone);
//        emailEdit.setText(email);
//        fullNameEdit.setText(fullName);

//        Log.d("passedDataFromMainAct", phone + " " + fullName + " " + email);

        //retrieve data from firebase data store
        DocumentReference docRef = fStore.collection("users").document(fAuth.getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d("TAG", "fetched user data: " + document.getData());

                    Map<String, Object> userData = new HashMap<>();
                    userData = document.getData();

                    editCPF.setText(userData.get("cpf").toString());
                    editBirthDate.setText(userData.get("birthDate").toString());
                    editServiceText.setText(userData.get("services").toString());
                    editLocation_long.setText("Longitude: " + userData.get("location_longitude").toString());
                    editLocation_lat.setText("Latutude: " + userData.get("location_latitude").toString());
                    fullNameEdit.setText(userData.get("fullName").toString());
                    emailEdit.setText(userData.get("email").toString());
                    phoneEdit.setText(userData.get("phone").toString());

                } else {
                    Log.d("TAG", "No such document");
                }
            } else {
                Log.d("TAG", "get failed with ", task.getException());
            }
        });

        //Date Picker - to pick birthdate on plain text
        final Calendar myCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

            private void updateLabel() {
                String myFormat = "MM/dd/yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                editBirthDate.setText(sdf.format(myCalendar.getTime()));
            }

        };

        editBirthDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(EditProfile.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((fullNameEdit.getText().toString().isEmpty() || phoneEdit.getText().toString().isEmpty() || emailEdit.getText().toString().isEmpty()) || editCPF.getText().toString().isEmpty()) {
                    Toast.makeText(EditProfile.this, "one of the fields is empty.", Toast.LENGTH_SHORT);
                }


                Map<String, Object> edited = new HashMap<>();
//                edited.put("email", emailEdit.getText().toString());
                edited.put("phone", phoneEdit.getText().toString());
                edited.put("fullName", fullNameEdit.getText().toString());
                edited.put("cpf", editCPF.getText().toString());
                edited.put("birthDate", editBirthDate.getText().toString());

                //updating datas on firestore
                fStore.collection("users").document(fUser.getUid()).update(edited);

                //Reloading again the EditProfile activity
                Intent refresh = new Intent(v.getContext(), MainActivity.class);
                startActivity(refresh);

                finish();

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                ///check and get permission
                if (ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditProfile.this, new String[]
                            {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                }

                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {

                        String longitude = String.valueOf(location.getLongitude());
                        String latitude = String.valueOf(location.getLatitude());

                        //save location on FireStore
                        Map<String, Object> user = new HashMap<>();
                        user.put("location_longitude", longitude);
                        user.put("location_latitude", latitude);

                        //updating datas on firestore
                        fStore.collection("users").document(fUser.getUid()).update(user);

                        editLocation_lat.setText("Latitude : " + latitude);
                        editLocation_long.setText("Longitude : " + longitude);

                        Log.d("==LOCATION=Longitude=", String.valueOf(location.getLongitude()));
                        Log.d("==LOCATION=Latitude=", String.valueOf(location.getLatitude()));
                    }
                });

            }
        });


        servicesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open Services Activity
                Intent servicesIntent = new Intent(v.getContext(), Services.class);
                startActivity(servicesIntent);
            }
        });


    }


}





