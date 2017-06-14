package org.recap.service.DeletedRecords;

import org.recap.ReCAPConstants;
import org.recap.repository.DeletedRecordsRepository;
import org.recap.request.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by sudhishk on 2/6/17.
 */
@Service
public class DeletedRecordsService {

    private static final Logger logger = LoggerFactory.getLogger(DeletedRecordsService.class);

    @Autowired
    private DeletedRecordsRepository deletedRecordsRepository;

    @Autowired
    private EmailService emailService;

    /**
     * @return boolean
     */
    public boolean deletedRecords() {
        boolean bReturnMsg = false;

        try {
            long lCountDeleted = deletedRecordsRepository.countByDeletedReportedStatus(ReCAPConstants.DELETED_STATUS_NOT_REPORTED);
            logger.info("Count : " + lCountDeleted);
            if (lCountDeleted > 0) {
                // Change Status
                int statusChange = deletedRecordsRepository.updateDeletedReportedStatus(ReCAPConstants.DELETED_STATUS_REPORTED, ReCAPConstants.DELETED_STATUS_NOT_REPORTED);
                logger.info("Delete Count : " + statusChange);
                // Send Email
                emailService.sendEmail("Total No. of Records Deleted : " + lCountDeleted, "", ReCAPConstants.DELETED_MAIl_TO, "List of Deleted Records");
                bReturnMsg = true;
            }
        } catch (Exception ex) {
            logger.error("", ex);
        }
        return bReturnMsg;
    }
}
