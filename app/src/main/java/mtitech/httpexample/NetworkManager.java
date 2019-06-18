package mtitech.httpexample;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class NetworkManager
{
    private static final String BASE_URL = "https://iotdev.igates.com/";
    private static final String TAG = NetworkManager.class.getName();

    public static void downloadFile(final NetworkCallback callback)
    {
        getRequest("api/Catalog", new NetworkCallback()
        {
            @Override
            public void onSuccess(JSONObject res)
            {
                Log.e(TAG, res.toString());
                if(callback != null)
                {
                    callback.onSuccess(res);
                }
            }

            @Override
            public void onFailure(String errorMsg)
            {
                Log.e(TAG, errorMsg);
                if(callback != null)
                {
                    callback.onFailure(errorMsg);
                }            }
        });
    }

    private static HttpsURLConnection httpsGetUrlConnection(String action)
    {
        try
        {
            StringBuilder stringBuilder = new StringBuilder(BASE_URL + action);
            URL url = new URL(stringBuilder.toString());
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.connect();

            return connection;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }


    private static void getRequest(final String action, final NetworkCallback callback)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    HttpsURLConnection connection = httpsGetUrlConnection(action);
                    StringBuilder sb = new StringBuilder();
                    int responseCode = getResponseString(connection, sb);
                    if(responseCode >= HttpURLConnection.HTTP_OK
                            && // all good responses
                            responseCode < HttpURLConnection.HTTP_MULT_CHOICE)
                    {
                        String responseString = sb.toString();

                        if (responseString != null && responseString.length() > 0)
                        {
                            JSONObject responseObj = new JSONObject(responseString);
                            callback.onSuccess(responseObj);
                        } else
                            callback.onSuccess(new JSONObject());
                    }
                    else //errpr response
                    {
                        callback.onFailure(sb.toString());
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    callback.onFailure(e.getMessage());
                }
            }
        }).start();
    }


    private static int getResponseString(HttpURLConnection connection, StringBuilder result)
    {
        InputStream in = null;
        int status = 0;
        try
        {
            status = connection.getResponseCode();
            if (status < 400)
            {
                in = new BufferedInputStream(connection.getInputStream());
            } else
            {
                in = new BufferedInputStream(connection.getErrorStream());
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null)
            {
                if(line.startsWith("\"") && line.endsWith("\""))
                {
                    line = line.substring(1, line.length()-1);
                }
                result.append(line + "\n");
            }
        }
        catch (Exception e)
        {
            if(result.length() == 0)
            {
                result.append("Server unreachable check your internet access\n");
                result.append(e.getMessage());
            }
            e.printStackTrace();
            Log.e("NetworkManager", e.getMessage());
        }
        return status;
    }
}