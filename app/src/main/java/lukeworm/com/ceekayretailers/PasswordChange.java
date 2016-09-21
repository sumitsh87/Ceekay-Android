package lukeworm.com.ceekayretailers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PasswordChange extends AppCompatActivity {
    EditText oldPasswordView, newPasswordView, confirmNewPasswordView;
    Button changeButton;
    String dseCode;
    ProgressDialog progressDialog;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);
        context = getBaseContext();
        dseCode = getIntent().getStringExtra("dseCode");
        oldPasswordView = (EditText) findViewById(R.id.old_password);
        newPasswordView = (EditText) findViewById(R.id.new_password);
        confirmNewPasswordView = (EditText) findViewById(R.id.confirm_new_password);
        changeButton = (Button) findViewById(R.id.change_button);
        changeButton.setOnClickListener(changeClickListener);


    }

    View.OnClickListener changeClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            String newPassword = newPasswordView.getText().toString();
            String oldPassword = oldPasswordView.getText().toString();
            String confirmNewPassword = confirmNewPasswordView.getText().toString();
            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(PasswordChange.this, "New Passwords did not match. Kindly retry", Toast.LENGTH_LONG).show();
                return;
            }

            changePassword(oldPassword, newPassword);
        }
    };

    private void changePassword(String oldPassword, String newPassword){
        ChangePasswordAsyncTask pwdChangeTask = new ChangePasswordAsyncTask(dseCode, oldPassword, newPassword, PasswordChange.this);
        pwdChangeTask.execute((Void) null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_password_change, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void gotoHomePageAfterPwdChange(){
        Intent intent = new Intent(PasswordChange.this, RetailerDetails1.class);
        intent.putExtra("dseCode", dseCode);
        startActivity(intent);
        finish();
    }

    void showProgress(boolean arg){
        if(arg){
            progressDialog = new ProgressDialog(PasswordChange.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();
        }
        else
            progressDialog.dismiss();

    }

}
