package org.recap.routebuilder;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.recap.ReCAPConstants;
import org.recap.camel.route.StartRouteProcessor;
import org.recap.camel.route.StopRouteProcessor;
import org.recap.mqconsumer.RequestItemQueueConsumer;
import org.recap.request.BulkItemRequestProcessService;
import org.recap.request.BulkItemRequestService;
import org.recap.request.ItemEDDRequestService;
import org.recap.request.ItemRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by sudhishk on 2/12/16.
 */
@Component
public class RequestItemRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(RequestItemRouteBuilder.class);

    /**
     * Instantiates a new Request item route builder.
     *
     * @param camelContext          the camel context
     * @param itemRequestService    the item request service
     * @param itemEDDRequestService the item edd request service
     */
    @Autowired
    public RequestItemRouteBuilder(@Value("${bulk.request.concurrent.consumer.count}") Integer bulkRequestConsumerCount, CamelContext camelContext, ItemRequestService itemRequestService, ItemEDDRequestService itemEDDRequestService, BulkItemRequestService bulkItemRequestService, BulkItemRequestProcessService bulkItemRequestProcessService) {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.REQUEST_ITEM_QUEUE)
                        .routeId(ReCAPConstants.REQUEST_ITEM_QUEUE_ROUTEID)
                        .threads(30,50)
                        .choice()
                            .when(header(ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER).isEqualTo(ReCAPConstants.REQUEST_TYPE_RETRIEVAL))
                                .bean(new RequestItemQueueConsumer(itemRequestService), "requestItemOnMessage")
                            .when(header(ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER).isEqualTo(ReCAPConstants.REQUEST_TYPE_EDD))
                                .bean(new RequestItemQueueConsumer(itemEDDRequestService), "requestItemEDDOnMessage")
                            .when(header(ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER).isEqualTo(ReCAPConstants.REQUEST_TYPE_BORROW_DIRECT))
                                .bean(new RequestItemQueueConsumer(itemRequestService, itemEDDRequestService), "requestItemBorrowDirectOnMessage")
                            .when(header(ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER).isEqualTo(ReCAPConstants.REQUEST_TYPE_RECALL))
                                .bean(new RequestItemQueueConsumer(itemRequestService), "requestItemRecallOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SCSB_OUTGOING_QUEUE)
                        .routeId(ReCAPConstants.SCSB_OUTGOING_ROUTE_ID)
                        .to(ReCAPConstants.LAS_OUTGOING_QUEUE)
                        .onCompletion().bean(new RequestItemQueueConsumer(itemRequestService),"lasOutgoingQOnCompletion");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.LAS_INCOMING_QUEUE)
                        .routeId(ReCAPConstants.LAS_INCOMING_ROUTE_ID)
                        .choice()
                        .when(header(ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER).isEqualTo(ReCAPConstants.REQUEST_TYPE_RETRIEVAL))
                        .bean(new RequestItemQueueConsumer(itemRequestService), "lasResponseRetrivalOnMessage")
                        .when(header(ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER).isEqualTo(ReCAPConstants.REQUEST_TYPE_EDD))
                        .bean(new RequestItemQueueConsumer(itemRequestService), "lasResponseEDDOnMessage")
                        .when(header(ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER).isEqualTo(ReCAPConstants.REQUEST_TYPE_PW_INDIRECT))
                        .bean(new RequestItemQueueConsumer(itemRequestService), "lasResponsePWIOnMessage")
                        .when(header(ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER).isEqualTo(ReCAPConstants.REQUEST_TYPE_PW_DIRECT))
                        .bean(new RequestItemQueueConsumer(itemRequestService), "lasResponsePWDOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.BULK_REQUEST_ITEM_QUEUE)
                            .routeId(ReCAPConstants.BULK_REQUEST_ITEM_QUEUE_ROUTEID)
                            .bean(new RequestItemQueueConsumer(bulkItemRequestService), "bulkRequestItemOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.BULK_REQUEST_ITEM_PROCESSING_QUEUE + ReCAPConstants.ASYNC_CONCURRENT_CONSUMERS + bulkRequestConsumerCount)
                            .routeId(ReCAPConstants.BULK_REQUEST_ITEM_PROCESSING_QUEUE_ROUTEID)
                            .bean(new RequestItemQueueConsumer(bulkItemRequestProcessService), "bulkRequestProcessItemOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.REQUEST_ITEM_LAS_STATUS_CHECK_QUEUE)
                            .routeId(ReCAPConstants.REQUEST_ITEM_LAS_STATUS_CHECK_QUEUE_ROUTEID)
                            .noAutoStartup()
                            .choice()
                                .when(body().isNull())
                                    .log("No Requests To Process")
                                .otherwise()
                                    .log("Start Route 1")
                                    .process(new StartRouteProcessor(ReCAPConstants.REQUEST_ITEM_LAS_STATUS_CHECK_QUEUE_ROUTEID))
                                    .bean(new RequestItemQueueConsumer(itemRequestService), "requestItemLasStatusCheckOnMessage")
                            .endChoice();
                }
            });
            /* PUL Topics*/
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.PUL_REQUEST_TOPIC)
                            .routeId(ReCAPConstants.PUL_REQUEST_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(itemRequestService, itemEDDRequestService), "pulRequestTopicOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.PUL_EDD_TOPIC)
                            .routeId(ReCAPConstants.PUL_EDD_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(itemRequestService, itemEDDRequestService), "pulEDDTopicOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.PUL_RECALL_TOPIC)
                            .routeId(ReCAPConstants.PUL_RECALL_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(itemRequestService, itemEDDRequestService), "pulRecalTopicOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.PUL_BORROW_DIRECT_TOPIC)
                            .routeId(ReCAPConstants.PUL_BORROW_DIRECT_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(itemRequestService, itemEDDRequestService), "pulBorrowDirectTopicOnMessage");
                }
            });
            /* PUL Topics*/

            /* CUL Topics*/
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.CUL_REQUEST_TOPIC)
                            .routeId(ReCAPConstants.CUL_REQUEST_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(itemRequestService, itemEDDRequestService), "culRequestTopicOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.CUL_EDD_TOPIC)
                            .routeId(ReCAPConstants.CUL_EDD_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(itemRequestService, itemEDDRequestService), "culEDDTopicOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.CUL_RECALL_TOPIC)
                            .routeId(ReCAPConstants.CUL_RECALL_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(itemRequestService, itemEDDRequestService), "culRecalTopicOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.CUL_BORROW_DIRECT_TOPIC)
                            .routeId(ReCAPConstants.CUL_BORROW_DIRECT_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(itemRequestService, itemEDDRequestService), "culBorrowDirectTopicOnMessage");
                }
            });
            /* CUL Topics*/

            /* NYPL Topics */
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.NYPL_REQUEST_TOPIC)
                            .routeId(ReCAPConstants.NYPL_REQUEST_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(itemRequestService, itemEDDRequestService), "nyplRequestTopicOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.NYPL_EDD_TOPIC)
                            .routeId(ReCAPConstants.NYPL_EDD_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(itemRequestService, itemEDDRequestService), "nyplEDDTopicOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.NYPL_RECALL_TOPIC)
                            .routeId(ReCAPConstants.NYPL_RECALL_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(itemRequestService, itemEDDRequestService), "nyplRecalTopicOnMessage");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.NYPL_BORROW_DIRECT_TOPIC)
                            .routeId(ReCAPConstants.NYPL_BORROW_DIRECT_TOPIC_ROUTEID)
                            .bean(new RequestItemQueueConsumer(itemRequestService, itemEDDRequestService), "nyplBorrowDirectTopicOnMessage");
                }
            });
            /* NYPL Topics */


        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }

    }

}
