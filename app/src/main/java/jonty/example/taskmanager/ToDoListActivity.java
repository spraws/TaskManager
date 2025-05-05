package jonty.example.taskmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Arrays;
import java.util.List;

public class ToDoListActivity extends AppCompatActivity {


    AllTaskFragment allTaskFragment = new AllTaskFragment();
    CompletedTaskFragment completedTaskFragment = new CompletedTaskFragment();
    PendingTaskFragment pendingTaskFragment = new PendingTaskFragment();
    public void loadFragment(Fragment fragment) {
        FragmentTransaction fragTran = getSupportFragmentManager().beginTransaction();
        fragTran.replace(R.id.fragmentHolder, fragment);
        fragTran.commit();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list_layout);
        loadFragment(allTaskFragment);

        //Navigation bar
        BottomNavigationView bottomNavigationView =
                findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(
                new NavigationBarView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int id = item.getItemId();
                        if(id == R.id.page_all) {
                            loadFragment(allTaskFragment);
                            return true;
                        }
                        else if(id == R.id.page_done) {
                            loadFragment(completedTaskFragment);
                            return true;
                        }
                        else if(id == R.id.page_todo) {
                            loadFragment(pendingTaskFragment);
                            return true;
                        }
                        else if(id == R.id.page_map) {
                            loadFragment(new MapFragment());
                            return true;
                        }
                        else if(id == R.id.account_settings) {
//                            Intent accountIntent = new Intent(ToDoListActivity.this, AccountSettings.class);
//                            startActivity(accountIntent);
//                            return true;
                            loadFragment(new AccountSettingsFragment());
                            return true;
                        }
                        else return false;
                    }
                });
    }


    public void onNewTaskClicked(View view){
        Intent taskIntent = new Intent(this, MainActivity.class);
        startActivity(taskIntent);
    }


}
