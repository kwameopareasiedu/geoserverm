package com.digitalanalogy.geoserverm;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.digitalanalogy.geoserverm.util.InputStreamRequest;
import com.digitalanalogy.geoserverm.util.ResponseProcessor;

import org.json.JSONException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements Response.Listener<byte[]>, Response.ErrorListener {
    ViewGroup contentLayout;

    TextView noDataText;

    TextView serviceNameText;
    TextView serviceTitleText;
    TextView serviceAbstractText;

    TextView capabilitiesText;
    TextView mapFormatsText;
    TextView exceptionsText;
    TextView layerCountText;

    Toolbar toolbarMain;
    ProgressDialog progress;

    public void viewLayers(View v) {
        startActivity(new Intent(this, LayerListActivity.class));
    }

    public void showConnectionDialogue(View v) {
        // Create input field and show it to user
        final EditText addressInput = new EditText(this);
        addressInput.setText(R.string.main_server_input_prefix_tag);

        // Assign the custom background
        addressInput.setBackground(ContextCompat.getDrawable(this, R.drawable.drawable_input));

        // Get the final view
        LinearLayout container = createContainer(addressInput);

        // Create a popup menu to show to user
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(R.string.alert_address_input_title)
                .setView(container)
                .setNegativeButton(R.string.alert_cancel, null)
                .setPositiveButton(R.string.alert_connect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String address = String.valueOf(addressInput.getText());

                        if (address.isEmpty()) {
                            Toast.makeText(MainActivity.this, R.string.alert_empty_address, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        saveAddress(address);
                        connectToHost();
                    }
                }).create();

        alert.show();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
        Toast.makeText(this, R.string.alert_network_error, Toast.LENGTH_LONG).show();
        progress.dismiss();
    }

    @Override
    public void onResponse(byte[] response) {
        try {
            // Open stream to write to file
            String outputFileName = getString(R.string.download_path_key);

            FileOutputStream outputStream = openFileOutput(outputFileName, MODE_PRIVATE);

            outputStream.write(response);

            outputStream.close();

            Thread t = createProcessorThread();

            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                startActivity(new Intent(this, HelpActivity.class));
                break;
        }
        return true;
    }

    private void initialize() {
        // Get components from the associated view
        contentLayout = (ViewGroup) findViewById(R.id.main_layout_content);
        noDataText = (TextView) findViewById(R.id.main_txt_nodata);

        serviceNameText = (TextView) findViewById(R.id.main_txt_service_name);
        serviceTitleText = (TextView) findViewById(R.id.main_txt_service_title);
        serviceAbstractText = (TextView) findViewById(R.id.main_txt_service_abstract);

        capabilitiesText = (TextView) findViewById(R.id.main_txt_capabilities);
        mapFormatsText = (TextView) findViewById(R.id.main_txt_map_formats);
        exceptionsText = (TextView) findViewById(R.id.main_txt_exception_formats);
        layerCountText = (TextView) findViewById(R.id.main_txt_layer_count);

        toolbarMain = (Toolbar) findViewById(R.id.toolbar_main);

        toolbarMain.setTitle(R.string.app_name);
        toolbarMain.setTitleTextColor(Color.WHITE);

        // Initialize progress dialogue view
        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setTitle(R.string.progress_title);
        progress.setMessage(getString(R.string.progress_message));

        // Assign custom toolbar as app toolbar
        setSupportActionBar(toolbarMain);

        // Initialize the no data to sho initially
        toggleVisibility(0);
    }

    private LinearLayout createContainer(EditText input) {
        // Initialize the  container view
        LinearLayout container = new LinearLayout(this);
        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        container.setPadding(32, 8, 32, 8);

        // Set the input field to match parent
        input.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 72));

        container.addView(input);

        return container;
    }

    private void toggleVisibility(int index) {
        // Toggles visibility between 'data-less' state and 'data-ful' states
        // 0    -   show no data (Hide content)
        // 1    -   show content (Hide no data)
        noDataText.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
    }

    private void saveAddress(String address) {
        // Remove any '/' at the end of the address name
        if (address.endsWith("/")) {
            address = address.substring(0, address.length() - 1);
        }

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_key), MODE_PRIVATE);
        preferences.edit().putString(getString(R.string.prefs_address_key), address).apply();
    }

    private void connectToHost() {
        // Get address from preferences
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_key), MODE_PRIVATE);
        String address = preferences.getString(getString(R.string.prefs_address_key), "NONE");

        if (address.equals("NONE")) {
            Toast.makeText(this, R.string.alert_empty_address_verify, Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare request and send to server
        address = String.format(Locale.getDefault(), "%s/wms?service=wms&version=1.1.1&request=GetCapabilities", address);
        InputStreamRequest request = new InputStreamRequest(address, this, this);
        RequestQueue queue = Volley.newRequestQueue(this, new HurlStack());

        queue.add(request);
        progress.show();
    }

    private void onParseComplete(boolean status) {
        progress.dismiss();

        if (status) {
            toggleVisibility(1);
            Toast.makeText(this, R.string.alert_download_success, Toast.LENGTH_SHORT).show();
        } else {
            toggleVisibility(0);
            Toast.makeText(this, R.string.alert_download_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private Thread createProcessorThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                // Update views
                try {
                    ResponseProcessor.getInstance().bindDataToViews(MainActivity.this, serviceNameText, serviceTitleText, serviceAbstractText, capabilitiesText, mapFormatsText, exceptionsText, layerCountText);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onParseComplete(true);
                        }
                    });
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onParseComplete(false);
                        }
                    });
                }
            }
        });
    }
}
