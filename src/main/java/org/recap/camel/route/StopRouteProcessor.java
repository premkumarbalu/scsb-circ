package org.recap.camel.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

/**
 * The type Stop route processor.
 */
public class StopRouteProcessor implements Processor {
    private static final Logger logger = Logger.getLogger(StopRouteProcessor.class);
    private String routeId;

    /**
     * Instantiates a new Stop route processor.
     *
     * @param routeId the route id
     */
    public StopRouteProcessor(String routeId) {
        this.routeId = routeId;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Thread stopThread;
        stopThread = new Thread() {
            @Override
            public void run() {
                try {
                    exchange.getContext().stopRoute(routeId);
                } catch (Exception e) {
                    logger.error("Exception while stop route : " + routeId);
                    e.printStackTrace();
                }
            }
        };
        stopThread.start();
    }
}

