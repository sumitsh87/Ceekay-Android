package lukeworm.com.ceekayretailers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.org.apache.http.HttpEntity;
import com.amazonaws.org.apache.http.HttpResponse;
import com.amazonaws.org.apache.http.NameValuePair;
import com.amazonaws.org.apache.http.client.HttpClient;
import com.amazonaws.org.apache.http.client.entity.UrlEncodedFormEntity;
import com.amazonaws.org.apache.http.client.methods.HttpGet;
import com.amazonaws.org.apache.http.client.methods.HttpPost;
import com.amazonaws.org.apache.http.impl.client.DefaultHttpClient;
import com.amazonaws.org.apache.http.message.BasicNameValuePair;
import com.amazonaws.org.apache.http.protocol.HTTP;
import com.amazonaws.org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RetailerDetails2 extends AppCompatActivity {
    public static String REV_GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    Button saveBtn, fetchLocation;
    EditText latitudeVal, longitudeVal, address;
    Double currentLatitude, currentLongitude;
    String currentAddress;
    Handler retailerHandler2, retailerHandler;
    String errorMessage = "";
    ProgressDialog progressDialog;
    String DSECode, selectedRetailerCode;
    String successMessage;
    EditText retailerCode, retailerContactNo, retailerTinNo, retailerPanNo;
    boolean isGpsEnabled = false;
    JSONArray resultArray;
    String selectedRetailerLong, selectedRetailerLat, selectedRetailerAddress, selectedRetailerContact, selectedRetailerPan, selectedRetailerTin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.retailer_details_2);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        saveBtn = (Button) findViewById(R.id.save);
        fetchLocation = (Button) findViewById(R.id.fetchLocation);
        latitudeVal = (EditText) findViewById(R.id.latitude);
        longitudeVal = (EditText) findViewById(R.id.longitude);
        address = (EditText) findViewById(R.id.address);
        retailerCode = (EditText) findViewById(R.id.retailerCode2);
        retailerContactNo = (EditText) findViewById(R.id.contact);
        retailerTinNo = (EditText) findViewById(R.id.tin);
        retailerPanNo = (EditText) findViewById(R.id.pan);


        fetchLocation.setOnClickListener(fetchLocationClickListener);
        saveBtn.setOnClickListener(retailerDetailsSaveListener);

        DSECode = getIntent().getStringExtra("dseCode");
        selectedRetailerCode = getIntent().getStringExtra("selectedRetailerCode");
        selectedRetailerLong = getIntent().getStringExtra("selectedRetailerLong");
        selectedRetailerLat = getIntent().getStringExtra("selectedRetailerLat");
        selectedRetailerAddress = getIntent().getStringExtra("selectedRetailerAddress");
        selectedRetailerContact = getIntent().getStringExtra("selectedRetailerContact");
        selectedRetailerPan = getIntent().getStringExtra("selectedRetailerPan");
        selectedRetailerTin = getIntent().getStringExtra("selectedRetailerTin");


        retailerCode.setText(selectedRetailerCode);
        latitudeVal.setText(selectedRetailerLat);
        longitudeVal.setText(selectedRetailerLong);
        address.setText(selectedRetailerAddress);
        retailerContactNo.setText(selectedRetailerContact);
        retailerTinNo.setText(selectedRetailerTin);
        retailerPanNo.setText(selectedRetailerPan);

        setupBackButton();


    }

    void setupBackButton() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.apps_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.entry_animation_left, R.anim.exit_animation_right);

            }
        });
    }


    OnClickListener fetchLocationClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {

            if (!isInternetAvail()) {

                Toast.makeText(RetailerDetails2.this, "No network available, Kindly check your internet connectivity",
                        Toast.LENGTH_LONG).show();
                return;
            }
            LocationManager lm = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);

            try {
                isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
            }

            if (isGpsEnabled) {
                Intent intent = new Intent(RetailerDetails2.this, MapsActivity.class);
                startActivityForResult(intent, 1);
            } else
                Toast.makeText(RetailerDetails2.this, "GPS not enabled. Kindly enable it on from your settings.",
                        Toast.LENGTH_LONG).show();


        }
    };

    OnClickListener retailerDetailsSaveListener = new OnClickListener() {

        @Override
        public void onClick(View view) {

            if (!isInternetAvail()) {
                Toast.makeText(RetailerDetails2.this, "No network available, Kindly check your internet connectivity",
                        Toast.LENGTH_LONG).show();
                return;
            }
            updateRetailerLocationToDB();
        }
    };

    public void updateLocalCachedValues() {

        RetailerDetails1.selectedRetailerLat = latitudeVal.getText().toString();
        RetailerDetails1.selectedRetailerLong = longitudeVal.getText().toString();
        RetailerDetails1.selectedRetailerAddress = address.getText().toString();
        RetailerDetails1.selectedRetailerContact = retailerContactNo.getText().toString();
        RetailerDetails1.selectedRetailerTin = retailerTinNo.getText().toString();
        RetailerDetails1.selectedRetailerPan = retailerPanNo.getText().toString();


    }


    public void updateRetailerLocationToDB() {


        showProgress(true);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    HttpPost httpPost = new HttpPost(Constants.updateRetailerAddress);
                    List<NameValuePair> nvps = new ArrayList<NameValuePair>();

                    nvps.add(new BasicNameValuePair("dse_code", DSECode));
                    nvps.add(new BasicNameValuePair("access_token", Constants.accessToken));
                    nvps.add(new BasicNameValuePair("retailer_code", selectedRetailerCode));
                    nvps.add(new BasicNameValuePair("latitude", latitudeVal.getText().toString()));
                    nvps.add(new BasicNameValuePair("longitude", longitudeVal.getText().toString()));
                    nvps.add(new BasicNameValuePair("address", address.getText().toString()));
                    nvps.add(new BasicNameValuePair("contact_number", retailerContactNo.getText().toString()));
                    nvps.add(new BasicNameValuePair("tin", retailerTinNo.getText().toString()));
                    nvps.add(new BasicNameValuePair("pan", retailerPanNo.getText().toString()));

                    HttpClient httpclient = new DefaultHttpClient();
                    httpPost.setEntity(new UrlEncodedFormEntity(nvps,
                            HTTP.UTF_8));
                    HttpResponse response = httpclient.execute(httpPost);
                    HttpEntity entity = response.getEntity();
                    JSONObject responseJSON = new JSONObject(EntityUtils.toString(
                            entity, "UTF-8"));
                    boolean success = responseJSON.get("success") != null && responseJSON.get("success").toString().equals("true");
                    if (!success) {
                        errorMessage = responseJSON.get("reason").toString();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    errorMessage = "Could not save location, Kindly retry after a while.";
                }
                retailerHandler2.sendEmptyMessage(1);
            }
        };

        thread.start();

        retailerHandler2 = new Handler() {
            public void handleMessage(Message msg) {

                showProgress(false);
                if (!errorMessage.equals("")) {
                    Toast.makeText(RetailerDetails2.this, errorMessage, Toast.LENGTH_LONG).show();
                } else {
                    successMessage = "Retailer Location successfully updated in the server";
                    Toast.makeText(RetailerDetails2.this, successMessage, Toast.LENGTH_LONG).show();
                    updateLocalCachedValues();

                }
            }

        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == MapsActivity.SEND_LOCATION) {
            currentLatitude = intent.getDoubleExtra("latitude", 0.000000);
            currentLongitude = intent.getDoubleExtra("longitude", 0.000000);
            latitudeVal.setText(String.valueOf(currentLatitude));
            longitudeVal.setText(String.valueOf(currentLongitude));

            if (!isInternetAvail()) {
                Toast.makeText(RetailerDetails2.this, "No network available, Kindly check your internet connectivity",
                        Toast.LENGTH_LONG).show();
                return;
            }

            fetchAndUpdateCurrentAddress(currentLatitude, currentLongitude);

        }

        super.onActivityResult(requestCode, resultCode, intent);
    }


    private void fetchAndUpdateCurrentAddress(final Double currentLatitude, final Double currentLongitude) {

        if (!isInternetAvail()) {
            Toast.makeText(RetailerDetails2.this, "No network available, Kindly check your internet connectivity", Toast.LENGTH_LONG).show();
            return;
        }
        showProgress(true);
        Thread thread = new Thread() {
            @Override
            public void run() {

                String url = REV_GEOCODING_URL + "?latlng=" + currentLatitude + "," + currentLongitude;
                GetExample example = new GetExample();
                String response = null;

                try {
                    response = example.run(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(response);
                try {
                    JSONObject responseJSON = new JSONObject(response);
                    resultArray = responseJSON.getJSONArray("results");
                    JSONObject addressListJsonArray = resultArray.getJSONObject(0);
                    if (addressListJsonArray != null)
                        currentAddress = resultArray.getJSONObject(0).get("formatted_address").toString();
                    else {
                        Toast.makeText(RetailerDetails2.this, "Address for the given coordinates not available currently", Toast.LENGTH_LONG).show();
                        currentAddress = "";
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                retailerHandler.sendEmptyMessage(1);
            }
        };

        thread.start();

        retailerHandler = new Handler() {
            public void handleMessage(Message msg) {

                showProgress(false);
                address.setText(currentAddress);
                address.setEnabled(true);

            }

        };

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
            overridePendingTransition(R.anim.entry_animation_left, R.anim.exit_animation_right);

        }
        return super.onKeyDown(keyCode, event);
    }

    public class GetExample {
        OkHttpClient client = new OkHttpClient();

        String run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                return response.body().string();
            }
        }

    }

    public boolean isInternetAvail() {
        InternetConnectionManager iCM = new InternetConnectionManager();
        return iCM.isInternetConnAvail(getApplication());

    }


    void showProgress(boolean arg) {
        if (arg) {
            progressDialog = new ProgressDialog(RetailerDetails2.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();
        } else
            progressDialog.dismiss();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_reset, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.reset) {
            resetActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void resetActivity() {
        finish();
        startActivity(getIntent());
    }

}
