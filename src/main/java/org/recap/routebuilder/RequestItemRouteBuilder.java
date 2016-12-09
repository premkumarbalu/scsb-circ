package org.recap.routebuilder;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.jboss.logging.Logger;
import org.recap.ReCAPConstants;
import org.recap.controller.ItemController;
import org.recap.controller.RequestItemController;
import org.recap.controller.RequestItemValidatorController;
import org.recap.mqconsumer.RequestItemQueueConsumer;
import org.recap.repository.ItemDetailsRepository;
import org.recap.repository.PatronDetailsRepository;
import org.recap.repository.RequestItemDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Created by sudhishk on 2/12/16.
 */
@Component
public class RequestItemRouteBuilder {

    private Logger logger = Logger.getLogger(RequestItemQueueConsumer.class);

    @Autowired
    RequestItemValidatorController requestItemValidatorController;

    @Autowired
    RequestItemController requestItemController;

    @Autowired
    ItemController itemController;

    @Autowired
    ItemDetailsRepository itemDetailsRepository;

    @Autowired
    RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    PatronDetailsRepository patronDetailsRepository;

    @Autowired
    public void RequestItemRouteBuilder(CamelContext camelContext,
                                        RequestItemValidatorController requestItemValidatorController,
                                        RequestItemController requestItemController,
                                        ItemController itemController,
                                        ItemDetailsRepository itemDetailsRepository,
                                        RequestItemDetailsRepository requestItemDetailsRepository
    ){
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.REQUEST_ITEM_QUEUE)
                            .routeId(ReCAPConstants.REQUEST_ITEM_QUEUE_ROUTEID)
                            .threads(10)
                            .choice()
                            .when(header(ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER).isEqualTo(ReCAPConstants.REQUEST_TYPE_RETRIEVAL))
                            .bean(new RequestItemQueueConsumer(requestItemValidatorController, requestItemController,itemController,itemDetailsRepository,requestItemDetailsRepository, patronDetailsRepository), "requestItemOnMessage")
                            .when(header(ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER).isEqualTo(ReCAPConstants.REQUEST_TYPE_EDD))
                            .bean(new RequestItemQueueConsumer(requestItemValidatorController, requestItemController,itemController,itemDetailsRepository,requestItemDetailsRepository, patronDetailsRepository), "requestItemEDDOnMessage")
                            .when(header(ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER).isEqualTo(ReCAPConstants.REQUEST_TYPE_HOLD))
                            .bean(new RequestItemQueueConsumer(requestItemValidatorController, requestItemController,itemController,itemDetailsRepository,requestItemDetailsRepository, patronDetailsRepository), "requestItemHoldOnMessage")
                            .when(header(ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER).isEqualTo(ReCAPConstants.REQUEST_TYPE_BORROW_DIRECT))
                            .bean(new RequestItemQueueConsumer(requestItemValidatorController, requestItemController,itemController,itemDetailsRepository,requestItemDetailsRepository, patronDetailsRepository), "requestItemBorrowDirectOnMessage")
                            .when(header(ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER).isEqualTo(ReCAPConstants.REQUEST_TYPE_RECALL))
                            .bean(new RequestItemQueueConsumer(requestItemValidatorController, requestItemController,itemController,itemDetailsRepository,requestItemDetailsRepository, patronDetailsRepository), "requestItemRecallOnMessage");

                }
            });


            /* PUL Topics*/
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.PUL_REQUEST_TOPIC)
                            .routeId(ReCAPConstants.PUL_REQUEST_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(requestItemValidatorController, requestItemController,itemController,itemDetailsRepository,requestItemDetailsRepository, patronDetailsRepository), "pulRequestTopicOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.PUL_EDD_TOPIC)
                            .routeId(ReCAPConstants.PUL_EDD_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(requestItemValidatorController, requestItemController,itemController,itemDetailsRepository,requestItemDetailsRepository, patronDetailsRepository), "pulEDDTopicOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.PUL_HOLD_TOPIC)
                            .routeId(ReCAPConstants.PUL_HOLD_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(requestItemValidatorController, requestItemController,itemController,itemDetailsRepository,requestItemDetailsRepository, patronDetailsRepository), "pulHoldTopicOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.PUL_RECALL_TOPIC)
                            .routeId(ReCAPConstants.PUL_RECALL_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(requestItemValidatorController, requestItemController,itemController,itemDetailsRepository,requestItemDetailsRepository, patronDetailsRepository), "pulRecalTopicOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.PUL_BORROW_DIRECT_TOPIC)
                            .routeId(ReCAPConstants.PUL_BORROW_DIRECT_TOPIC)
                            .bean(new RequestItemQueueConsumer(requestItemValidatorController, requestItemController,itemController,itemDetailsRepository,requestItemDetailsRepository, patronDetailsRepository), "pulBorrowDirectTopicOnMessage");
                }
            });
            /* PUL Topics*/
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

    }

}
