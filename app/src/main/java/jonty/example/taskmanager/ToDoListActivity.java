package jonty.example.taskmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.List;

public class ToDoListActivity extends AppCompatActivity {
    TasksDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list_layout);
        //get an instance of the room database
        db = TasksDB.getInstance(this);
        //Get instance of the linear layout for the list
        LinearLayout linearLayout = findViewById(R.id.taskLinearLayout);
        //Observe for changes in the list of all tasks in the database
        LiveData<List<Task>> tasks = db.tasksDAO().observeAll();
        //Handle any changes in the observer
        tasks.observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                Log.d("ToDoApp", "The task list has changed");
                //Wipe the layout so we can redraw the updated tasks.
                linearLayout.removeAllViews();
                //Loop through the list of tasks.
                for (Task task : tasks) {
                //Create a simple TextView using the task title
                    TextView textView = new TextView(getApplicationContext());
                    textView.setText(task.title);
                    //Add the TextView to the LinearLayout
                    linearLayout.addView(textView);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ToDoApp", "onResume");
    }

    public void onNewTaskClicked(View view) {
        Log.d("ToDoApp", "onNewTaskClicked");
        //create explicit intent for ToDoAcivity
        Intent taskIntent = new Intent(this, MainActivity.class);
        startActivity(taskIntent);
    }
}
