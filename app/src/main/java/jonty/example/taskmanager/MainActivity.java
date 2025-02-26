package jonty.example.taskmanager;
import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TimePicker;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import jonty.example.taskmanager.R;

public class MainActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> launchCameraActivity;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        launchCameraActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult activityResult) {
                        if (activityResult.getResultCode() == RESULT_OK) {
                            Log.d("ToDoApp", "picture stored in: " + imageUri);
                            ImageView taskImage = findViewById(R.id.taskImage);
                            ;
                            taskImage.setImageURI(imageUri);

                        }
                    }
                });


        Button saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextInputEditText titleView = findViewById(R.id.titleInputEditText);
                EditText dateView = findViewById(R.id.dateInputEditText);
                EditText timeView = findViewById(R.id.timeInputEditText);
                TextInputEditText descView = findViewById(R.id.descInputEditText);

                String title = titleView.getText().toString();
                String date = dateView.getText().toString();
                String time = timeView.getText().toString();
                String desc = descView.getText().toString();

                SharedPreferences sharedPreferences = getSharedPreferences("toDoApp", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("title", title);
                editor.putString("date", date);
                editor.putString("time", time);
                editor.putString("desc", desc);
                editor.apply();


                //SQLite
                TasksDB db = TasksDB.getInstance(MainActivity.this);
                final Task task1 = new Task();
                task1.title = title;
                task1.description = desc;
                task1.date = date;
                task1.time = time;
                task1.imageURI = imageUri.toString();
                task1.done = false;


                Executor myExecutor = Executors.newSingleThreadExecutor();
                myExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        db.tasksDAO().insert(task1);
                    }
                });

                //Print data to log cat
                LiveData<List<Task>> tasks = db.tasksDAO().getAll();
                tasks.observe(MainActivity.this, new Observer<List<Task>>() {
                    @Override
                    public void onChanged(List<Task> tasks) {
                        for (Task task : tasks) {
                            Log.d("ToDoAPP", task.title + " " + task.description + "");
                        }
                    }
                });
                finish();


            }
        });

        //File Storage
        String fileName = "file.txt";
        String contents = "this is a test";

        File file = new File(getFilesDir(), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(contents);
            osw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onDateClick(View view) {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                EditText dateView = findViewById(R.id.dateInputEditText);

                dateView.setText(dayOfMonth + "/" + (month + 1) + "/" + year);

            }
        };

        //create date picker dialog
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, listener, year, month, day);
        datePickerDialog.show();
    }


    public void onTimeClick(View view) {
        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                EditText timeView = findViewById(R.id.timeInputEditText);
                timeView.setText(hourOfDay + ":" + minute);
            }
        };


        TimePickerDialog timePickerDialog = new TimePickerDialog(this, listener, 0, 0, true);
        timePickerDialog.show();
    }


    //Camera logic
    public void onCameraClick(View view) {
//        Log.d("ToDoApp", "onCameraClick");
        String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(getFilesDir(), imageFileName);
        imageUri = FileProvider.getUriForFile(this, ".fileprovider", imageFile);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        launchCameraActivity.launch(takePictureIntent);
    }


}

