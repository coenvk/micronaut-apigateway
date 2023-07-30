package org.coenvk.notificationsystem.gateway;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class ConfigurationTests {
    @Test
    void testConfiguration() {
        var items = new HashMap<String, Object>();
        items.put("gateway.routes[0].id", "identity-provider");
        items.put("gateway.routes[0].path", "identity");

        var ctx = ApplicationContext.run(items);
        var config = ctx.getBean(RouteDefinition.class);

        Assertions.assertEquals("identity-provider", config.getId());
    }
}
