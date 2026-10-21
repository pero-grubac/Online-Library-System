package org.unibl.etf.mdp.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP client for the LibraryServer API. After {@link #login} every request carries the session token,
 * and an expired token is renewed once, transparently, with the same credentials.
 */
public class RestClient {
    public static final String JSON = "application/json";
    private static final RestClient shared = new RestClient(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());

    private final HttpClient http;
    private volatile String token;
    private String loginUrl;
    private String username;
    private String password;

    public RestClient(HttpClient http) {
        this.http = http;
    }

    /** One client per application, so the whole app shares the session. */
    public static RestClient shared() {
        return shared;
    }

    public static final class Result {
        private final int status;
        private final String body;

        Result(int status, String body) {
            this.status = status;
            this.body = body;
        }

        public int status() {
            return status;
        }

        public String body() {
            return body;
        }

        public boolean ok() {
            return status >= 200 && status < 300;
        }
    }

    public synchronized Result login(String loginUrl, String username, String password) throws IOException {
        JsonObject credentials = new JsonObject();
        credentials.addProperty("username", username);
        credentials.addProperty("password", password);
        Result result = exchange("POST", loginUrl, JSON, credentials.toString(), null);
        if (result.ok()) {
            token = JsonParser.parseString(result.body()).getAsJsonObject().get("token").getAsString();
            this.loginUrl = loginUrl;
            this.username = username;
            this.password = password;
        }
        return result;
    }

    public synchronized void logout() {
        token = null;
        loginUrl = username = password = null;
    }

    public boolean isLoggedIn() {
        return token != null;
    }

    public Result get(String url) throws IOException {
        return send("GET", url, null, null);
    }

    public Result delete(String url) throws IOException {
        return send("DELETE", url, null, null);
    }

    public Result post(String url, String json) throws IOException {
        return send("POST", url, JSON, json);
    }

    public Result put(String url, String json) throws IOException {
        return send("PUT", url, JSON, json);
    }

    public Result patch(String url, String contentType, String body) throws IOException {
        return send("PATCH", url, contentType, body);
    }

    private Result send(String method, String url, String contentType, String body) throws IOException {
        Result result = exchange(method, url, contentType, body, token);
        if (result.status() == 401 && renewSession()) {
            result = exchange(method, url, contentType, body, token);
        }
        return result;
    }

    private synchronized boolean renewSession() throws IOException {
        if (loginUrl == null) {
            return false;
        }
        return login(loginUrl, username, password).ok();
    }

    private Result exchange(String method, String url, String contentType, String body, String bearer)
            throws IOException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30))
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        if (contentType != null) {
            request.header("Content-Type", contentType + "; charset=UTF-8");
        }
        if (bearer != null) {
            request.header("Authorization", "Bearer " + bearer);
        }
        try {
            HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
            return new Result(response.statusCode(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }
}
