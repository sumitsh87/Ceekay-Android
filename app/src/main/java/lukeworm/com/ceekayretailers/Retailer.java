package lukeworm.com.ceekayretailers;

/**
 * Created by sumitsharma on 19/06/16.
 */
public class Retailer {

    private String name, code, dse_code, route;

    public Retailer() {
    }

    public Retailer(String name, String code, String dse_code, String route) {
        this.name = name;
        this.code = code;
        this.dse_code = dse_code;
        this.route = route;

    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDse() {
        return dse_code;
    }

    public String getRoute() {
        return route;
    }

}
