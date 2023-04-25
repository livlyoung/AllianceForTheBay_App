package dill.group.riparianreport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class Map extends AppCompatActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        // Upon clicking a specific marker, it will get the most recent report that was submitted from that location.
        return false;
    }

    interface SiteLatLngCallback {
        void onComplete(LatLng output, String title, GoogleMap googleMap);
    }

    public LatLng getLatLng(String location) {
        LatLng latlng = new LatLng(0,0);
        try {
            URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + location + "&key=AIzaSyCHngQx3l2R6XJPdm3-IxVj1r1QJm_uSNI");

            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            StringBuffer data = new StringBuffer();
            String curr;
            while ((curr = in.readLine()) != null) {
                data.append(curr);
            }
            JSONObject root = new JSONObject(data.toString());
            JSONArray results = root.getJSONArray("results");
            JSONObject geometry = ((JSONObject)results.get(0)).getJSONObject("geometry");
            JSONObject j_location = geometry.getJSONObject("location");
            latlng = new LatLng(j_location.getDouble("lat"),j_location.getDouble("lng"));
        }
        catch (MalformedURLException murle) {
            murle.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        catch (JSONException jsone) {
            jsone.printStackTrace();
        }
        return latlng;
    }
    SiteLatLngCallback sllc = new SiteLatLngCallback() {
        @Override
        public void onComplete(LatLng output, String title, GoogleMap googleMap) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (output != null) {
                        Random rng = new Random();
                        site_latlng.add(output);

                        googleMap.addMarker(new MarkerOptions().position(output).title(title).icon(BitmapDescriptorFactory.defaultMarker((float)60.0 + rng.nextFloat()*(float)60.0)).alpha(0.7f));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(output));
                    }
                }
            });
        }
    };

    public class SiteLatLngExecutor {
        public void fetch(String location, String title, GoogleMap googleMap, final Map.SiteLatLngCallback sllc) {
            ExecutorService es = Executors.newFixedThreadPool(1);
            es.execute(new Runnable() {
                @Override
                public void run() {
                    LatLng output = getLatLng(location);
                    Log.d("MAIN","Running from thread!");
                    sllc.onComplete(output, title, googleMap);
                }
            });
        }
    }

    String[] site_names = {"Christiana Park", "Stephan Petersheim", "Warwick Twp Saylor Park", "Church of the Apostles", "Hunter Hess", "Shawn Peirson", "Shallow Brook Elementary", "South Lebanon Twp", "Laleh Baublitz", "Usdin", "David Stoltzfus", "Michael King", "Tom Savage", "Summit Living", "Joe Shirk/Davis Stoltzfoos", "Scott Weber", "Fleishmann et al (Swarr Run)", "Hempfield Twp", "Tim White", "Wittel Farm", "Dan McAllister", "Little Creek Park", "Jacob Kaufman", "Ashley Conrad", "Shannan Donivan", "Ron Funk", "Houser", "Oxford Boro", "Tisha Walmer", "Millport Conservancy", "Paradise Twp", "Tom Kessler", "Brilyn Acres (Lynette and Brian Sauder)", "John Stoltzfus", "Hempfield Rec Center", "New Enterprise Stone and Lime", "Abner Stoltzfus", "Overlook Park", "Jason Hondru", "Joseph Glick ", "Shallowbrook Elementary", "Logan Park", "Tim White - TU Planting", "Scott Mullins", "Jackson Edwards", "Jeff and Tina Gleim", "Plum Creek Park", "LCBC Manheim", "Jason Fauth", "John Stoltzfus", "Conestoga Reserve ", "Windemere Lane", "Northeastern Middle School"};
    String[] site_locations = {"232 S Bridge St, Christiana, PA 17509", "232 Old Dam Rd, Christiana PA", "301 E Newport Rd, Lititz, PA 17543", "1950 Apostle Way, Lancaster, PA 17603", "400 Becker Rd, Leola PA 17540", "825 Lititz Rd, Manheim, PA 17545, USA", "213 S Hartman St, Manchester, PA 17345", "747 Kiner Ave, Lebanon PA", "804 Stone House Ln, Lititz, PA", "20 Wildflower Lane, Lancaster PA 17603", "388 School Lane Rd, Gap, PA 17527", "1093 Mt Pleasant Rd, Quarryville, PA 17566", "36 Maple Shade Rd, Christiana, PA", "755 Summit Dr, Lancaster, PA 17601, USA", "234 S Groffdale Rd, Leola, PA 17540, USA", "85 Spring Run Road Conestoga, PA", "3060 Harrisburg Pike, Landisville, PA 17538", "40.086681, -76.391330", "1236 Woolen Mill Rd, Stewartstown, PA 17363, USA", "1753 Mill Rd, Elizabethtown PA 17022", "423 Valley Road, Goldsboro, PA", "1657 PA-116, Spring Grove, PA 17362", "1036 Prawls Hollow Rd. Peach Bottom, PA 17563", "475 Buch Ave, Lancaster PA 17601", "1987 Glen Rock Rd. Glen Rock, PA 17327", "136 Voneida St, Narvon, PA 17555, USA", "125 Hopeland Rd. Newmanstown, PA", "450 W Locust St, Oxford, PA 19363", "220 Distillery Rd, Newmanstown, PA 17073, USA", "737 E Millport Rd, Lititz, PA 17543 (40.134484, -76.258895)", "2 Township Dr, Paradise, PA 17562 (40.0048818002664, -76.10987308958515)", "430 Quaker Church Rd, York Springs PA", "153 Millway Rd, Ephrata, PA 17522", "5608 Meadville Rd, Gap PA", "950 Church Street, Landisville, PA", "300 Quarry Rd, Chambersburg, PA 17202. 39.920293, -77.628496", "263 Byerland Church Rd Willow Street, PA", "40.08012, -76.32258", "416 Lebanon Road, Manheim, PA", "1003 Mondale Rd. Bird in Hand PA 17505", "213 S Hartman St, Manchester, PA 17345 (40.057676, -76.714284)", "80 Logan Rd, Dillsburg, PA 17019", "1236 Woolen Mill Rd, Stewartstown, PA 17363", "208 Kennedy Rd, Airville PA 17302", "1085 Milton Grove Rd, Mt. Joy PA 17552", "34 N. Peiffer Rd Wellsville, PA 17365", "98 Airport Rd, Hanover PA 17331", "2392 Mount Joy Rd, Manheim, PA 17545", "2890 Admire Rd, York PA 17315", "254 Big Oak Rd. Spring Mills", "8 Woodlyn Ct, Lancaster, PA 17602", "Windemere Lane, Landisville, Pennsylvania", "300 Chestnut Street Mount Wolf PA"};
    ArrayList<LatLng> site_latlng = new ArrayList<LatLng>();

    LatLng findClosest(ArrayList<LatLng> lat_lng_list, LatLng location) {
        double pb = -32767;
        LatLng lb = new LatLng(0,0);
        for (LatLng curr : lat_lng_list) {
            double curr_dist = Math.sqrt(Math.pow((curr.latitude - location.latitude),2) + Math.pow((curr.longitude - location.longitude),2));
            if (pb < curr_dist) {
                pb = curr_dist;
                lb = curr;
            }
        }
        return lb;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.INTERNET},1);

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng sydney = new LatLng(-33.852, 151.211);
        for (int i = 0; i < site_names.length; i++) {
            Map.SiteLatLngExecutor slle = new Map.SiteLatLngExecutor();
            slle.fetch(site_locations[i],site_names[i],googleMap,sllc);
        }
    }
}