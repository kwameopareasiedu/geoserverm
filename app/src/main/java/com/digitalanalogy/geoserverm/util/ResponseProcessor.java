package com.digitalanalogy.geoserverm.util;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.digitalanalogy.geoserverm.R;
import com.digitalanalogy.geoserverm.model.Layer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;

public class ResponseProcessor {
    private static final ResponseProcessor instance = new ResponseProcessor();
    private static final ArrayList<String> layers = new ArrayList<>();

    private ArrayList<String> capabilities;
    private ArrayList<String> mapFormats;
    private ArrayList<String> exceptionFormats;

    private ResponseProcessor() {
        capabilities = new ArrayList<>();
        mapFormats = new ArrayList<>();
        exceptionFormats = new ArrayList<>();
    }

    public static ResponseProcessor getInstance() {
        return instance;
    }

    static ArrayList<String> getLayers() {
        return layers;
    }

    public void bindDataToViews(Context context, TextView serviceNameText, TextView serviceTitleText, TextView serviceAbstractText,
                                TextView capabilitiesText, TextView mapFormatsText, TextView exceptionsText, TextView layerCountText) throws IOException, JSONException {

        capabilities.clear();
        mapFormats.clear();
        exceptionFormats.clear();
        layers.clear();

        StringBuilder content = new StringBuilder();
        FileInputStream inputStream = context.openFileInput(context.getString(R.string.download_path_key));
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String buffer;
        reader.readLine();
        while ((buffer = reader.readLine()) != null) {
            content.append(buffer);
        }

        XmlToJson xmlToJson = new XmlToJson.Builder(content.toString()).build();

        JSONObject root = xmlToJson.toJson();


        if (root != null) {
            Log.d("Processor", root.toString());

            JSONObject docRoot = root.getJSONObject("WMT_MS_Capabilities");

            if (docRoot != null) {
                // Get Service Details
                JSONObject service = docRoot.getJSONObject("Service");
                String serviceName = service.getString("Name");
                String serviceTitle = service.getString("Title");
                String serviceAbstract = service.getString("Abstract");

                serviceNameText.setText(serviceName);
                serviceTitleText.setText(serviceTitle);
                serviceAbstractText.setText(serviceAbstract);

                // Get Web Map Features
                JSONObject capability = docRoot.getJSONObject("Capability");
                JSONObject request = capability.getJSONObject("Request");

                if (request.has("GetCapabilities")) {
                    capabilities.add("GetCapabilities");
                }

                if (request.has("GetMap")) {
                    capabilities.add("GetMap");
                }

                if (request.has("GetFeatureInfo")) {
                    capabilities.add("GetFeatureInfo");
                }

                if (request.has("DescribeLayer")) {
                    capabilities.add("DescribeLayer");
                }

                if (request.has("GetLegendGraphic")) {
                    capabilities.add("GetLegendGraphic");
                }

                if (request.has("GetStyles")) {
                    capabilities.add("GetStyles");
                }

                String capBufferText = "";
                for (String cap : capabilities) {
                    capBufferText += cap + "\n";
                }

                capabilitiesText.setText(capBufferText);

                // Get Map Formats
                JSONObject mapFormats = request.getJSONObject("GetMap");

                if (mapFormats != null) {
                    JSONArray mapFormatArray = mapFormats.getJSONArray("Format");

                    for (int i = 0; i < mapFormatArray.length(); i++) {
                        JSONObject obj = mapFormatArray.getJSONObject(i);
                        this.mapFormats.add(obj.getString("content"));
                    }

                    String mapBufferText = "";
                    for (String map : this.mapFormats) {
                        mapBufferText += map + "\n";
                    }

                    mapFormatsText.setText(mapBufferText);
                }

                JSONObject exception = capability.getJSONObject("Exception");
                if (exception != null) {
                    JSONArray exceptionFormatArray = exception.getJSONArray("Format");

                    for (int i = 0; i < exceptionFormatArray.length(); i++) {
                        JSONObject obj = exceptionFormatArray.getJSONObject(i);
                        this.exceptionFormats.add(obj.getString("content"));
                    }

                    String exceptBufferText = "";
                    for (String map : this.exceptionFormats) {
                        exceptBufferText += map + "\n";
                    }

                    exceptionsText.setText(exceptBufferText);
                }

                /* Get Layers
                 For layers write each to a file and decompress on demand (To prevent potential OutOfMemoryError)*/
                JSONObject layerRoot = capability.getJSONObject("Layer");

                if (layerRoot != null) {
                    JSONArray layerArray = layerRoot.getJSONArray("Layer");

                    for (int i = 0; i < layerArray.length(); i++) {
                        JSONObject layerJSON = layerArray.getJSONObject(i);

                        if (layerJSON != null) {
                            // This would obviously overwrite any files that exist with the same name
                            String outputFileName = String.format(Locale.getDefault(), "layer-%d.txt", i);

                            File file = new File(context.getExternalFilesDir(null), outputFileName);

                            // The files are written to a public path and can be accessed
                            FileOutputStream outputStream = new FileOutputStream(file);
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

                            writer.write(layerJSON.toString());

                            writer.close();
                            outputStream.close();
                            layers.add(layerJSON.getString("Title"));
                        }

                    }

                    layerCountText.setText(String.valueOf(layerArray.length()));
                }
            }
        }

        reader.close();
    }

    public Layer extractDetailsFromFile(Context context, String targetFileName) throws IOException, JSONException {
        StringBuilder fileContent = new StringBuilder();
        FileInputStream inputStream = context.openFileInput(targetFileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String buffer;
        while ((buffer = reader.readLine()) != null) {
            fileContent.append(buffer);
        }

        reader.close();
        inputStream.close();

        JSONObject root = new JSONObject(fileContent.toString());
        JSONObject bbox = root.getJSONObject("BoundingBox");

        String name = root.getString("Name");
        String title = root.getString("Title");

        // Not all layers (most especially user-defined layers) have an abstract, thus we need to check if it exists
        String _abstract = "";
        if(root.has("Abstract")) {
            _abstract = root.getString("Abstract");
        }

        String srs = root.getString("SRS");
        double minX = bbox.getDouble("minx");
        double maxX = bbox.getDouble("maxx");
        double minY = bbox.getDouble("miny");
        double maxY = bbox.getDouble("maxy");

        return new Layer(name, title, _abstract, srs, minX, minY, maxX, maxY);
    }
}