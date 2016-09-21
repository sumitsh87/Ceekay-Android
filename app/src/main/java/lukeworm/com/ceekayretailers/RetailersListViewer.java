package lukeworm.com.ceekayretailers;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Movie;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
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
import java.util.List;

public class RetailersListViewer extends AppCompatActivity {
    RecyclerView recyclerView;
    RetailerListAdapter mAdapter;
    private List<Retailer> retailerList = new ArrayList<>();
    JSONObject responseJSON;
    JSONArray resultArray;
    String retailerCode, retailerName, retailerRoute, retailerDse;
    ProgressDialog progressDialog;
    String errorMessage;
    String retailerListUrl;
    String selectedRetailerName, selectedRetailerLat, selectedRetailerLong, selectedRetailerAddress, selectedRetailerContact, selectedRetailerPan,  selectedRetailerTin;
    Handler retailerHandler1, retailerHandler2;
    String pageTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retailers_list);
        retailerListUrl = getIntent().getStringExtra("retailerListUrl");
        pageTitle = getIntent().getStringExtra("title");

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mAdapter = new RetailerListAdapter(retailerList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                showRetailersDetail(retailerList.get(position));
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        setupBackButton();
        prepareRetailerData();
    }


    public void prepareRetailerData() {


        if (!isInternetAvail()) {
            Toast.makeText(RetailersListViewer.this, "No network available, Kindly check your internet connectivity",
                    Toast.LENGTH_LONG).show();
            return;
        }

        showProgress(true);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    HttpGet httget = new HttpGet(retailerListUrl);
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpResponse response = httpclient.execute(httget);
                    HttpEntity entity = response.getEntity();
                    responseJSON = new JSONObject(EntityUtils.toString(
                            entity, "UTF-8"));
                    boolean success = responseJSON.get("success") != null && responseJSON.get("success").toString().equals("true");
                    if (!success)
                        errorMessage = responseJSON.get("reason").toString();
                    else {
                        errorMessage = "";
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    errorMessage = "Could not fetch Retailer Code list, Kindly retry after a while.";
                }
                retailerHandler1.sendEmptyMessage(1);
            }
        };

        thread.start();

        retailerHandler1 = new Handler() {
            public void handleMessage(Message msg) {

                showProgress(false);
                if (errorMessage.equals("")) {
                    try {
                        resultArray = responseJSON.getJSONArray("retailers");
                        for (int index = 0; index < resultArray.length(); index++) {
                            retailerCode = resultArray.getJSONObject(index).get("retailer_code").toString();
                            retailerName = resultArray.getJSONObject(index).get("retailer_name").toString();
                            retailerDse = resultArray.getJSONObject(index).get("dse_code").toString();
                            retailerRoute = resultArray.getJSONObject(index).get("route").toString();
                            Retailer retailer = new Retailer(retailerName, retailerCode, retailerDse, retailerRoute);
                            retailerList.add(retailer);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mAdapter.notifyDataSetChanged();
                    return;
                } else {
                    Toast.makeText(RetailersListViewer.this, "No Retailers found", Toast.LENGTH_LONG).show();
                }
            }

        };

    }

    void showProgress(boolean arg) {
        if (arg) {
            progressDialog = new ProgressDialog(RetailersListViewer.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();
        } else
            progressDialog.dismiss();

    }

    private void showRetailersDetail(final Retailer retailer ){
        if (!isInternetAvail()) {
            Toast.makeText(RetailersListViewer.this, "No network available, Kindly check your internet connectivity",
                    Toast.LENGTH_LONG).show();
            return;
        }

        showProgress(true);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    String url = Constants.getRetailerDetailsUrl + "?dse_code=" + retailer.getDse() + "&access_token=" + Constants.accessToken + "&route=" + retailer.getRoute() + "&retailer_code=" + retailer.getCode();
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
                    errorMessage = "Could not fetch Retailer Code list details, Kindly retry after a while.";
                }
                retailerHandler2.sendEmptyMessage(1);
            }
        };

        thread.start();

        retailerHandler2 = new Handler() {
            public void handleMessage(Message msg) {

                showProgress(false);
                if (!errorMessage.equals("")) {
                    Toast.makeText(RetailersListViewer.this, errorMessage, Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(RetailersListViewer.this, RetailerDetails2.class);
                intent.putExtra("dseCode", Constants.userDseCode);
                intent.putExtra("selectedRetailerCode", retailer.getCode());
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

    }

    public boolean isInternetAvail() {
        InternetConnectionManager iCM = new InternetConnectionManager();
        return iCM.isInternetConnAvail(getApplication());

    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }


    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private RetailersListViewer.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final RetailersListViewer.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    void setupBackButton() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.apps_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity)RetailersListViewer.this).getSupportActionBar().setTitle(pageTitle);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.entry_animation_left, R.anim.exit_animation_right);
            }
        });
    }
}
