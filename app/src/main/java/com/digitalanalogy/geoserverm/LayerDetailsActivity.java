package com.digitalanalogy.geoserverm;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.digitalanalogy.geoserverm.model.Layer;
import com.digitalanalogy.geoserverm.util.ResponseProcessor;

import org.json.JSONException;

import java.io.IOException;
import java.util.Locale;

public class LayerDetailsActivity extends AppCompatActivity {

    TextView nameText;
    TextView titleText;
    TextView abstractText;
    TextView srsText;
    TextView bboxMinX;
    TextView bboxMaxX;
    TextView bboxMinY;
    TextView bboxMaxY;
    ImageView mapView;

    Toolbar toolbarMain;
    Layer currentLayer;
    ProgressDialog progress;

    public void showRequestDialogue(View v) {
        // Allow the user to set quality
        final Spinner qualitySelector = new Spinner(this, Spinner.MODE_DROPDOWN);
        qualitySelector.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 72));
        // Assign the custom background
        qualitySelector.setBackground(ContextCompat.getDrawable(this, R.drawable.drawable_input));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.layer_details_quality_values));
        qualitySelector.setAdapter(adapter);

        // Create the container view
        LinearLayout container = new LinearLayout(this);
        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        container.setPadding(32, 8, 32, 8);

        container.addView(qualitySelector);

        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(R.string.alert_quality_header)
                .setView(container)
                .setNegativeButton(R.string.alert_cancel, null)
                .setPositiveButton(R.string.alert_request, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int qualityLevel = 256;

                        switch (qualitySelector.getSelectedItemPosition()) {
                            case 0:
                                qualityLevel = 256;
                                break;
                            case 1:
                                qualityLevel = 512;
                                break;
                            case 2:
                                qualityLevel = 1024;
                                break;
                            case 3:
                                qualityLevel = 2048;
                                break;
                        }

                        requestMap(qualityLevel);
                    }
                }).create();

        alert.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layer_details);

        initialize();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }

        return true;
    }

    private void initialize() {
        toolbarMain = (Toolbar) findViewById(R.id.toolbar_main);
        nameText = (TextView) findViewById(R.id.details_txt_layer_name);
        titleText = (TextView) findViewById(R.id.details_txt_layer_title);
        abstractText = (TextView) findViewById(R.id.details_txt_layer_abstract);
        srsText = (TextView) findViewById(R.id.details_txt_layer_srs);
        bboxMinX = (TextView) findViewById(R.id.details_txt_bbox_min_x);
        bboxMinY = (TextView) findViewById(R.id.details_txt_bbox_min_y);
        bboxMaxX = (TextView) findViewById(R.id.details_txt_bbox_max_x);
        bboxMaxY = (TextView) findViewById(R.id.details_txt_bbox_max_y);
        mapView = (ImageView) findViewById(R.id.details_img_map);

        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setMessage(getString(R.string.layer_details_process_pending_tag));
        progress.show();

        // Initialize a progress dialogue to show while the process completes

        readDataFromFile();

        toolbarMain.setTitleTextColor(Color.WHITE);

        setSupportActionBar(toolbarMain);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void readDataFromFile() {
        final String targetFileName = getSharedPreferences(getString(R.string.preferences_key), MODE_PRIVATE).getString(getString(R.string.target_layer_file_key), "NONE");

        if (targetFileName.equals("NONE")) {
            AlertDialog alert = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(getString(R.string.alert_file_not_found))
                    .setNegativeButton(R.string.alert_close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LayerDetailsActivity.this.finish();
                        }
                    }).create();
            alert.show();
            return;
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    currentLayer = ResponseProcessor.getInstance().extractDetailsFromFile(LayerDetailsActivity.this, targetFileName);
                    LayerDetailsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                            onProcessCompleted();
                        }
                    });
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    LayerDetailsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                            onProcessCompleted();

                            AlertDialog alert = new AlertDialog.Builder(LayerDetailsActivity.this)
                                    .setCancelable(false)
                                    .setMessage(getString(R.string.alert_file_not_found))
                                    .setNegativeButton(R.string.alert_close, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            LayerDetailsActivity.this.finish();
                                        }
                                    }).create();
                            alert.show();
                        }
                    });
                }
            }
        });

        t.start();

        if (targetFileName.equals("NONE")) {
            AlertDialog alert = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(getString(R.string.alert_file_not_found))
                    .setNegativeButton(R.string.alert_close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LayerDetailsActivity.this.finish();
                        }
                    }).create();
            alert.show();
        }
    }

    private void onProcessCompleted() {
        if (currentLayer != null) {
            toolbarMain.setTitle(currentLayer.getTitle());
            nameText.setText(currentLayer.getName());
            titleText.setText(currentLayer.getTitle());
            abstractText.setText(currentLayer.get_abstract());
            srsText.setText(currentLayer.getSRS());
            bboxMinX.setText(String.valueOf(currentLayer.getMinXBound()));
            bboxMinY.setText(String.valueOf(currentLayer.getMinYBound()));
            bboxMaxX.setText(String.valueOf(currentLayer.getMaxXBound()));
            bboxMaxY.setText(String.valueOf(currentLayer.getMaxYBound()));
        }
    }

    private void requestMap(int quality) {
        // Get the host address
        String hostAddress = getSharedPreferences(getString(R.string.preferences_key), MODE_PRIVATE).getString(getString(R.string.prefs_address_key), "NONE");

        if (hostAddress.equals("NONE")) {
            Toast.makeText(this, R.string.layer_details_map_request_failed_tag, Toast.LENGTH_LONG).show();
            return;
        }
        // Build request
        //  String example = "http://localhost:8090/geoserver/cite/wms?service=WMS&version=1.1.0&request=GetMap&layers=cite:ghana_administrative&styles=&bbox=-3.3217031955719,4.67666530609131,1.22211825847626,11.275990486145&width=528&height=768&srs=EPSG:404000&format=image%2Fpng";
        String requestURL = String.format(Locale.getDefault(), "%s/wms?request=GetMap&service=WMS&layers=%s&styles=&bbox=%.12f,%.12f,%.12f,%.12f&width=%d&height=%d&srs=%s&format=image%%2Fpng", hostAddress, currentLayer.getName(), currentLayer.getMinXBound(), currentLayer.getMinYBound(), currentLayer.getMaxXBound(), currentLayer.getMaxYBound(), quality, quality, currentLayer.getSRS());
        Log.d("Details", requestURL);

        Glide.with(this)
                .load(requestURL)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Toast.makeText(LayerDetailsActivity.this, R.string.alert_network_error, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Log.d("Details", "Image ready");
                        return false;
                    }
                })
                .into(mapView);
    }
}
