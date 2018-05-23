package com.iavariav.root.examplegoogledirectionjava;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    TextView tvFrom, tvTo, tvhasil;
    View view;
    MapFragment map;
    Button btnRequestDirection;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    Boolean isFromPlacePicker = false;
    String TAG = "tag";
    private LatLng latLngFrom;
    private LatLng latLngTo;
    private String[] colors = {"#FF3F51B5"};

    private GoogleMap googleMap;
    private String serverKey = "AIzaSyDK9afBrpN0wHnA5T_O_opQsbhui-PYF_c";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvFrom  = findViewById(R.id.fromText);
        tvTo    = findViewById(R.id.toText);
        tvhasil = findViewById(R.id.distanceText);
        btnRequestDirection = findViewById(R.id.btn_request_direction);


        tvFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFromPlacePicker = true;
                loadPlaceAutoIntent();
                tvhasil.setVisibility(View.GONE);
                btnRequestDirection.setVisibility(View.VISIBLE);
            }
        });

        tvTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFromPlacePicker = false;
                loadPlaceAutoIntent();
                tvhasil.setVisibility(View.GONE);
                btnRequestDirection.setVisibility(View.VISIBLE);
            }
        });

        btnRequestDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDirection();
            }
        });

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public void loadPlaceAutoIntent(){
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());
                if (isFromPlacePicker) {
                    tvFrom.setText(place.getName());
                    latLngFrom = place.getLatLng();
                    Log.d(TAG, "resultttttt: "+latLngFrom);
                } else {
                    tvTo.setText(place.getName());
                    latLngTo = place.getLatLng();
                    Log.d(TAG, "longitude1111: "+latLngTo);
                }


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public void requestDirection() {
//        Snackbar.make(btnRequestDirection, "Direction Requesting...", Snackbar.LENGTH_SHORT).show();
        GoogleDirection.withServerKey(serverKey)
                .from(latLngFrom)
                .to(latLngTo)
                .transportMode(TransportMode.DRIVING)
                .alternativeRoute(true)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()) {
                            googleMap.clear();
                            googleMap.addMarker(new MarkerOptions().position(latLngFrom));
                            googleMap.addMarker(new MarkerOptions().position(latLngTo));
                            Log.d(TAG, "ooooooooooo: "+latLngTo);

                            for (int i = 0; i < direction.getRouteList().size(); i++) {
                                Route route = direction.getRouteList().get(i);
                                Leg leg = route.getLegList().get(0);
                                String color = colors[i % colors.length];
                                ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
                                googleMap.addPolyline(DirectionConverter.createPolyline(MainActivity.this,
                                        directionPositionList, 10, Color.parseColor(color)));

                                setCameraWithCoordinationBounds(route);

                                btnRequestDirection.setVisibility(View.GONE);
                                tvhasil.setVisibility(View.VISIBLE);
                                tvhasil.setText(String.format("distance = %s , duration = %s"
                                        ,leg.getDistance().getText() , leg.getDuration().getText()));
                            }


                        } else {
//                            Snackbar.make(btnRequestDirection, direction.getStatus(), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {

                    }
                });
        Log.d(TAG, "requestDirectionnnnn: "+latLngTo);
    }

    private void setCameraWithCoordinationBounds(Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }
}
