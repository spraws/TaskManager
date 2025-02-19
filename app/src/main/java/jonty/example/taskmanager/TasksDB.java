package jonty.example.taskmanager;

import android.content.Context;
import android.view.View;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Database(entities = {Task.class}, version = 2)
public abstract class TasksDB extends RoomDatabase {
    public abstract TasksDAO tasksDAO();
    private static final String DB_NAME = "tasks_database_name";
    private static TasksDB db;
    //Return an database instance.
// If the database instance already exists it return the existing instance.
// If not then it creates a new instance and returns that.
    public static TasksDB getInstance(Context context)
    {
        if (db == null) db =buildDatabaseInstance(context);
        return db;
    }
    //Create instance of Database
    private static TasksDB buildDatabaseInstance(Context context)
    {
        return Room.databaseBuilder(context, TasksDB.class, DB_NAME).build();
    }
}