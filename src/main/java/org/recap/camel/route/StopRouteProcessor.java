package org.recap.camel.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.recap.ReCAPConstants;

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
                    if (routeId.equalsIgnoreCase(ReCAPConstants.ACCESSION_RECONCILATION_FTP_PUL_ROUTE) ||
                            routeId.equalsIgnoreCase(ReCAPConstants.ACCESSION_RECONCILATION_FTP_CUL_ROUTE) ||
                            routeId.equalsIgnoreCase(ReCAPConstants.ACCESSION_RECONCILATION_FTP_NYPL_ROUTE)) {
                        stopRouteWithTimeOutOption();
                    } else if (routeId.equalsIgnoreCase(ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_FTP_ROUTE) ||
                            routeId.equalsIgnoreCase(ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_FTP_ROUTE) ||
                            routeId.equalsIgnoreCase(ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_FS_ROUTE)) {
                        stopRouteWithTimeOutOption();
                    } else {
                        exchange.getContext().stopRoute(routeId);
                    }
                    logger.info("Stop Route " + routeId);
                } catch (Exception e) {
                    logger.error("Exception while stop route : " + routeId);
                    logger.error(ReCAPConstants.LOG_ERROR + e);

                }
            }

            private void stopRouteWithTimeOutOption() throws Exception {
                exchange.getContext().getShutdownStrategy().setTimeout(1);
                exchange.getContext().stopRoute(routeId);
            }
        };
        stopThread.start();
    }
}

