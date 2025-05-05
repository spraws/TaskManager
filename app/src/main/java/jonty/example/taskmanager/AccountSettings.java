package jonty.example.taskmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.tasks.Task;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;

public class AccountSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//    Button signOutButton = findViewById(R.id.signOutButton);
//        signOutButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                signOut();
//            }
//        });
//    }


//    public void signOut(){
//        AuthUI.getInstance()
//                .signOut(this)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    public void onComplete(@NonNull Task <Void> task) {
//                        Toast.makeText(AccountSettings.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
//
//                        // Redirect to your login screen (e.g., MainActivity or LoginActivity)
//                        Intent intent = new Intent(AccountSettings.this, LogIn.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears backstack
//                        startActivity(intent);
//
//                    }
//                });
//    }

//    public void signOut(){
//        AuthUI.getInstance()
//                .signOut(this)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    public void onComplete(@NonNull Task <Void> task) {
//                        Toast.makeText(AccountSettings.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
//
//                        // Redirect to your login screen (e.g., MainActivity or LoginActivity)
//                        Intent intent = new Intent(AccountSettings.this, LogIn.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears backstack
//                        startActivity(intent);
//
//                    }
//                });
    }
}