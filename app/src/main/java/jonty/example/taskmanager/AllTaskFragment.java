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

public class AllTaskFragment extends Fragment {
    RecyclerView recyclerView;
    TaskListAdapter taskListAdapter;
    TasksDB tasksDB;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle
            savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TasksDB db = TasksDB.getInstance(view.getContext());

        recyclerView = view.findViewById(R.id.taskListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        taskListAdapter = new TaskListAdapter();
        recyclerView.setAdapter(taskListAdapter);
        //Observe for changes in db
        LiveData<List<Task>> tasks = db.tasksDAO().observeAll();
        tasks.observe(getViewLifecycleOwner(), new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                taskListAdapter.setTaskList(db, tasks);
            }
        });

        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback touchHelperCallback =
                new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0,
                        androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }


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