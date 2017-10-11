package org.recap.routebuilder;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.recap.ReCAPConstants;
import org.recap.controller.SubmitCollectionJobController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by rajeshbabuk on 14/9/17.
 */
@Component
public class SubmitCollectionJobRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionJobRouteBuilder.class);

    /**
     * Instantiates a new Submit collection job route builder.
     *
     * @param camelContext                  the camel context
     * @param submitCollectionJobController the submit collection job controller
     */
    @Autowired
    public SubmitCollectionJobRouteBuilder(CamelContext camelContext, SubmitCollectionJobController submitCollectionJobController) {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SUBMIT_COLLECTION_JOB_INITIATE_QUEUE)
                            .routeId(ReCAPConstants.SUBMIT_COLLECTION_JOB_INITIATE_ROUTE_ID)
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    String jobId = (String) exchange.getIn().getBody();
                                    logger.info("Submit Collection Job Initiated for Job Id : {}", jobId);
                                    String submitCollectionJobStatus = submitCollectionJobController.startSubmitCollection();
                                    logger.info("Job Id : {} Submit Collection Job Status : {}", jobId, submitCollectionJobStatus);
                                    exchange.getIn().setBody("JobId:" + jobId + "|" + submitCollectionJobStatus);
                                }
                            })
                            .onCompletion()
                            .to(ReCAPConstants.SUBMIT_COLLECTION_JOB_COMPLETION_OUTGOING_QUEUE)
                            .end();
                }
            });
        } catch (Exception ex) {
            logger.error(ReCAPConstants.LOG_ERROR, ex);
        }
    }
}
