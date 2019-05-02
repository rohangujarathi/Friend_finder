package com.umbc.friend_finder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class FirstPage extends AppCompatActivity {

    private Button logout;
    private String currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);
        EditText user = findViewById(R.id.editText);
        currentUser = getIntent().getStringExtra("User");
        user.setText(currentUser);
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser = null;
                Intent intent = new Intent(FirstPage.this, LoginActivity.class);
                intent.putExtra("User", currentUser);
                startActivity(intent);
            }
        });
    }
}
