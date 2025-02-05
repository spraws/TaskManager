package jonty.example.taskmanager;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
@Dao
public interface TasksDAO {
    //retrieves a list of all tasks
    @Query("SELECT * FROM task")
    LiveData<List<Task>> getAll();
    //adds a task to the database
    @Insert
    void insert(Task task);


}