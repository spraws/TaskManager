package jonty.example.taskmanager;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

import jonty.example.taskmanager.R;

public class MainActivity extends AppCompatActivity {

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

                Log.d("toDoApp", "Save button clicked!");
            }
        });


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

    public void onTimeClick(View view){
        TimePickerDialog.OnTimeSetListener listener =  new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                EditText timeView = findViewById(R.id.timeInputEditText);
                timeView.setText(hourOfDay + ":" + minute);
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, listener, 0, 0, true);
        timePickerDialog.show();
    }

}