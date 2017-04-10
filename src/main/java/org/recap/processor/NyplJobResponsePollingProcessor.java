package org.recap.processor;

import org.recap.ReCAPConstants;
import org.recap.callable.NyplJobResponsePollingCallable;
import org.recap.ils.NyplApiServiceConnector;
import org.recap.ils.model.nypl.JobData;
import org.recap.ils.model.nypl.response.JobResponse;
import org.recap.ils.service.NyplApiResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * Created by rajeshbabuk on 12/1/17.
 */
@Component
public class NyplJobResponsePollingProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NyplJobResponsePollingProcessor.class);

    @Value("${nypl.polling.max.timeout}")
    private Integer pollingMaxTimeOut;

    @Value("${nypl.polling.time.interval}")
    private Integer pollingTimeInterval;

    /**
     * The Nypl api service connector.
     */
    @Autowired
    NyplApiServiceConnector nyplApiServiceConnector;

    /**
     * The Nypl api response util.
     */
    @Autowired
    NyplApiResponseUtil nyplApiResponseUtil;

    /**
     * Poll nypl request item job response job response.
     *
     * @param jobId the job id
     * @return the job response
     */
    public JobResponse pollNyplRequestItemJobResponse(String jobId) {
        JobResponse jobResponse = new JobResponse();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<JobResponse> future = executor.submit(new NyplJobResponsePollingCallable(jobId, pollingTimeInterval, nyplApiServiceConnector));
            logger.info("Polling on job id {} started", jobId);
            jobResponse = future.get(pollingMaxTimeOut, TimeUnit.SECONDS);
            JobData jobData = jobResponse.getData();
            if (null != jobData) {
                jobResponse.setStatusMessage(nyplApiResponseUtil.getJobStatusMessage(jobData));
            }
            executor.shutdown();
            return jobResponse;
        } catch (InterruptedException e) {
            logger.error("Nypl job response interrupted for job id -> " + jobId);
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
            executor.shutdown();
            jobResponse.setStatusMessage("Nypl job response interrupted : " + e.getMessage());
            return jobResponse;
        } catch (ExecutionException e) {
            logger.error("Nypl job response execution failed for job id -> " + jobId);
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
            executor.shutdown();
            jobResponse.setStatusMessage("Nypl job response execution failed : " + e.getMessage());
            return jobResponse;
        } catch (TimeoutException e) {
            logger.error("Nypl job response polling timed out for job id -> " + jobId);
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
            executor.shutdown();
            jobResponse.setStatusMessage("Nypl job response polling timed out : " + e.getMessage());
            return jobResponse;
        } catch (Exception e) {
            logger.error("Nypl job response polling failed for job id -> " + jobId);
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
            executor.shutdown();
            jobResponse.setStatusMessage("Nypl job response polling failed : " + e.getMessage());
            return jobResponse;
        }
    }
}
