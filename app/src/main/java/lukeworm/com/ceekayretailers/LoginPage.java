package lukeworm.com.ceekayretailers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.org.apache.http.HttpEntity;
import com.amazonaws.org.apache.http.HttpResponse;
import com.amazonaws.org.apache.http.NameValuePair;
import com.amazonaws.org.apache.http.client.HttpClient;
import com.amazonaws.org.apache.http.client.entity.UrlEncodedFormEntity;
import com.amazonaws.org.apache.http.client.methods.HttpPost;
import com.amazonaws.org.apache.http.impl.client.DefaultHttpClient;
import com.amazonaws.org.apache.http.message.BasicNameValuePair;
import com.amazonaws.org.apache.http.protocol.HTTP;
import com.amazonaws.org.apache.http.util.EntityUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginPage extends Activity {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
   TextView mDSECodeView;
    EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        // Set up the login form.
        mDSECodeView = (TextView) findViewById(R.id.dse_code);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mDseSignInButton = (Button) findViewById(R.id.sign_in_button);
        mDseSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isInternetAvail()) {
                    Toast.makeText(LoginPage.this, "No network available, Kindly check your internet connectivity",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        final TextInputLayout dseCodeWrapper = (TextInputLayout) findViewById(R.id.dse_code_layout);
        final TextInputLayout passwordWrapper = (TextInputLayout) findViewById(R.id.password_layout);
        dseCodeWrapper.setHint("DSE Code");
        passwordWrapper.setHint("Password");

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        // Reset errors.
        mDSECodeView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String dseCode = mDSECodeView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid email address.
        if (TextUtils.isEmpty(dseCode)) {
            mDSECodeView.setError(getString(R.string.error_field_required));
            focusView = mDSECodeView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }



        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(dseCode, password);
            mAuthTask.execute((Void) null);

        }
    }

    void showProgress(boolean arg){
        if(arg){
            progressDialog = new ProgressDialog(LoginPage.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();
        }
        else
            progressDialog.dismiss();

    }




    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mDseCode;
        private final String mPassword;
        String failMessage;

        UserLoginTask(String dseCode, String password) {
            mDseCode = dseCode;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HttpPost httpPost = new HttpPost(Constants.loginUrl);
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();

                nvps.add(new BasicNameValuePair("dse_code", mDseCode));
                nvps.add(new BasicNameValuePair("password", mPassword));
                HttpClient httpclient = new DefaultHttpClient();
                httpPost.setEntity(new UrlEncodedFormEntity(nvps,
                        HTTP.UTF_8));
                HttpResponse response = httpclient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                JSONObject responseJSON = new JSONObject(EntityUtils.toString(
                        entity, "UTF-8"));
                boolean success = responseJSON.get("success") != null && responseJSON.get("success").toString().equals("true");
                if(!success){
                    failMessage = responseJSON.get("reason").toString();
                    return false;
                }
                Constants.userRole = responseJSON.get("role").toString();
                Constants.accessToken = responseJSON.get("access_token").toString();
                Constants.isFirstLogin = responseJSON.get("first_time_login").toString().equals("true");


            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            if (success) {
//                finish();
                Toast.makeText(LoginPage.this, "Login Successful", Toast.LENGTH_LONG).show();
                Constants.userDseCode = mDseCode;

                if(Constants.isFirstLogin){
                    Intent intent = new Intent(LoginPage.this, PasswordChange.class);
                    intent.putExtra("dseCode", mDseCode);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(LoginPage.this, RetailerDetails1.class);
                    intent.putExtra("dseCode", mDseCode);
                    startActivity(intent);
                }

                finish();

            } else {
                Toast.makeText(LoginPage.this, failMessage, Toast.LENGTH_LONG).show();

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

    }

    public boolean isInternetAvail() {
        InternetConnectionManager iCM = new InternetConnectionManager();
        return iCM.isInternetConnAvail(getApplication());

    }
}

