package com.kenny.openimgur.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

/**
 * Created by kcampagna on 6/14/14.
 */
public class ApiClient {

    public enum HttpRequest {
        GET,
        POST,
        DELETE;
    }

    private static final String TAG = ApiClient.class.getSimpleName();

    public static final int STATUS_OK = 200;

    public static final int STATUS_INVALID_PARAM = 400;

    public static final int STATUS_INVALID_PERMISSIONS = 401;

    public static final int STATUS_FORBIDDEN = 403;

    public static final int STATUS_NOT_FOUND = 404;

    public static final int STATUS_RATING_LIMIT = 429;

    public static final int STATUS_INTERNAL_ERROR = 500;

    public static final int STATUS_OVER_CAPACITY = 503;

    // These are custom error codes not given from the server
    public static final int STATUS_IO_EXCEPTION = 600;

    public static final int STATUS_JSON_EXCEPTION = 700;

    public static final int STATUS_EMPTY_RESPONSE = 800;

    public static final String KEY_SUCCESS = "success";

    public static final String KEY_STATUS = "status";

    public static final String KEY_DATA = "data";

    public static final String CLIENT_ID = "YOUR API KEY";

    public static final String CLIENT_SECRET = "YOUR API KEY";

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String CLIENT_ID_HEADER = "Client-ID " + CLIENT_ID;

    private static final long DEFAULT_TIMEOUT = DateUtils.SECOND_IN_MILLIS * 15;

    private String mUrl;

    private String mBearerToken;

    private OkHttpClient mClient = new OkHttpClient();

    private HttpRequest mRequestType = HttpRequest.GET;

    public ApiClient(String url, HttpRequest requestType) {
        mRequestType = requestType;
        mUrl = url;
        mClient.setConnectTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public ApiClient(String url, HttpRequest requestType, String bearerToken) {
        mRequestType = requestType;
        mUrl = url;
        mBearerToken = "Bearer " + bearerToken;
        mClient.setConnectTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public void setBearerToken(String token) {
        mBearerToken = "Bearer " + token;
    }

    public void setRequestType(HttpRequest requestType) {
        this.mRequestType = requestType;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    /**
     * Executes a GET HTTP Request
     *
     * @return
     * @throws IOException
     * @throws JSONException
     */
    private JSONObject get() throws IOException, JSONException {
        Request request = new Request.Builder()
                .addHeader(AUTHORIZATION_HEADER, mBearerToken != null ? mBearerToken : CLIENT_ID_HEADER)
                .get()
                .url(mUrl)
                .build();

        return makeRequest(request);
    }

    private JSONObject post(@NonNull RequestBody body) throws IOException, JSONException {
        Request request = new Request.Builder()
                .addHeader(AUTHORIZATION_HEADER, mBearerToken != null ? mBearerToken : CLIENT_ID_HEADER)
                .post(body)
                .url(mUrl)
                .build();

        return makeRequest(request);
    }

    /**
     * Makes the request and returns the result
     *
     * @param request
     * @return
     * @throws IOException
     * @throws JSONException
     */
    private JSONObject makeRequest(Request request) throws IOException, JSONException {
        JSONObject json = null;
        Log.v(TAG, "Making request to " + mUrl);
        Response response = mClient.newCall(request).execute();
        if (response.isSuccessful()) {
            Log.v(TAG, "Request Successful with status code " + response.code());
            String serverResponse = response.body().string();
            response.body().close();

            // Sometimes the Api response with an empty string when it is experiencing problems
            if (TextUtils.isEmpty(serverResponse)) {
                json = new JSONObject();
                json.put(KEY_SUCCESS, false);
                json.put(KEY_STATUS, STATUS_EMPTY_RESPONSE);
            } else {
                json = new JSONObject(serverResponse);
            }
        } else {
            Log.w(TAG, "Request Failed with status code " + response.code());
            json = new JSONObject();
            json.put(KEY_SUCCESS, false);
            json.put(KEY_STATUS, response.code());
        }

        return json;
    }

    /**
     * Calls the appropriate method based on the HTTPRequest type
     *
     * @param type       The Type of event
     * @param id         An optional unique id for the EventBus
     * @param postParams Items to be posted. MUST be supplied if RequestType is POST
     * @throws IOException
     * @throws JSONException
     */
    public void doWork(ImgurBusEvent.EventType type, @Nullable String id, @Nullable RequestBody postParams)
            throws IOException, JSONException {
        if (mUrl == null) {
            throw new MalformedURLException("Url is null");
        }

        switch (mRequestType) {
            case POST:
                if (postParams == null) {
                    throw new NullPointerException("Post params can not be null when making a POST call");
                }

                post(postParams);
                break;

            case DELETE:
                break;

            case GET:
            default:
                EventBus.getDefault().post(new ImgurBusEvent(get(), type, HttpRequest.GET, id));
        }

    }
}
