package com.digitalanalogy.geoserverm.util;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.HashMap;
import java.util.Map;

public class InputStreamRequest extends Request<byte[]> {
    private Map<String, String> map;
    private Map<String, String> responseHeaders;
    private Response.Listener<byte[]> listener;

    public InputStreamRequest(String address, Response.Listener<byte[]> listener, Response.ErrorListener errorListener) {
        super(Method.GET, address, errorListener);
        setShouldCache(false);
        this.listener = listener;
        this.map = new HashMap<>();
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }

    @Override
    protected void deliverResponse(byte[] response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        responseHeaders = response.headers;
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }
}
