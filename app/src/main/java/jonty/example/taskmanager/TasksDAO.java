package jonty.example.taskmanager;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
public interface TasksDAO {
    @Insert
    void insert(Task task);
    @Query("SELECT * FROM Task")
    LiveData<List<Task>> getAll();
    @Query("SELECT * FROM Task")
    LiveData<List<Task>> observeAll();

    @Query("SELECT * FROM Task WHERE done = '1'")
    LiveData<List<Task>> observeDone();

    @Query("SELECT * FROM Task WHERE done = '0'")
    LiveData<List<Task>> observePending();

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);


}