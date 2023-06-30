package dill.group.riparianreport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
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
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class Map extends AppCompatActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        // Upon clicking a specific marker, it will get the most recent report that was submitted from that location.
        String title = marker.getTitle();
        ArrayList<String> questions = new ArrayList<String>();
        ArrayList<String> answers = new ArrayList<String>();
        for (HashMap<String, String> curr : Main.reports) {
            questions = new ArrayList<String>();
            answers = new ArrayList<String>();
            if (Objects.equals(curr.get("Site name or location"), title)) {
                for (String s : curr.keySet()) {
                    Log.d("DEBUG", s + " : " + curr.get(s));
                    questions.add(s);
                    answers.add(curr.get(s));
                }
                if (!questions.isEmpty()) {
                    Intent i = new Intent(getApplicationContext(), HistoryInner.class);
                    String[] qarray = questions.toArray(new String[0]);
                    String[] aarray = answers.toArray(new String[0]);
                    i.putExtra("questions",qarray);
                    i.putExtra("answers",aarray);
                    startActivity(i);
                }

            }
        }


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

            StringBuilder data = new StringBuilder();
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
        catch (MalformedURLException | JSONException murle) {
            murle.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
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
                        MarkerOptions markOpt = new MarkerOptions().position(output).title(title).icon(BitmapDescriptorFactory.defaultMarker((float)60.0 + rng.nextFloat()*(float)60.0)).alpha(0.5f);
                        for (HashMap<String, String> qa : Main.reports) {
                            if (Objects.equals(qa.get("Site name or location"), title)) {
                                markOpt.alpha(1f);
                            }
                        }
                        googleMap.addMarker(markOpt);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(output));
                        googleMap.moveCamera(CameraUpdateFactory.zoomTo(10f));
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

    ArrayList<LatLng> site_latlng = new ArrayList<LatLng>();


    @Override
    public void onResume(){
        super.onResume();
        Toast.makeText(Map.this, "Mapping feature removed.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.INTERNET},1);

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        for (int i = 0; i < Main.names.size(); i++) {
            Map.SiteLatLngExecutor slle = new Map.SiteLatLngExecutor();
            slle.fetch(Main.locations.get(i),Main.names.get(i),googleMap,sllc);
        }
        googleMap.setOnMarkerClickListener(this);
    }
}