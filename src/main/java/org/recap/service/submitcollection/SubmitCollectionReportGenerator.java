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

    @Value("${scsb.solr.client.url}")
    private String solrClientUrl;

    /**
     * Gets rest template.
     *
     * @return the rest template
     */
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    /**
     * Gets solr client url.
     *
     * @return the solr client url
     */
    public String getSolrClientUrl() {
        return solrClientUrl;
    }

    /**
     * Generate report string.
     *
     * @param reportDataRequest the report data request
     * @return the string
     */
    public String generateReport(ReportDataRequest reportDataRequest){
        return getRestTemplate().postForObject(getSolrClientUrl() + "/reportsService/generateCsvReport", reportDataRequest, String.class);

    }

}
