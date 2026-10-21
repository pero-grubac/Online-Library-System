package org.unibl.etf.mdp.common;

import java.io.IOException;

/** The LibraryServer refused a request; the message is the server's own explanation. */
public class ApiException extends IOException {
    private final int status;

    public ApiException(RestClient.Result result) {
        super(result.body() == null || result.body().isBlank() ? "HTTP " + result.status() : result.body());
        this.status = result.status();
    }

    public int getStatus() {
        return status;
    }

    /** @return the result when it is a success */
    public static RestClient.Result check(RestClient.Result result) throws ApiException {
        if (!result.ok()) {
            throw new ApiException(result);
        }
        return result;
    }
}
