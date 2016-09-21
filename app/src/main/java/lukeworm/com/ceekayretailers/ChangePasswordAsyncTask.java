package lukeworm.com.ceekayretailers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
 * Created by sumitsharma on 16/06/16.
 */
public class ChangePasswordAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private String oldPwd, newPwd, dseCode;
    ProgressDialog progressDialog;
    PasswordChange activity;
    String failMessage;

    ChangePasswordAsyncTask(String dseCode, String oldPwd, String newPwd, PasswordChange activity) {
        this.oldPwd = oldPwd;
        this.newPwd = newPwd;
        this.dseCode = dseCode;
        this.activity = activity;

    }

    @Override
    protected void onPreExecute(){
        activity.showProgress(true);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            HttpPost httpPost = new HttpPost(Constants.updatePasswordUrl);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();

            nvps.add(new BasicNameValuePair("dse_code", dseCode));
            nvps.add(new BasicNameValuePair("old_password", oldPwd));
            nvps.add(new BasicNameValuePair("new_password", newPwd));
            nvps.add(new BasicNameValuePair("access_token", Constants.accessToken));
            HttpClient httpclient = new DefaultHttpClient();
            httpPost.setEntity(new UrlEncodedFormEntity(nvps,
                    HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            JSONObject responseJSON = new JSONObject(EntityUtils.toString(
                    entity, "UTF-8"));
            boolean success = responseJSON.get("success").toString().equals("true");
            if (!success){
                failMessage = responseJSON.get("reason").toString();
                return false;
            }


        } catch (Exception e) {
            failMessage = "Failed to update the password, retry after some time.";
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {

        activity.showProgress(false);

        if (success) {
//                finish();
            Toast.makeText(activity, "Password Changed Successfully", Toast.LENGTH_LONG).show();
            activity.gotoHomePageAfterPwdChange();

        } else {
            Toast.makeText(activity, failMessage, Toast.LENGTH_LONG).show();
        }
    }

}
