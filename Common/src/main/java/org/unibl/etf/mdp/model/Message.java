package org.unibl.etf.mdp.model;

import com.google.gson.JsonElement;
import org.unibl.etf.mdp.common.Json;

import java.lang.reflect.Type;
import java.util.List;

/**
 * A protocol message. The body is kept as JSON and decoded only into the type the receiver asks for.
 */
public class Message {
    /** entityType values for UNREGISTER: which DiscoveryServer registry to leave */
    public static final String ENTITY_SERVER = "server";
    public static final String ENTITY_USER = "user";

    private String type;
    private String username;
    private JsonElement body;
    private String entityType;

    public Message(String type) {
        super();
        this.type = type;
    }

    public Message(String type, String username) {
        super();
        this.type = type;
        this.username = username;
    }

    public Message(String type, String username, Object body) {
        super();
        this.type = type;
        this.username = username;
        setBody(body);
    }

    public Message(String type, String username, Object body, String entityType) {
        this(type, username, body);
        this.entityType = entityType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean hasBody() {
        return body != null && !body.isJsonNull();
    }

    public <T> T getBody(Class<T> bodyType) {
        return Json.fromTree(body, bodyType);
    }

    public <T> T getBody(Type bodyType) {
        return Json.fromTree(body, bodyType);
    }

    public <T> List<T> getBodyList(Class<T> elementType) {
        return Json.fromTree(body, Json.listOf(elementType));
    }

    public void setBody(Object body) {
        this.body = Json.toTree(body);
    }

    @Override
    public String toString() {
        String text = hasBody() ? body.toString() : "null";
        // Bodies can hold whole books
        if (text.length() > 200) {
            text = text.substring(0, 200) + "...";
        }
        return "Message [type=" + type + ", username=" + username + ", body=" + text + "]";
    }

}
