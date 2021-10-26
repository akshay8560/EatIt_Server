package akshay.kumar.eatitserver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import akshay.kumar.eatitserver.Common.Common;
import akshay.kumar.eatitserver.Model.User;


public class SignIn extends AppCompatActivity {
    EditText edtPhone, edtPassword;
    Button btnSignIn;
    CheckBox ckbRemember;
    ImageButton showHideBtn;
    FirebaseDatabase db;
    DatabaseReference users;
    boolean flag = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPassword = findViewById(R.id.editPassword);
        edtPhone = findViewById(R.id.editPhoneNumber);
        btnSignIn = findViewById(R.id.btnSignIn);

        showHideBtn = findViewById(R.id.showHideBtn);

        showHideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag == true) {
                    flag = false;
                    edtPassword.setTransformationMethod(null);
                    if (edtPassword.getText().length() > 0)
                        edtPassword.setSelection(edtPassword.getText().length());

                } else {
                    flag = true;
                    edtPassword.setTransformationMethod(new PasswordTransformationMethod());
                    if (edtPassword.getText().length() > 0)
                        edtPassword.setSelection(edtPassword.getText().length());

                }
            }
        });


               db = FirebaseDatabase.getInstance();
               users = db.getReference("User");
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser(edtPhone.getText().toString(),edtPassword.getText().toString());
            }
        });

    }

    private void signInUser(String phone, String password) {
        final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
        mDialog.setMessage("Please Wait....");
        mDialog.show();
        final  String localphone=phone;
        final String localpassword=password;
        users.addValueEventListener(new ValueEventListener( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child(localphone).exists()){

                    mDialog.dismiss();
                    User user=snapshot.child(localphone).getValue(User.class);
                    user.setPhone(localphone);
                    if (Boolean.parseBoolean(user.getIsStaff())){
                        if (user.getPassword().equals(localpassword)){

                            Intent intent=new Intent( SignIn.this,Home.class );
                            Common.currentUser=user;
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(SignIn.this, "Wrong password !", Toast.LENGTH_SHORT).show( );
                        }
                    }else {
                        Toast.makeText(SignIn.this, "Please Login with Staff account !", Toast.LENGTH_SHORT).show( );
                    }

                }else {

                    Toast.makeText(SignIn.this, "User not exists !", Toast.LENGTH_SHORT).show( );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}