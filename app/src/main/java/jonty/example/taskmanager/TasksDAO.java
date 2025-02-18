package jonty.example.taskmanager;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
@Dao
public interface TasksDAO {
    @Insert
    void insert(Task task);
    @Query("SELECT * FROM Task")
    LiveData<List<Task>> getAll();
    @Query("SELECT * FROM Task")
    LiveData<List<Task>> observeAll();


}