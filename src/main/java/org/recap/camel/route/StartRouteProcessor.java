package org.recap.camel.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.recap.ReCAPConstants;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by akulak on 19/7/17.
 */
@Service
@Scope("prototype")
public class StartRouteProcessor implements Processor {

    private static final Logger logger = Logger.getLogger(StartRouteProcessor.class);
    private String routeId;

    public StartRouteProcessor(String routeId) {
        this.routeId = routeId;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        exchange.getContext().startRoute(routeId);
    }
}
