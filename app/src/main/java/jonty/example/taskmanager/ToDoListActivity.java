package jonty.example.taskmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
//                    TextView textView = new TextView(getApplicationContext());
//                    textView.setText(task.title);
//                    linearLayout.addView(textView);

                    View listView =
                            getLayoutInflater().inflate(R.layout.task_layout, linearLayout, false);
                    TextView titleView = listView.findViewById(R.id.taskListTitle);
                    TextView descView = listView.findViewById(R.id.taskListDesc);
                    ImageView imageView = listView.findViewById(R.id.taskListImage);

                    titleView.setText(task.title);
                    descView.setText(task.description);
                    imageView.setImageURI(Uri.parse(task.imageURI));

                    linearLayout.addView(listView);

                    //Text Styling
//                    textView.setText(task.title);
//                    textView.setTextSize(25);
//                    textView.setPadding(10, 10, 10, 10);
//                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//                    textView.setTextColor(getResources().getColor(R.color.white));
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
