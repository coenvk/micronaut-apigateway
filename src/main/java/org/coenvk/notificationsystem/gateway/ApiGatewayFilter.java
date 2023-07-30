package org.coenvk.notificationsystem.gateway;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.discovery.ServiceInstance;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.http.client.ProxyHttpClient;
import io.micronaut.http.filter.FilterChain;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.reactivex.rxjava3.core.Flowable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

import java.util.Map;

@Slf4j
@Filter("/**")
public class ApiGatewayFilter implements HttpServerFilter {
    @Named("serviceLoadBalancers")
    private final Map<String, LoadBalancer> serviceLoadBalancers;
    private final ProxyHttpClient proxyHttpClient;

    @Inject
    public ApiGatewayFilter(Map<String, LoadBalancer> serviceLoadBalancers, ProxyHttpClient proxyHttpClient) {
        this.serviceLoadBalancers = serviceLoadBalancers;
        this.proxyHttpClient = proxyHttpClient;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {

        String servicePath = request.getPath().replaceAll("^/([^/]+).*$", "$1");

        if (!serviceLoadBalancers.containsKey(servicePath)) {
            return Publishers.just(HttpResponse.notFound());
        }

        LoadBalancer loadBalancer = serviceLoadBalancers.get(servicePath);
        return Flowable.fromPublisher(loadBalancer.select())
                .flatMap(serviceInstance -> {
                    var finalRequest = prepareRequestForTarget(request, serviceInstance);
                    log.info("Proxying {} to service {} ({}:{}) as {}",
                            request.getPath(), serviceInstance.getId(), serviceInstance.getHost(),
                            serviceInstance.getPort(), finalRequest.getPath());

                    return proxyHttpClient.proxy(finalRequest);
                });
    }

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(HttpRequest<?> request, FilterChain chain) {
        return HttpServerFilter.super.doFilter(request, chain);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    private MutableHttpRequest<?> prepareRequestForTarget(HttpRequest<?> request, ServiceInstance serviceInstance) {
        return request.mutate()
                .uri(uri -> uri
                        .scheme("http")
                        .host(serviceInstance.getHost())
                        .port(serviceInstance.getPort())
                        .replacePath(request.getPath().replace("/" + serviceInstance.getId(), "")));
    }
}
