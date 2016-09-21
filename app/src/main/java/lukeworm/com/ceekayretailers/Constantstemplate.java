package lukeworm.com.ceekayretailers;

import android.app.ProgressDialog;

/**
 * Created by sumitsharma on 16/06/16.
 */
public class Constantstemplate {

   public static String baseUrl = "";
//
    public static String loginUrl = baseUrl+"/v1/account/session/login";
    public static String getRouteListUrl = baseUrl+"/v1/account/dse/get_dse_routes";
    public static String editProfileUrl = baseUrl+"/v1/account/profile/edit_profile";
    public static String updatePasswordUrl = baseUrl+"/v1/account/profile/update_password";

    public static String getRetailerCodeListUrl = baseUrl+"/v1/account/retailer/get_retailer_code_list";
    public static String getRetailerDetailsUrl = baseUrl+"/v1/account/retailer/get_retailer_details";
    public static String updateRetailerAddress = baseUrl+"/v1/account/retailer/update_retailer_address";

    public static String getMyRetailersUrl = baseUrl+"/v1/account/retailer/get_my_retailers";
    public static String getMyRetailersUpdatedTodayUrl = baseUrl+"/v1/account/retailer/get_my_retailers_updated_today";
    public static String getPendingRetailers= baseUrl+"/v1/account/retailer/get_my_pending_retailers";

    public static String accessToken;
    public static String userDseCode;

    public static String userRole;
    public static boolean isFirstLogin;
    public static ProgressDialog progressDialog;



}
