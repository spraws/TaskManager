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

        @Query("SELECT * FROM Task WHERE uid = :userId")
        LiveData<List<Task>> getAll(String userId);

        @Query("SELECT * FROM Task WHERE uid = :userId")
        LiveData<List<Task>> observeAll(String userId);

        @Query("SELECT * FROM Task WHERE done = '1' AND uid = :userId")
        LiveData<List<Task>> observeDone(String userId);

        @Query("SELECT * FROM Task WHERE done = '0' AND uid = :userId")
        LiveData<List<Task>> observePending(String userId);

        @Update
        void updateTask(Task task);

        @Delete
        void deleteTask(Task task);
    }