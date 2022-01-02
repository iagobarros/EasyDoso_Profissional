package com.easydoso_profissional;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Services extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;
    FirebaseUser fUser;

    ListView listView;

    EditProfile editProfileInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        Log.d("khalil", "=========Services============");

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        fUser = fAuth.getCurrentUser();

        listView = findViewById(R.id.categoriaListView);


        final String[] categoriasArray = new String[8]; //= new String[categoriasMap.size()]

        editProfileInstance = new EditProfile();

        //retrieve data from firebase data store
        DocumentReference docRef = fStore.collection("appdata").document("categorias");
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d("TAG", "DocumentSnapshot data: " + document.getData());

                    Map<String, Object> categoriasMap = new HashMap<>();
                    categoriasMap = document.getData();

                    Iterator it = categoriasMap.entrySet().iterator();

                    int iterateNum = 0;

                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        categoriasArray[iterateNum++] = pair.getValue().toString();
                    }

                    int images[] = {R.drawable.diarista,
                            R.drawable.taxi,
                            R.drawable.electrista,
                            R.drawable.mecanico,
                            R.drawable.barbeiro,
                            R.drawable.manicurepedicure,
                            R.drawable.tecnicoarcondicionado,
                            R.drawable.jardineiro,};

                    //calling adapter
                    MyAdapter adapter = new MyAdapter(this, categoriasArray, categoriasArray, images);
                    listView.setAdapter(adapter);

                    //setting onclick listener in listview items
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            Map<String, Object> edited = new HashMap<>();
                            if (position == 0) {
                                edited.put("services", "Diarista");
                            } else if (position == 1) {
                                edited.put("services", "Taxi");
                            }else  if(position==2){
                                edited.put("services", "Eletricista");
                            }else if(position==3){
                                edited.put("services", "Mecanico");
                            }else if (position==4){
                                edited.put("services", "Barbeiro");
                            }else if (position==5){
                                edited.put("services", "Manicure/Pedicure");
                            }else if (position==6){
                                edited.put("services", "Tecnico ar Condicionado");
                            }else if(position==7){
                                edited.put("services", "Jardineiro");
                            }

                            //updating datas on firestore
                            fStore.collection("users").document(fUser.getUid()).update(edited);

                            //Reloading again the EditProfile activity
                            Intent refresh = new Intent(view.getContext(), EditProfile.class);
                            startActivity(refresh);

                            finish();
                        }
                    });


                } else {
                    Log.d("TAG", "No such document");
                }
            } else {
                Log.d("TAG", "get failed with ", task.getException());
            }
        });

    }

    // create an adapter class
    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        String mainText[];
        String subText1[];
        int images1[];


        MyAdapter(Context context, String title[], String description[], int imgs[]) {
            super(context, R.layout.row, R.id.textViewMain, title);
            this.context = context;
            this.mainText = title;
            this.subText1 = description;
            this.images1 = imgs;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row, parent, false);
            ImageView images2 = row.findViewById(R.id.rowImage);
            TextView myTitle = row.findViewById(R.id.textViewMain);
            TextView subText2 = row.findViewById(R.id.testViewSub);

            //setting our resources on view
            images2.setImageResource(images1[position]);
            myTitle.setText(mainText[position]);
            subText2.setText(subText1[position]);


            return row;
        }
    }
}