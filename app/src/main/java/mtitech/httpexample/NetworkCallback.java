package mtitech.httpexample;

import org.json.JSONObject;

public interface NetworkCallback
{
    void onSuccess(JSONObject res);

    void onFailure(String errorMsg);
}
