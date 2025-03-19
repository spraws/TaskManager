package jonty.example.taskmanager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


public class PendingTaskFragment extends Fragment {

    RecyclerView recyclerView;
    TaskListAdapter taskListAdapter;
    TasksDB tasksDB;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pending_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle
            savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
// 1. get a database instance
        TasksDB db = TasksDB.getInstance(view.getContext());
// 2. Bind recycler view to adapter
        recyclerView = view.findViewById(R.id.taskListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        taskListAdapter = new TaskListAdapter();
        recyclerView.setAdapter(taskListAdapter);
// 3. Observe for changes in the list of all tasks in the database
        LiveData<List<Task>> tasks = db.tasksDAO().observePending();
//Handle any changes in the observer
        tasks.observe(getViewLifecycleOwner(), new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
//Update the Recyclerview via its adapter
                taskListAdapter.setTaskList(db, tasks);
            }
        });
// 4. Create callbacks for the ItemTouchHelper to
// detect right swipes for deletion
        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback touchHelperCallback =
                new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0,
                        androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    //Responds to swipes with deletion of task at the
//swiped position in the recyclerview
                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                         int direction) {
                        int swipePosition = viewHolder.getAdapterPosition();
                        taskListAdapter.deleteTask(swipePosition);
                    }
                };
// 5. Attach item touch callbacks to recyclerview
        androidx.recyclerview.widget.ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}