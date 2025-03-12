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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
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
        RecyclerView recyclerView = findViewById(R.id.taskListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        TaskListAdapter taskListAdapter = new TaskListAdapter();
        recyclerView.setAdapter(taskListAdapter);
        //Observe for changes in the list of all tasks in th    e database
        LiveData<List<Task>> tasks = db.tasksDAO().observeAll();
        //Handle any changes in the observer
        tasks.observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
//Update the Recyclerview via its adapter
                taskListAdapter.setTaskList(db, tasks);
            }
        });

        ItemTouchHelper.SimpleCallback touchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int swipedPosition = viewHolder.getAdapterPosition();

                taskListAdapter.deleteTask(swipedPosition);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ToDoApp", "onResume");
    }

    public void onNewTaskClicked(View view) {
        Log.d("ToDoApp", "onNewTaskClicked");
        //create explicit intent for ToDoActivity
        Intent taskIntent = new Intent(this, MainActivity.class);
        startActivity(taskIntent);
    }
}
