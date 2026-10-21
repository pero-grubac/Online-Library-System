package org.unibl.etf.mdp.library.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.unibl.etf.mdp.common.ApiException;
import org.unibl.etf.mdp.common.RestClient;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.model.StatusEnum;
import org.unibl.etf.mdp.model.User;
import org.unibl.etf.mdp.model.UserDto;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Member accounts on the LibraryServer (users.xml). */
public class UserService {
    private static AppConfig conf = new AppConfig();
    private static final String USERS_URL = conf.getServerUsersUrl();
    private static UserService instance;
    private final RestClient api = RestClient.shared();

    private UserService() {
    }

    public static synchronized UserService getInstance() {
        if (instance == null)
            instance = new UserService();
        return instance;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public List<UserDto> getAll() throws IOException {
        JSONArray jsonArray = new JSONArray(ApiException.check(api.get(USERS_URL)).body());
        List<UserDto> users = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.getJSONObject(i);
            UserDto user = new UserDto();
            user.setId(json.optInt("id"));
            // One member with an empty field must not hide all the others
            user.setFirstName(json.optString("firstName"));
            user.setLastName(json.optString("lastName"));
            user.setAddress(json.optString("address"));
            user.setEmail(json.optString("email"));
            user.setUsername(json.optString("username"));
            user.setStatus(StatusEnum.valueOf(json.optString("status", StatusEnum.PENDING.name())));
            users.add(user);
        }
        return users;
    }

    /** A member added by the librarian is active at once. */
    public void create(User user) throws IOException {
        JSONObject json = new JSONObject();
        json.put("firstName", user.getFirstName());
        json.put("lastName", user.getLastName());
        json.put("address", user.getAddress());
        json.put("email", user.getEmail());
        json.put("username", user.getUsername());
        json.put("password", user.getPassword());
        ApiException.check(api.post(USERS_URL, json.toString()));
        changeStatus(user.getUsername(), StatusEnum.APPROVED);
    }

    public void delete(UserDto user) throws IOException {
        ApiException.check(api.delete(USERS_URL + encode(user.getUsername())));
    }

    /** Saves the profile and the status in one request. */
    public void update(UserDto user) throws IOException {
        JSONObject json = new JSONObject();
        json.put("id", user.getId());
        json.put("firstName", user.getFirstName());
        json.put("lastName", user.getLastName());
        json.put("address", user.getAddress());
        json.put("email", user.getEmail());
        json.put("username", user.getUsername());
        json.put("status", user.getStatus().toString());
        ApiException.check(api.put(USERS_URL, json.toString()));
    }

    public void changeStatus(String username, StatusEnum status) throws IOException {
        ApiException.check(api.patch(USERS_URL + encode(username) + "/status?status=" + status, "text/plain", ""));
    }
}
