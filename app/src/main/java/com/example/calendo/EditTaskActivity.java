package com.example.calendo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

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
import com.example.calendo.fragments.EditDatePickerFragment;
import com.example.calendo.fragments.todolist.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import static com.example.calendo.utils.User.MY_PREFS_NAME;

public class EditTaskActivity extends AppCompatActivity {
    private FloatingActionButton fab_save;
    private EditText title;
    private TextView date;
    private EditText notes;
    private Spinner dropdownCategory;
    //Data
    private ArrayList<String> categories;
    private String userID;
    public static final String TASK_TITLE = "title";
    public static final String TASK_CATEGORY = "category";
    public static final String TASK_DATE = "duedate";
    public static final String TASK_DESCRIPTION = "description";
    private static final String TAG = "AddNewTaskActivity";

    //Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");
    private String newDate;
    private String status;

    Task currentTask;

    @Override
    protected void onStart() {
        super.onStart();
        renderCategories();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        //Link UI elements
        fab_save = findViewById(R.id.fab_edit);
        title = findViewById(R.id.TaskNameEdit);
        date = findViewById(R.id.TaskTimeLabelEdit);
        notes = findViewById(R.id.notesEdit);
        dropdownCategory = findViewById(R.id.categoriesEdit);


        //Fill dropdown
        // you need to have a list of data that you want the spinner to display
        getCategories();

        //Retrieve userID
        SharedPreferences sharedPref = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        userID = sharedPref.getString("userID", "NOUSERFOUND");

        //Get data from firebase
        CollectionReference usersRef = db.collection("Users").document(this.userID).collection("list");
        Log.d(TAG, usersRef.toString());
        String selectedTitle = getIntent().getStringExtra("Title");
        title.setText(selectedTitle);
        usersRef.whereEqualTo("title", selectedTitle)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Task todolist = documentSnapshot.toObject(Task.class);

                    todolist.setId(documentSnapshot.getId());
                    String editID = todolist.getId();
                    String editTitle = todolist.getTitle();
                    String editNotes = todolist.getNotes();
                    status = todolist.getStatus();
                    newDate = todolist.getDate();
                    String editDates="";
                    if(!newDate.equals("")){
                         editDates =  newDate.substring(6,8)+ "/" + newDate.substring(4, 6) + "/"+newDate.substring(0,4);

                    }
                    String editCategory = todolist.getCategory();
                    ArrayAdapter myAdap = (ArrayAdapter) dropdownCategory.getAdapter(); //cast to an ArrayAdapter
                    int spinnerPosition = myAdap.getPosition(editCategory);
                    dropdownCategory.setSelection(spinnerPosition);
                    title.setText(editTitle);
                    notes.setText(editNotes);
                    date.setText(editDates);
                    Log.d(TAG, todolist.toString());
                    currentTask = todolist;
                    Log.d(TAG, currentTask.getTitle());
                }


            }
        });
    }

    public void showDatePicker(View view) {
        DialogFragment newFragment = new EditDatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void getCategories() {

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

    public void renderCategories() {
        //Set the UI
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownCategory.setAdapter(adapter);
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
        TextView tasktimelabel = findViewById(R.id.TaskTimeLabelEdit);

        newDate = year_string+month_string+day_string;

        tasktimelabel.setText(dateMessage);

    }

    public void editTask(View view) {
        String datetoShow;
        if(date.getText().toString().equals("Set a reminder")){
            newDate= "";
        }

        Task newTask = new Task("#", title.getText().toString(),dropdownCategory.getSelectedItem().toString(),notes.getText().toString(), newDate , status);
        CollectionReference usersRef = db.collection("Users").document(userID).collection("list");
        usersRef.document(currentTask.getId())
                .set(newTask)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditTaskActivity.this, notes.getText(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

    }
}