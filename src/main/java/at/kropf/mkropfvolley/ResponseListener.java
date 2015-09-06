package at.kropf.mkropfvolley;

import org.json.JSONObject;

/**
 * Interface for success and error methods
 */
public interface ResponseListener {
    void onComplete(JSONObject json);

    void onError(Throwable error);
}
