package org.recap.service.submitcollection;

import org.marc4j.MarcException;
import org.recap.ReCAPConstants;
import org.recap.model.BibliographicEntity;
import org.recap.model.InstitutionEntity;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.marc.BibRecords;
import org.recap.model.report.SubmitCollectionReportInfo;
import org.recap.service.submitcollection.callable.SubmitCollectionCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.ResourceAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by premkb on 19/9/17.
 */
@Service
public class SubmitCollectionExecutorService extends SubmitCollectionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionExecutorService.class);

    @Autowired
    private SubmitCollectionHelperService submitCollectionHelperService;

    @Autowired
    private ApplicationContext applicationContext;

    private ExecutorService executorService;

    @Value("${submit.collection.thread.size}")
    private int submitCollectionThreadSize;

    public String loadBibRecord(Set<Integer> processedBibIds, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<Map<String, String>> idMapToRemoveIndexList, boolean isCGDProtected, InstitutionEntity institutionEntity, String format, BibRecords bibRecords, int count, Set<String> processedBarcodeSetForDummyRecords) throws Exception{
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //ExecutorService executorService = Executors.newFixedThreadPool(submitCollectionThreadSize);
        logger.info("inside SubmitCollectionExecutorService loadBibRecord");
        List<Callable<BibliographicEntity>> callables = new ArrayList<>();
        for(BibRecord bibRecord : bibRecords.getBibRecords()){
            SubmitCollectionCallable submitCollectionCallable = applicationContext.getBean(SubmitCollectionCallable.class,bibRecord,format, submitCollectionReportInfoMap, idMapToRemoveIndexList,isCGDProtected,institutionEntity,processedBarcodeSetForDummyRecords);
            callables.add(submitCollectionCallable);
        }
        try {
            List<Future<BibliographicEntity>> futureList = getExecutorService().invokeAll(callables);
            futureList.stream()
                    .map(future -> {
                        try{
                            return future.get();
                        } catch (InterruptedException | ExecutionException e){
                            logger.error(ReCAPConstants.LOG_ERROR,e);
                            throw new RuntimeException(e);
                        }
                    });
            for(Future future:futureList){
                BibliographicEntity bibliographicEntity = (BibliographicEntity)future.get();
                if (null!=bibliographicEntity && null != bibliographicEntity.getBibliographicId()) {
                    processedBibIds.add(bibliographicEntity.getBibliographicId());
                }
                logger.info("Process completed for Bib record no: {}",count);
                count ++;
            }

    } catch (MarcException me) {
        logger.error(ReCAPConstants.LOG_ERROR,me);
        return ReCAPConstants.INVALID_MARC_XML_FORMAT_IN_SCSBXML_MESSAGE;
    } catch (ResourceAccessException rae){
        logger.error(ReCAPConstants.LOG_ERROR,rae);
        return ReCAPConstants.SCSB_SOLR_CLIENT_SERVICE_UNAVAILABLE;
    }

/*        for (BibRecord bibRecord : bibRecords.getBibRecords()) {
            logger.info("Processing Bib record no: {}",count);
            try {
                BibliographicEntity bibliographicEntity = submitCollectionHelperService.loadData(bibRecord, format, submitCollectionReportInfoMap, idMapToRemoveIndexList,isCGDProtected,institutionEntity,processedBarcodeSetForDummyRecords);
                if (null!=bibliographicEntity && null != bibliographicEntity.getBibliographicId()) {
                    processedBibIds.add(bibliographicEntity.getBibliographicId());
                }
            } catch (MarcException me) {
                logger.error(ReCAPConstants.LOG_ERROR,me);
                return ReCAPConstants.INVALID_MARC_XML_FORMAT_IN_SCSBXML_MESSAGE;
            } catch (ResourceAccessException rae){
                logger.error(ReCAPConstants.LOG_ERROR,rae);
                return ReCAPConstants.SCSB_SOLR_CLIENT_SERVICE_UNAVAILABLE;
            }
            logger.info("Process completed for Bib record no: {}",count);
            count ++;
        }*/
        stopWatch.stop();
        logger.info("time take to update the record through threading--->{} sec",stopWatch.getTotalTimeSeconds());
        return null;
    }

    /**
     * Gets executor service.
     *
     * @return the executor service
     */
    public ExecutorService getExecutorService() {
        logger.info("submitCollectionThreadSize--->{}",submitCollectionThreadSize);
        if (null == executorService) {
            executorService = Executors.newFixedThreadPool(submitCollectionThreadSize);
        }
        if (executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(submitCollectionThreadSize);
        }
        return executorService;
    }
}
