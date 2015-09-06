package at.kropf.mkropfvolley;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


/**
 * Base class for calling rest services in the backend
 */
public class BaseRequestData {

    public static final String NO_CONNECTION_TITLE = "noConnection";

    public final ResponseListener responseListener;

    private Context context;

    private int timeout;

    private boolean enableDebug = false;

    public boolean isEnableDebug() {
        return enableDebug;
    }

    public void setEnableDebug(boolean enableDebug) {
        this.enableDebug = enableDebug;
    }

    public BaseRequestData(Context context, ResponseListener responseListener){
        this.context = context;
        this.responseListener = responseListener;
        if (!isOnline()) {
            responseListener.onError(new Throwable(NO_CONNECTION_TITLE));
        }

        this.timeout = 4000;
    }



    public BaseRequestData(Context context, ResponseListener listener, int timeout) {
        this.responseListener = listener;
        this.context = context;
        if (!isOnline()) {
            responseListener.onError(new Throwable(NO_CONNECTION_TITLE));
        }

        this.timeout = timeout;
    }

    /*
    *   POST
    *   post json data to backend
     */
    public void doPost(String url, JSONObject jsonObj) {
        JsonUTF8Request jsonObjectRequest = new JsonUTF8Request(Request.Method.POST,
                url,
                jsonObj,
                createMyReqSuccessListener(),
                createMyReqErrorListener());
        RetryPolicy policy = new DefaultRetryPolicy(timeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        if(enableDebug)
            Log.d("REQUEST", jsonObj.toString());

        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /*
    *   GET
    *   send get request to backend
    */
    public void doGet(String url) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                myStringSuccessListener(),
                myStringErrorListener()

        );

        RetryPolicy policy = new DefaultRetryPolicy(timeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);

        if(enableDebug)
            Log.d("REQUEST", "SENT REQUEST:" + url);

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);

    }

    /*
    *   PUT
    *   send put request to backend
    */
    public void doPut(String url, final JSONObject jsonObject) {
        StringRequest putRequest = new StringRequest(
                Request.Method.PUT,
                url,
                myStringSuccessListener(),
                myStringErrorListener()
        ) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() {

                try {
                    return jsonObject.toString().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy(timeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        putRequest.setRetryPolicy(policy);

        if(enableDebug)
            Log.d("REQUEST", jsonObject.toString());

        VolleySingleton.getInstance(context).addToRequestQueue(putRequest);

    }

    /*
    *   DELETE
    *   send delete request to backend
    */
    public void doDelete(String url, final JSONObject jsonObj) {
        StringRequest deleteRequest = new StringRequest(
                Request.Method.DELETE,
                url,
                myStringSuccessListener(),
                myStringErrorListener()
        ) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() {

                try {
                    return jsonObj.toString().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy(timeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        deleteRequest.setRetryPolicy(policy);
        if(enableDebug)
            Log.d("REQUEST", "SENT REQUEST:" + url);

        VolleySingleton.getInstance(context).addToRequestQueue(deleteRequest);
    }

    /*
     * String error-response listener
     */
    public Response.ErrorListener myStringErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if(enableDebug)
                        Log.e("REQUESTERR", "response: " + error);
                    responseListener.onError(error);
                } catch (Exception e) {
                    e.printStackTrace();
                    if(enableDebug)
                        Log.e("REQUESTERR", "error: " + e);
                }
            }
        };
    }

    /*
     * String success-response listener
     */
    public Response.Listener myStringSuccessListener() {
        return new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                try {
                    JSONObject responseJson = new JSONObject(((String) response));
                    if(enableDebug)
                        Log.d("REQUEST", responseJson.toString());
                    responseListener.onComplete(responseJson);


                } catch (Exception e) {
                    e.printStackTrace();
                    if(enableDebug)
                        Log.e("REQUESTERR", "error: " + e);
                }

            }
        };
    }

    /*
     * Error-response listener
     */
    public Response.ErrorListener createMyReqErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if(enableDebug)
                        Log.e("REQUESTERR", "response: " + error);

                    responseListener.onError(error);

                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    if(enableDebug)
                        Log.e("REQUESTERR", "error: " + e);
                }
            }
        };
    }

    /*
     * response listener
     */
    public Response.Listener<JSONObject> createMyReqSuccessListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(enableDebug)
                        Log.d("REQUEST", response.toString());
                    responseListener.onComplete(response);

                } catch (Exception e) {
                    try {
                        responseListener.onError(e);
                    } catch (Exception e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        if(enableDebug)
                            Log.e("REQUESTERR", "error: " + e);
                    }
                }

            }
        };
    }
}