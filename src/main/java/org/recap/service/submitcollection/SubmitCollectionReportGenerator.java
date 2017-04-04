package org.recap.service.submitcollection;

import org.recap.model.ReportDataRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Created by premkb on 20/3/17.
 */
@Service
public class SubmitCollectionReportGenerator {

    @Value("${server.protocol}")
    private String serverProtocol;

    @Value("${scsb.solr.client.url}")
    private String solrClientUrl;

    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    public String getServerProtocol() {
        return serverProtocol;
    }

    public String getSolrClientUrl() {
        return solrClientUrl;
    }

    public String generateReport(ReportDataRequest reportDataRequest){
        return getRestTemplate().postForObject(getServerProtocol() + getSolrClientUrl() + "/reportsService/generateCsvReport", reportDataRequest, String.class);

    }

}
