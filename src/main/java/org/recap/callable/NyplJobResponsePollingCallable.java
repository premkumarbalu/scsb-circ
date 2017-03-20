package org.recap.callable;

import org.recap.ils.NyplApiServiceConnector;
import org.recap.ils.model.nypl.JobData;
import org.recap.ils.model.nypl.response.JobResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Created on 11/1/17.
 * @author rajeshbabuk
 */
public class NyplJobResponsePollingCallable implements Callable {

    private static final Logger logger = LoggerFactory.getLogger(NyplJobResponsePollingCallable.class);

    private String jobId;
    private NyplApiServiceConnector nyplApiServiceConnector;
    private Integer pollingTimeInterval;

    /**
     *
     * @param jobId
     * @param pollingTimeInterval
     * @param nyplApiServiceConnector
     */
    public NyplJobResponsePollingCallable(String jobId, Integer pollingTimeInterval, NyplApiServiceConnector nyplApiServiceConnector) {
        this.jobId = jobId;
        this.nyplApiServiceConnector = nyplApiServiceConnector;
        this.pollingTimeInterval = pollingTimeInterval;
    }

    /**
     *
     * @return
     * @throws Exception
     */
    @Override
    public JobResponse call() throws Exception {
        return poll();
    }

    /**
     *
     * @return
     * @throws Exception
     */
    private JobResponse poll() throws Exception {
        Boolean statusFlag;
        JobResponse jobResponse = nyplApiServiceConnector.queryForJob(jobId);
        JobData jobData = jobResponse.getData();
        statusFlag = jobData.getFinished();
        if (!statusFlag) {
            Thread.sleep(pollingTimeInterval);
            jobResponse = poll();
        }
        return jobResponse;
    }
}
