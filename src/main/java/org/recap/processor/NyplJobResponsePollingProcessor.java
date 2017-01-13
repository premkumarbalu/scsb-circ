package org.recap.processor;

import org.jboss.logging.Logger;
import org.recap.callable.NyplJobResponsePollingCallable;
import org.recap.ils.NyplApiServiceConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * Created by rajeshbabuk on 12/1/17.
 */
@Component
public class NyplJobResponsePollingProcessor {

    private Logger logger = Logger.getLogger(NyplJobResponsePollingProcessor.class);

    @Value("${nypl.polling.max.timeout}")
    private Integer pollingMaxTimeOut;

    @Value("${nypl.polling.time.interval}")
    private Integer pollingTimeInterval;

    @Autowired
    NyplApiServiceConnector nyplApiServiceConnector;

    public Boolean pollNyplRequestItemJobResponse(String jobId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(new NyplJobResponsePollingCallable(jobId, pollingTimeInterval, nyplApiServiceConnector));
        logger.info("Polling on job id " + jobId + " started");
        try {
            Boolean statusFlag = future.get(pollingMaxTimeOut, TimeUnit.SECONDS);
            executor.shutdown();
            return statusFlag;
        } catch (InterruptedException e) {
            logger.error("Nypl job response interrupted for job id -> " + jobId);
            logger.error("Exception -> " + e.getMessage());
            executor.shutdown();
            return false;
        } catch (ExecutionException e) {
            logger.error("Nypl job response execution failed for job id -> " + jobId);
            logger.error("Exception -> " + e.getMessage());
            executor.shutdown();
            return false;
        } catch (TimeoutException e) {
            logger.error("Nypl job response polling timed out for job id -> " + jobId);
            logger.error("Exception -> " + e.getMessage());
            executor.shutdown();
            return false;
        } catch (Exception e) {
            logger.error("Nypl job response polling failed for job id -> " + jobId);
            logger.error("Exception -> " + e.getMessage());
            executor.shutdown();
            return false;
        }
    }
}
