package com.example.sue;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.collections.GroundOverlayManager;
import com.google.maps.android.collections.MarkerManager;
import com.google.maps.android.collections.PolygonManager;
import com.google.maps.android.collections.PolylineManager;
import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.Renderer;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlLineString;
import com.google.maps.android.data.kml.KmlPlacemark;
import com.google.maps.android.data.kml.KmlPoint;
import com.google.maps.android.data.kml.KmlPolygon;

import org.xmlpull.v1.XmlPullParserException;


public class KmlActivity extends MapsActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    boolean fabExpanded = false;
    Uri uri;
    String st;
    TextView loading;
    FloatingActionButton fab, sat, def;
    CardView satCard, defCard;

    private boolean permissionDenied = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    public void start() {
        try {
            loading = (TextView) findViewById(R.id.loading);
            satCard = (CardView) findViewById(R.id.sat);
            defCard = (CardView) findViewById(R.id.def);
            Intent intent = getIntent();
            st = intent.getStringExtra(MainActivity.URI_STRING);
            mMap = getMap();
            fab = findViewById(R.id.fab);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
            enableMyLocation();

            fab.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    if (fabExpanded == true){
                        closeSubMenusFab();
                    } else { openSubMenusFab(); } } });
            sat = findViewById(R.id.sat_fab);
            sat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getMap().getMapType() != GoogleMap.MAP_TYPE_SATELLITE){
                        getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    }
                }
            });
            def = findViewById(R.id.def_fab);
            def.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getMap().getMapType() != GoogleMap.MAP_TYPE_NORMAL){
                        getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    }
                }
            });
            fab.setVisibility(View.INVISIBLE);
            sat.setVisibility(View.INVISIBLE);
            satCard.setVisibility(View.INVISIBLE);
            def.setVisibility(View.INVISIBLE);
            defCard.setVisibility(View.INVISIBLE);

            retrieveFileFromUrl(st);
        } catch (Exception e) {
            Log.e("Exception caught", e.toString());
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (grantResults.length > 0) {
            for (int i = 1; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    permissionDenied = true;
                    return;
                }
            }
        }
        enableMyLocation();
    }



    private void openSubMenusFab() {
        try {
            sat.setVisibility(View.VISIBLE);
            satCard.setVisibility(View.VISIBLE);
            def.setVisibility(View.VISIBLE);
            defCard.setVisibility(View.VISIBLE);
        }
        catch (NullPointerException e){
            Log.e("Exception caught", e.toString());
        }
        fab.setImageResource(R.drawable.ic_baseline_close_24);
        fabExpanded = true;
    }

    private void closeSubMenusFab() {
        try {
            sat.setVisibility(View.INVISIBLE);
            satCard.setVisibility(View.INVISIBLE);
            def.setVisibility(View.INVISIBLE);
            defCard.setVisibility(View.INVISIBLE);
        }
        catch (NullPointerException e){
            Log.e("Exception caught", e.toString());
        }
        fab.setImageResource(R.drawable.ic_baseline_layers_24);
        fabExpanded = false;
    }

    private void retrieveFileFromUrl(String st) {
        new DownloadKmlFile(st).execute();
    }

    private double distance(LatLng test) {
        Location tloc = new Location(LocationManager.GPS_PROVIDER);
        Location sloc = new Location(LocationManager.GPS_PROVIDER);

        tloc.setLatitude(test.latitude);
        tloc.setLongitude(test.longitude);
        sloc.setLatitude(startlatlong.latitude);
        sloc.setLongitude(startlatlong.longitude);

        return tloc.distanceTo(sloc);
    }

    private double distanceEnd(LatLng test) {
        Location tloc = new Location(LocationManager.GPS_PROVIDER);
        Location sloc = new Location(LocationManager.GPS_PROVIDER);

        tloc.setLatitude(test.latitude);
        tloc.setLongitude(test.longitude);
        sloc.setLatitude(endlatlong.latitude);
        sloc.setLongitude(endlatlong.longitude);

        return tloc.distanceTo(sloc);
    }

    Marker deepestMarker,nearestMarker,nearestEndMarker,startMarker,endMarker;

    private void calculate(KmlLayer kmlLayer) {
        findDeepestUtility(kmlLayer.getContainers());
        deepestMarker = getMap().addMarker(new MarkerOptions().position(deepestLatLong).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title("Deepest Utility:" + deepestUtil).snippet(maxStringDepth.toLowerCase()+ " at " + deepestLatLong));
        findNearestStatic(kmlLayer.getContainers());
        minDist = Math.round(minDist*100)/100;
        nearestMarker = getMap().addMarker(new MarkerOptions().position(nearestStatic).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)).title("Nearest To Start: "+ nearestDesc).snippet((String.valueOf(minDist))+ " meters"));
        findNearestStaticEnd(kmlLayer.getContainers());
        minDistEnd = Math.round(minDistEnd*100)/100;
        nearestEndMarker = getMap().addMarker(new MarkerOptions().position(nearestStaticEnd).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)).title("Nearest To End:"+ nearestDescEnd).snippet((String.valueOf(minDistEnd)) + " meters"));

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    AlertDialog.Builder builder1,builder2,builder3,builder4;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.home:
                Intent intent = new Intent(this,MainActivity.class);
                builder3 = new AlertDialog.Builder(this);
                builder3.setMessage("Are you sure you want to exit to Main Menu?").setTitle("Exit")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mMap.clear();
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert3 = builder3.create();
                alert3.show();
                return true;
            case R.id.deepest_utility:
                builder1 = new AlertDialog.Builder(this);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(deepestLatLong,20));
                builder1.setMessage("Deepest Utility is " + deepestUtil + " With " + maxStringDepth.toLowerCase() + " Located at "+ deepestLatLong) .setTitle(R.string.deepestUtilTitle)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                AlertDialog alert1 = builder1.create();
                alert1.show();
                deepestMarker.showInfoWindow();
                return true;

            case R.id.nearest_static:
                builder2 = new AlertDialog.Builder(this);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nearestStatic,20));
                builder2.setMessage("Name " + nearestName + " and Description " + nearestDesc + " at Distance: " + minDist+" meters from Start") .setTitle(R.string.nearestStat)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                AlertDialog alert2 = builder2.create();
                alert2.show();
                nearestMarker.showInfoWindow();
                return true;

            case R.id.nearest_static_end:
                builder4 = new AlertDialog.Builder(this);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nearestStaticEnd,20));
                builder4.setMessage("Name " + nearestNameEnd + " and Description " + nearestDescEnd + " at Distance: " + minDistEnd+" meters from End") .setTitle(R.string.nearestStat)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                AlertDialog alert4 = builder4.create();
                alert4.show();
                nearestEndMarker.showInfoWindow();
                return true;

            case R.id.start_point:
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startlatlong,20));
                startMarker.showInfoWindow();
                return true;

            case R.id.end_point:
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(endlatlong,20));
                endMarker.showInfoWindow();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    LatLng startlatlong, endlatlong, deepestLatLong, nearestStatic, nearestStaticEnd;
    float maxDepth = 0;
    String maxStringDepth, deepestUtil, nearestDesc, nearestName, nearestNameEnd, nearestDescEnd;
    double minDist = 100000.0, minDistEnd = 100000.0;


    private void findDeepestUtility(Iterable<KmlContainer> containers) {
        for (KmlContainer container : containers) {
            Log.i("Worked", container.getProperty("name"));
            for (KmlPlacemark placemark : container.getPlacemarks()) {
                Geometry geometry = placemark.getGeometry();
                if (placemark.hasProperty("name") && placemark.hasProperty("description")) {
                    if (placemark.getProperty("description").toLowerCase().contains("depth") && geometry.getGeometryType().equals("LineString")) {
                        String number = placemark.getProperty("description").replaceAll("\\D+", "");
                        if (Integer.parseInt(number) > maxDepth) {
                            maxDepth = Integer.parseInt(number);
                            maxStringDepth = placemark.getProperty("name");
                            deepestUtil = placemark.getProperty("description");
                            KmlLineString lineString = (KmlLineString) placemark.getGeometry();
                            deepestLatLong = new LatLng(lineString.getGeometryObject().get(0).latitude, lineString.getGeometryObject().get(0).longitude);
                        }
                    }

                    if (String.valueOf(geometry).contains("LineString") && placemark.getProperty("name").equals("Start Point")) {
                        KmlLineString lineString = (KmlLineString) placemark.getGeometry();
                        startlatlong = new LatLng(lineString.getGeometryObject().get(0).latitude, lineString.getGeometryObject().get(0).longitude);
                        getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(startlatlong, 15));
                        startMarker = getMap().addMarker(new MarkerOptions().position(startlatlong).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("Start Point"));
                    }
                    else if(String.valueOf(geometry).contains("Point") && placemark.getProperty("name").equals("Start Point")){
                        KmlPoint point = (KmlPoint) placemark.getGeometry();
                        startlatlong = new LatLng(point.getGeometryObject().latitude, point.getGeometryObject().longitude);
                        getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(startlatlong, 15));
                        startMarker = getMap().addMarker(new MarkerOptions().position(startlatlong).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("Start Point"));
                    }
                    else if(String.valueOf(geometry).contains("Polygon") && placemark.getProperty("name").equals("Start Point")){
                        KmlPolygon polygon = (KmlPolygon) placemark.getGeometry();
                        startlatlong = new LatLng(polygon.getOuterBoundaryCoordinates().get(0).latitude,polygon.getOuterBoundaryCoordinates().get(0).longitude);
                        getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(startlatlong, 15));
                        startMarker = getMap().addMarker(new MarkerOptions().position(startlatlong).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("Start Point"));
                    }
                    else if (String.valueOf(geometry).contains("LineString") && placemark.getProperty("name").equals("End Point")) {
                        KmlLineString lineString = (KmlLineString) placemark.getGeometry();
                        endlatlong = new LatLng(lineString.getGeometryObject().get(0).latitude, lineString.getGeometryObject().get(0).longitude);
                        endMarker = getMap().addMarker(new MarkerOptions().position(endlatlong).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).title("End Point"));
                    }
                    else if(String.valueOf(geometry).contains("Point") && placemark.getProperty("name").equals("End Point")){
                        KmlPoint point = (KmlPoint) placemark.getGeometry();
                        endlatlong = new LatLng(point.getGeometryObject().latitude, point.getGeometryObject().longitude);
                        endMarker = getMap().addMarker(new MarkerOptions().position(endlatlong).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).title("End Point"));
                    }
                    else if(String.valueOf(geometry).contains("Polygon") && placemark.getProperty("name").equals("End Point")){
                        KmlPolygon polygon = (KmlPolygon) placemark.getGeometry();
                        endlatlong = new LatLng(polygon.getOuterBoundaryCoordinates().get(0).latitude,polygon.getOuterBoundaryCoordinates().get(0).longitude);
                        endMarker = getMap().addMarker(new MarkerOptions().position(endlatlong).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).title("End Point"));
                    }
                }
            }
            if (container.hasContainers()) {
                findDeepestUtility(container.getContainers());
            }
        }
    }

    private void findNearestStatic(Iterable<KmlContainer> containers) {
        for (KmlContainer container : containers) {
            for (KmlPlacemark placemark : container.getPlacemarks()) {
                Geometry geometry = placemark.getGeometry();
                if (startlatlong != null) {
                    if (String.valueOf(geometry).contains("Point") && !String.valueOf(placemark.getProperty("name")).contains("Start Point") && placemark.getProperty("description").toLowerCase().contains("landmark")) {
                        KmlPoint point = (KmlPoint) geometry;
                        LatLng testLatLong = new LatLng(point.getGeometryObject().latitude, point.getGeometryObject().longitude);
                        if (distance(testLatLong) < minDist) {
                            minDist = distance(testLatLong);
                            nearestStatic = testLatLong;
                            nearestDesc = placemark.getProperty("description");
                            nearestName = placemark.getProperty("name");
                        }
                    }
                    if (String.valueOf(geometry).contains("Polygon") && !String.valueOf(placemark.getProperty("name")).contains("Start Point") && placemark.getProperty("description").toLowerCase().contains("landmark")) {
                        KmlPolygon polygon = (KmlPolygon) geometry;
                        for(LatLng testLatLong : polygon.getOuterBoundaryCoordinates()) {
                            Log.i("Worked",String.valueOf(testLatLong));
                            if (distance(testLatLong) < minDist) {
                                minDist = distance(testLatLong);
                                nearestStatic = testLatLong;
                                nearestDesc = placemark.getProperty("description");
                                nearestName = placemark.getProperty("name");
                            }
                        }
                    }
                    if (String.valueOf(geometry).contains("LineString") && !String.valueOf(placemark.getProperty("name")).contains("Start Point") && placemark.getProperty("description").toLowerCase().contains("landmark")) {
                        KmlLineString lineString = (KmlLineString) geometry;
                        LatLng testLatLong = new LatLng(lineString.getGeometryObject().get(0).latitude,lineString.getGeometryObject().get(0).longitude);
                        if (distance(testLatLong) < minDist) {
                            minDist = distance(testLatLong);
                            nearestStatic = testLatLong;
                            nearestDesc = placemark.getProperty("description");
                            nearestName = placemark.getProperty("name");
                        }
                    }
                }
            }

            if (container.hasContainers()) {
                findNearestStatic(container.getContainers());
            }
        }
    }

    private void findNearestStaticEnd(Iterable<KmlContainer> containers){
        for (KmlContainer container : containers) {
            for (KmlPlacemark placemark : container.getPlacemarks()) {
                Geometry geometry = placemark.getGeometry();
                if (endlatlong != null) {
                    if (String.valueOf(geometry).contains("Point") && !String.valueOf(placemark.getProperty("name")).contains("End Point") && placemark.getProperty("description").toLowerCase().contains("landmark")) {
                        KmlPoint point = (KmlPoint) geometry;
                        LatLng testLatLong = new LatLng(point.getGeometryObject().latitude, point.getGeometryObject().longitude);
                        if (distanceEnd(testLatLong) < minDistEnd) {
                            minDistEnd = distanceEnd(testLatLong);
                            nearestStaticEnd = testLatLong;
                            nearestDescEnd = placemark.getProperty("description");
                            nearestNameEnd = placemark.getProperty("name");
                        }
                    }
                    if (String.valueOf(geometry).contains("Polygon") && !String.valueOf(placemark.getProperty("name")).contains("End Point") && placemark.getProperty("description").toLowerCase().contains("landmark")) {
                        KmlPolygon polygon = (KmlPolygon) geometry;
                        for(LatLng testLatLong : polygon.getOuterBoundaryCoordinates()) {
                            if (distanceEnd(testLatLong) < minDistEnd) {
                                minDistEnd = distanceEnd(testLatLong);
                                nearestStaticEnd = testLatLong;
                                nearestDescEnd = placemark.getProperty("description");
                                nearestNameEnd = placemark.getProperty("name");
                            }
                        }
                    }
                    if (String.valueOf(geometry).contains("LineString") && !String.valueOf(placemark.getProperty("name")).contains("End Point") && placemark.getProperty("description").toLowerCase().contains("landmark")) {
                        KmlLineString lineString = (KmlLineString) geometry;
                        LatLng testLatLong = new LatLng(lineString.getGeometryObject().get(0).latitude,lineString.getGeometryObject().get(0).longitude);
                        if (distanceEnd(testLatLong) < minDistEnd) {
                            minDistEnd = distanceEnd(testLatLong);
                            nearestStaticEnd = testLatLong;
                            nearestDescEnd = placemark.getProperty("description");
                            nearestNameEnd = placemark.getProperty("name");
                        }
                    }
                }
            }

            if (container.hasContainers()) {
                findNearestStaticEnd(container.getContainers());
            }
        }
    }


    private Renderer.ImagesCache getImagesCache() {
        final RetainFragment retainFragment =
                RetainFragment.findOrCreateRetainFragment(getSupportFragmentManager());
        return retainFragment.mImagesCache;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }


    /**
     * Fragment for retaining the bitmap cache between configuration changes.
     */
    public static class RetainFragment extends Fragment {
        private static final String TAG = RetainFragment.class.getName();
        Renderer.ImagesCache mImagesCache;

        static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
            RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(TAG);
            if (fragment == null) {
                fragment = new RetainFragment();
                fm.beginTransaction().add(fragment, TAG).commit();
            }
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }


    private class DownloadKmlFile extends AsyncTask<String, Void, KmlLayer> {

        DownloadKmlFile(String url) {
            uri = Uri.parse(url);
        }

        protected KmlLayer doInBackground(String... params) {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                try {
                    return new KmlLayer(mMap,
                            new ByteArrayInputStream(buffer.toByteArray()),
                            KmlActivity.this,
                            new MarkerManager(mMap),
                            new PolygonManager(mMap),
                            new PolylineManager(mMap),
                            new GroundOverlayManager(mMap),
                            getImagesCache());

                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(KmlLayer kmlLayer) {
            addKmlToMap(kmlLayer);
        }
    }

    private void addKmlToMap(KmlLayer kmlLayer) {
        if (kmlLayer != null) {
            loading.setVisibility(View.VISIBLE);
            kmlLayer.addLayerToMap();
            loading.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
            calculate(kmlLayer);
        }
    }

}

