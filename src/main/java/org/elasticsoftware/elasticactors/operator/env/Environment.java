package org.elasticsoftware.elasticactors.operator.env;

import java.util.Map;

public interface Environment {

    String OPERATOR_NAMESPACE = "OPERATOR_NAMESPACE";
    String TARGET_NAMESPACES = "TARGET_NAMESPACES";
    String POD_NAME = "POD_NAME";
    String KUBERNETES_SERVICE_HOST = "KUBERNETES_SERVICE_HOST";
    String KUBERNETES_SERVICE_PORT_HTTPS = "KUBERNETES_SERVICE_PORT_HTTPS";

    String get(String name);

    Map<String, String> all();

    static Environment fromMap(Map<String, String> map) {
        return new Environment() {
            @Override
            public String get(String name) {
                return map.get(name);
            }

            @Override
            public Map<String, String> all() {
                return map;
            }
        };
    }
}
