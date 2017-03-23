package org.recap.model;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.repository.ReportDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 23/3/17.
 */
public class ReportEntityUT extends BaseTestCase{

    @Autowired
    private ReportDetailRepository reportDetailRepository;

    @Test
    public void  saveDataDumpSuccessReport(){
        ReportEntity reportEntity = new ReportEntity();
        List<ReportDataEntity> reportDataEntities = new ArrayList<>();

        ReportDataEntity numberOfBibExportReportEntity = new ReportDataEntity();
        numberOfBibExportReportEntity.setHeaderName("NoOfBibsExported");
        numberOfBibExportReportEntity.setHeaderValue("1");
        reportDataEntities.add(numberOfBibExportReportEntity);

        ReportDataEntity requestingInstitutionReportDataEntity = new ReportDataEntity();
        requestingInstitutionReportDataEntity.setHeaderName("RequestingInstitution");
        requestingInstitutionReportDataEntity.setHeaderValue("CUL");
        reportDataEntities.add(requestingInstitutionReportDataEntity);

        ReportDataEntity institutionReportDataEntity = new ReportDataEntity();
        institutionReportDataEntity.setHeaderName("InstitutionCodes");
        institutionReportDataEntity.setHeaderValue("PUL");
        reportDataEntities.add(institutionReportDataEntity);

        ReportDataEntity fetchTypeReportDataEntity = new ReportDataEntity();
        fetchTypeReportDataEntity.setHeaderName("FetchType");
        fetchTypeReportDataEntity.setHeaderValue("1");
        reportDataEntities.add(fetchTypeReportDataEntity);

        ReportDataEntity exportDateReportDataEntity = new ReportDataEntity();
        exportDateReportDataEntity.setHeaderName("ExportFromDate");
        exportDateReportDataEntity.setHeaderValue(String.valueOf(new Date()));
        reportDataEntities.add(exportDateReportDataEntity);

        ReportDataEntity collectionGroupReportDataEntity = new ReportDataEntity();
        collectionGroupReportDataEntity.setHeaderName("CollectionGroupIds");
        collectionGroupReportDataEntity.setHeaderValue(String.valueOf(1));
        reportDataEntities.add(collectionGroupReportDataEntity);

        ReportDataEntity transmissionTypeReportDataEntity = new ReportDataEntity();
        transmissionTypeReportDataEntity.setHeaderName("TransmissionType");
        transmissionTypeReportDataEntity.setHeaderValue("0");
        reportDataEntities.add(transmissionTypeReportDataEntity);

        ReportDataEntity exportFormatReportDataEntity = new ReportDataEntity();
        exportFormatReportDataEntity.setHeaderName("ExportFormat");
        exportFormatReportDataEntity.setHeaderValue("1");
        reportDataEntities.add(exportFormatReportDataEntity);

        ReportDataEntity emailIdReportDataEntity = new ReportDataEntity();
        emailIdReportDataEntity.setHeaderName("ToEmailId");
        emailIdReportDataEntity.setHeaderValue("0");
        reportDataEntities.add(emailIdReportDataEntity);

        reportEntity.setFileName("2017-02-01 13:41");
        reportEntity.setCreatedDate(new Date());
        reportEntity.setType("BatchExportSuccess");
        reportEntity.setReportDataEntities(reportDataEntities);
        reportEntity.setInstitutionName("PUL");

        ReportEntity savedReportEntity = reportDetailRepository.save(reportEntity);
        assertNotNull(savedReportEntity);
        assertNotNull(savedReportEntity.getRecordNumber());
        assertNotNull(savedReportEntity.getCreatedDate());
        assertNotNull(savedReportEntity.getFileName());
        assertNotNull(savedReportEntity.getInstitutionName());
        assertNotNull(savedReportEntity.getType());
        assertNotNull(savedReportEntity.getReportDataEntities());
        assertNotNull(savedReportEntity.getReportDataEntities().get(0).getHeaderName());
        assertNull(savedReportEntity.getReportDataEntities().get(0).getRecordNum());
        assertNotNull(savedReportEntity.getReportDataEntities().get(0).getHeaderValue());
        assertNotNull(savedReportEntity.getReportDataEntities().get(0).getReportDataId());
    }

}