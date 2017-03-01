package org.recap.processor;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ils.model.nypl.response.JobResponse;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 21/2/17.
 */
public class NyplJobResponsePollingProcessorUT extends BaseTestCase{

    @Autowired
    NyplJobResponsePollingProcessor nyplJobResponsePollingProcessor;

    @Test
    public void testPollNyplRequestItemJobResponse(){
        JobResponse response = nyplJobResponsePollingProcessor.pollNyplRequestItemJobResponse("93558738b214f130");
        assertNotNull(response);
    }

}