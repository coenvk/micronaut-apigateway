package org.coenvk.notificationsystem.gateway;

import io.micronaut.context.annotation.Factory;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.http.client.loadbalance.DiscoveryClientLoadBalancerFactory;
import jakarta.inject.Singleton;

import java.util.*;

@Factory
public class GatewayLoadBalancersFactory {
    @Singleton
    public Map<String, LoadBalancer> serviceLoadBalancers(
            List<RouteDefinition> services,
            DiscoveryClientLoadBalancerFactory factory) {

        Map<String, LoadBalancer> loadBalancers = new HashMap<>();
        services.forEach(service ->
                loadBalancers.put(service.getPath(), factory.create(service.getId())));
        return Collections.unmodifiableMap(loadBalancers);
    }
}
