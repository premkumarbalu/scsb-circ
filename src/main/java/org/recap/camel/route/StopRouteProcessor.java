package org.recap.camel.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

/**
 * Created by akulak on 10/5/17.
 */
public class StopRouteProcessor implements Processor {
    Logger logger = Logger.getLogger(StopRouteProcessor.class);
    String routeId;
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

