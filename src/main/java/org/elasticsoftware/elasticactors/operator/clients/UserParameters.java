package org.elasticsoftware.elasticactors.operator.clients;

public class UserParameters {
    private final String password;

    public UserParameters(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getTags() {
        return "";
    }
}
