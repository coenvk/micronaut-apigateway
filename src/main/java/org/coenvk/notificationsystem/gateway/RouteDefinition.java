package org.coenvk.notificationsystem.gateway;

import io.micronaut.context.annotation.EachProperty;

@EachProperty(value = "gateway.routes", list = true)
public interface RouteDefinition {
    String getId();
    String getPath();
}
