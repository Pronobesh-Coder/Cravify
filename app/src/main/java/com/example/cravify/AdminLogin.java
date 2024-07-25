package com.example.cravify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminLogin extends AppCompatActivity {

    ImageView btn_back;
    EditText Email,Password;
    Button Login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        EdgeToEdge.enable(this);

        btn_back = findViewById(R.id.back);
        Email = findViewById(R.id.email);
        Password = findViewById(R.id.password);
        Login = findViewById(R.id.login_button);

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Email.getText().toString().equals("admin@gmail.com") && Password.getText().toString().equals("admin1234")){
                    Toast.makeText(getApplicationContext(),"Welcome Admin!",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AdminLogin.this,AdminDashboard.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Invalid Credentials!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(), UserLogin.class);
                startActivity(intent);
                finish();
            }
        });
    }
}