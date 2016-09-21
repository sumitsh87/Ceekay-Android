package lukeworm.com.ceekayretailers;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.org.apache.http.HttpEntity;
import com.amazonaws.org.apache.http.HttpResponse;
import com.amazonaws.org.apache.http.client.HttpClient;
import com.amazonaws.org.apache.http.client.methods.HttpGet;
import com.amazonaws.org.apache.http.impl.client.DefaultHttpClient;
import com.amazonaws.org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RetailerDetails1 extends AppCompatActivity {
    public static String REV_GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    EditText dseCodeField, retailerNameEditText;
    Spinner RouteNoSpinner, RetailerCodeSpinner;
    ArrayAdapter<String> routeListAdapter, RetailerCodeAdapter;
    ArrayList<String> retailerNameList;
    LocationManager locationManager;
    Location currentLocation;
    Button nextBtn, fetchLocation;
    EditText latitudeVal, longitudeVal, address;
    JSONArray resultArray;
    Toolbar toolbar;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    TextView userName;
    Double currentLatitude, currentLongitude;
    String currentAddress;
    Handler routeHandler, retailerHandler, retailerHandler2;
    String routes, retailerCodes;
    String errorMessage;
    List<String> routeList, retailerCodeList;
    ProgressDialog progressDialog;
    String DSECode, selectedRetailerCode, selectedRouteCode, selectedRetailerName;
    public static String selectedRetailerLong, selectedRetailerLat, selectedRetailerAddress, selectedRetailerContact, selectedRetailerPan, selectedRetailerTin;
    String successMessage;
    boolean isGpsEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.retailer_details_1);

        initNavigationDrawer();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        dseCodeField = (EditText) findViewById(R.id.dseCode);
        retailerNameEditText = (EditText) findViewById(R.id.retailerName);


        RouteNoSpinner = (Spinner) findViewById(R.id.routeNo);
        RetailerCodeSpinner = (Spinner) findViewById(R.id.retailerCode);
        DSECode = getIntent().getStringExtra("dseCode");
        dseCodeField.setText(DSECode);

        setRouteAdapter(DSECode);
        RouteNoSpinner.setOnItemSelectedListener(routeSelectListener);

        RetailerCodeSpinner.setOnItemSelectedListener(RetailerSelectListener);
        RetailerCodeSpinner.setEnabled(false);

        nextBtn = (Button) findViewById(R.id.next);
        nextBtn.setOnClickListener(nextClickListener);


    }

    private void setRouteAdapter(final String DSECode) {
        routeList = new ArrayList<String>();
        showProgress(true);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    String url = Constants.getRouteListUrl + "?dse_code=" + DSECode + "&access_token=" + Constants.accessToken;
                    HttpGet httget = new HttpGet(url);
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpResponse response = httpclient.execute(httget);
                    HttpEntity entity = response.getEntity();
                    JSONObject responseJSON = new JSONObject(EntityUtils.toString(
                            entity, "UTF-8"));
                    boolean success = responseJSON.get("success") != null && responseJSON.get("success").toString().equals("true");
                    if (!success)
                        errorMessage = responseJSON.get("reason").toString();
                    else {
                        errorMessage = "";
                        routes = responseJSON.get("route").toString();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    errorMessage = "Could not fetch Route list, Kindly retry after a while.";
                }
                routeHandler.sendEmptyMessage(1);
            }
        };

        thread.start();

        routeHandler = new Handler() {
            public void handleMessage(Message msg) {
                showProgress(false);
                if (!errorMessage.equals("")) {
                    Toast.makeText(RetailerDetails1.this, errorMessage, Toast.LENGTH_LONG).show();
                    return;
                } else {
                    routeList = Arrays.asList(routes.split("\\s*,\\s*"));
                }
                ArrayAdapter<String> adp = new ArrayAdapter<String>(RetailerDetails1.this,
                        android.R.layout.simple_list_item_1, routeList);
                adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                RouteNoSpinner.setAdapter(adp);
            }
        };


    }

    private void setRetailerCodeAdapter() {
        showProgress(true);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    String url = Constants.getRetailerCodeListUrl + "?dse_code=" + DSECode + "&access_token=" + Constants.accessToken + "&route=" + selectedRouteCode;
                    HttpGet httget = new HttpGet(url);
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpResponse response = httpclient.execute(httget);
                    HttpEntity entity = response.getEntity();
                    JSONObject responseJSON = new JSONObject(EntityUtils.toString(
                            entity, "UTF-8"));
                    boolean success = responseJSON.get("success") != null && responseJSON.get("success").toString().equals("true");
                    if (!success)
                        errorMessage = responseJSON.get("reason").toString();
                    else {
                        errorMessage = "";
                        retailerCodes = responseJSON.get("retailer_code_list").toString();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    errorMessage = "Could not fetch Retailer Code list, Kindly retry after a while.";
                }
                retailerHandler.sendEmptyMessage(1);
            }
        };

        thread.start();

        retailerHandler = new Handler() {
            public void handleMessage(Message msg) {

                showProgress(false);
                if (!errorMessage.equals("")) {
                    Toast.makeText(RetailerDetails1.this, errorMessage, Toast.LENGTH_LONG).show();
                    return;
                } else {
                    retailerCodeList = Arrays.asList(retailerCodes.split("\\s*,\\s*"));
                }
                ArrayAdapter<String> adp = new ArrayAdapter<String>(RetailerDetails1.this,
                        android.R.layout.simple_list_item_1, retailerCodeList);
                adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                RetailerCodeSpinner.setAdapter(adp);
            }

        };


    }

    AdapterView.OnItemSelectedListener routeSelectListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
            selectedRouteCode = RouteNoSpinner.getSelectedItem().toString();
            Toast.makeText(getBaseContext(), selectedRouteCode + " selected", Toast.LENGTH_SHORT).show();

            if (!isInternetAvail()) {
                Toast.makeText(RetailerDetails1.this, "No network available, Kindly check your internet connectivity",
                        Toast.LENGTH_LONG).show();
                return;
            }

            setRetailerCodeAdapter();
            RetailerCodeSpinner.setEnabled(true);

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            Toast.makeText(getBaseContext(),
                    "No Route Selected", Toast.LENGTH_SHORT).show();
            RetailerCodeSpinner.setEnabled(false);


        }
    };

    AdapterView.OnItemSelectedListener RetailerSelectListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> arg0,
                                   View arg1, int pos, long arg3) {
            selectedRetailerCode = RetailerCodeSpinner.getSelectedItem().toString();
            Toast.makeText(getBaseContext(), selectedRetailerCode + " selected", Toast.LENGTH_SHORT).show();

            if (!isInternetAvail()) {
                Toast.makeText(RetailerDetails1.this, "No network available, Kindly check your internet connectivity",
                        Toast.LENGTH_LONG).show();
                return;
            }

            showProgress(true);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        String url = Constants.getRetailerDetailsUrl + "?dse_code=" + DSECode + "&access_token=" + Constants.accessToken + "&route=" + selectedRouteCode + "&retailer_code=" + selectedRetailerCode;
                        HttpGet httget = new HttpGet(url);
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpResponse response = httpclient.execute(httget);
                        HttpEntity entity = response.getEntity();
                        JSONObject responseJSON = new JSONObject(EntityUtils.toString(
                                entity, "UTF-8"));
                        boolean success = responseJSON.get("success") != null && responseJSON.get("success").toString().equals("true");
                        if (!success)
                            errorMessage = responseJSON.get("reason").toString();
                        else {
                            errorMessage = "";
                            selectedRetailerName = responseJSON.get("retailer_name").toString();
                            selectedRetailerLat = responseJSON.get("latitude").toString();
                            selectedRetailerLong = responseJSON.get("longitude").toString();
                            selectedRetailerAddress = responseJSON.get("address").toString();
                            selectedRetailerContact = responseJSON.get("contact_number").toString();
                            selectedRetailerPan = responseJSON.get("pan").toString();
                            selectedRetailerTin = responseJSON.get("tin").toString();

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        errorMessage = "Could not fetch Retailer Code list, Kindly retry after a while.";
                    }
                    retailerHandler2.sendEmptyMessage(1);
                }
            };

            thread.start();

            retailerHandler2 = new Handler() {
                public void handleMessage(Message msg) {

                    showProgress(false);
                    if (!errorMessage.equals("")) {
                        Toast.makeText(RetailerDetails1.this, errorMessage, Toast.LENGTH_LONG).show();
                        return;
                    }
                    retailerNameEditText.setText(selectedRetailerName);

                }

            };


        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            Toast.makeText(getBaseContext(),
                    "No Retailer Selected", Toast.LENGTH_SHORT).show();
            retailerNameEditText.setEnabled(false);
        }
    };


    OnClickListener nextClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {

            if (!isInternetAvail()) {

                Toast.makeText(RetailerDetails1.this, "No network available, Kindly check your internet connectivity",
                        Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(RetailerDetails1.this, RetailerDetails2.class);
            intent.putExtra("dseCode", DSECode);
            intent.putExtra("selectedRetailerCode", selectedRetailerCode);
            intent.putExtra("selectedRetailerAddress", selectedRetailerAddress);
            intent.putExtra("selectedRetailerLat", selectedRetailerLat);
            intent.putExtra("selectedRetailerLong", selectedRetailerLong);
            intent.putExtra("selectedRetailerContact", selectedRetailerContact);
            intent.putExtra("selectedRetailerPan", selectedRetailerPan);
            intent.putExtra("selectedRetailerTin", selectedRetailerTin);

            Bundle bundleAnimation = ActivityOptions
                    .makeCustomAnimation(getApplicationContext(),
                            R.anim.entry_animation_right,
                            R.anim.exit_animation_left).toBundle();
            startActivity(intent, bundleAnimation);
        }
    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            showExitConfirmationDialog();
        }
        return super.onKeyDown(keyCode, event);
    }

    void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RetailerDetails1.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(R.string.exit_confirmation);
        builder.setMessage(R.string.exit_confirmation_message);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setNegativeButton("Cancel", null);

        builder.setCancelable(true);
        AlertDialog dialog = builder.show();
        WindowManager.LayoutParams lp1 = dialog.getWindow().getAttributes();
        lp1.dimAmount = 0.8F;
        dialog.getWindow().setAttributes(lp1);
    }


    public boolean isInternetAvail() {
        InternetConnectionManager iCM = new InternetConnectionManager();
        return iCM.isInternetConnAvail(getApplication());

    }

    private void initNavigationDrawer() {
        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.apps_toolbar);
        setSupportActionBar(toolbar);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Intent intent = null;
                drawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {

                    case R.id.myRetailers:
                        intent = new Intent(RetailerDetails1.this, RetailersListViewer.class);
                        intent.putExtra("retailerListUrl", Constants.getMyRetailersUrl+"?dse_code="+Constants.userDseCode+"&access_token="+Constants.accessToken);
                        intent.putExtra("title", "All Retailers");
                        startActivity(intent);
                        return true;

                    case R.id.todays_accomplishments:
                        intent = new Intent(RetailerDetails1.this, RetailersListViewer.class);
                        intent.putExtra("retailerListUrl", Constants.getMyRetailersUpdatedTodayUrl+"?dse_code="+Constants.userDseCode+"&access_token="+Constants.accessToken);
                        intent.putExtra("title", "Today's Accomplishments");
                        startActivity(intent);
                        return true;

                    case R.id.pending_retailers:
                        intent = new Intent(RetailerDetails1.this, RetailersListViewer.class);
                        intent.putExtra("retailerListUrl", Constants.getPendingRetailers+"?dse_code="+Constants.userDseCode+"&access_token="+Constants.accessToken);
                        intent.putExtra("title", "Pending Retailers");
                        startActivity(intent);
                        return true;



                    case R.id.sign_out:
                        showExitConfirmationDialog();
                        return true;

                    default:
                        return true;


                }
            }
        });


    }

    void showProgress(boolean arg) {
        if (arg) {
            progressDialog = new ProgressDialog(RetailerDetails1.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();
        } else
            progressDialog.dismiss();

    }

}
