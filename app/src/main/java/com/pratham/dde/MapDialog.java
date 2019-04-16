package com.pratham.dde;

import android.app.DialogFragment;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pratham.dde.activities.DisplayQuestions;

public class MapDialog extends DialogFragment implements OnMapReadyCallback {
    Double lat, longtude;
    private static View view;
    DisplayQuestions myContext;

    public static MapDialog newInstance(Double lat, Double longtude) {
        MapDialog f = new MapDialog();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putDouble("lat", lat);
        args.putDouble("longtude", longtude);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        myContext = (DisplayQuestions) context;
        super.onAttach(context);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lat = getArguments().getDouble("lat");
        longtude = getArguments().getDouble("longtude");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        /* return inflater.inflate(R.layout.map_view, container, false);*/
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.map_view, container, false);
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        myContext = (DisplayQuestions) getActivity();
        SupportMapFragment mapFragment = (SupportMapFragment) myContext.getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng india = new LatLng(lat, longtude);
        googleMap.addMarker(new MarkerOptions().position(india)
                .title("Marker in India"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(india));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(india).zoom(14.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.moveCamera(cameraUpdate);
    }

}
