package com.example.calendo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calendo.fragments.DatePickerFragment;
import com.example.calendo.fragments.todolist.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.example.calendo.utils.User.MY_PREFS_NAME;

public class AddNewTaskActivity extends AppCompatActivity {
    private FloatingActionButton fab_save;
    private EditText title;
    private TextView date;
    private EditText notes;
    private Spinner dropdownCategory;
    private String selectedDate;

    //Data
    private ArrayList<String> categories;

    public static final String TASK_TITLE="title";
    public static final String TASK_CATEGORY="category";
    public static final String TASK_DATE="duedate";
    public static final String TASK_DESCRIPTION="description";
    private static final String TAG = "AddNewTaskActivity";

    //Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_task);

        //Link UI elements
        fab_save = findViewById(R.id.fab_save);
        title = findViewById(R.id.TaskName);
        date = findViewById(R.id.TaskTimeLabel);
        notes = findViewById(R.id.notes);
        dropdownCategory = findViewById(R.id.categories);

        //Fill dropdown
        // you need to have a list of data that you want the spinner to display
        getCategories();

    }

    public void showDatePicker(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(),"datePicker");
    }

    public void getCategories(){

        //Retrieve the userID
        SharedPreferences sharedPref = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        final String userID = sharedPref.getString("userID", "NOUSERFOUND");

        categories = new ArrayList<>();

        //Retrieve user categories
        CollectionReference usersRef = db.collection("Users").document(userID).collection("categories");

        usersRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                categories.add(document.getString("categoryName"));

                            }

                            renderCategories();

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void renderCategories(){
        //Set the UI
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownCategory.setAdapter(adapter);

        //Select the category from the Previous activity
        Intent intent = getIntent();
        String selectedCategory = intent.getStringExtra("category");
        if(!selectedCategory.equals("All")){
            int position=0;

            //Search the string in the list and return the position
            while( !selectedCategory.equals(categories.get(position))){
                position++;
            }

            dropdownCategory.setSelection(position);
        }
    }

    public void processDatePickerResult(int year, int month, int day) {
        String day_string;
        String month_string;
        if (day>=10) {
            day_string = Integer.toString(day);
        }
        else {
            day_string = "0"+day;
        }
        if (month>=9) {
            month_string = Integer.toString(month+1);
        }
        else {
            month_string = "0"+(month+1);
        }
        String year_string = Integer.toString(year);
        String dateMessage = (day_string +
                "/" + month_string + "/" + year_string);
        selectedDate =year_string+month_string+day_string;

        TextView tasktimelabel = findViewById(R.id.TaskTimeLabel);
        tasktimelabel.setText(dateMessage);

    }

    public void saveTask(View view){

        //The only mandatory field is the title

        if(title.getText().toString().equals("")){
            Toast.makeText(this, "You did not enter a title!", Toast.LENGTH_SHORT).show();
        } else {
            //Title has been inserted

            //Set date as empty string when a date is not selected
            String datetoShow;
            if(date.getText().toString().equals("Set a reminder")){
                datetoShow= "";
            }else {
                datetoShow = selectedDate;
            }

            //Retrieve the userID
            SharedPreferences sharedPref = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            String userID = sharedPref.getString("userID", "NOUSERFOUND");



            //Now save the task
            Task todolist = new Task("#", title.getText().toString(),dropdownCategory.getSelectedItem().toString(),notes.getText().toString(), datetoShow , "uncompleted");
            usersRef.document(userID).collection("list").add(todolist);
            finish();

        }


    }
}
