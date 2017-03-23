package org.recap.model.jaxb;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jaxb.marc.*;


import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 23/3/17.
 */
public class BibRecordUT extends BaseTestCase {
    @Test
    public void testBib(){
        Bib bib = new Bib();
        bib.setContent(getContentType());
        bib.setOwningInstitutionBibId(".b100000046");
        bib.setOwningInstitutionId("NYPL");
        assertNotNull(bib.getContent());
        assertNotNull(bib.getOwningInstitutionId());
        assertNotNull(bib.getOwningInstitutionBibId());
        assertNotNull(bib.getContent());
        assertNotNull(bib.getContent().getCollection());
        assertNotNull(bib.getContent().getCollection().getId());
        assertNotNull(bib.getContent().getCollection().getRecord());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getId());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getControlfield());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getDatafield());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getLeader());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getType());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getLeader().getId());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getLeader().getValue());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getDatafield().get(0).getId());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getDatafield().get(0).getInd1());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getDatafield().get(0).getInd2());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getDatafield().get(0).getTag());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getDatafield().get(0).getSubfield());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getDatafield().get(0).getSubfield().get(0).getId());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getDatafield().get(0).getSubfield().get(0).getValue());
        assertNotNull(bib.getContent().getCollection().getRecord().get(0).getDatafield().get(0).getSubfield().get(0).getCode());
        assertNotNull(getControlFieldType().getId());
        assertNotNull(getControlFieldType().getValue());
        assertNotNull(getControlFieldType().getTag());
    }

    @Test
    public void testHolding(){
        Holding holding = new Holding();
        holding.setContent(getContentType());
        holding.setOwningInstitutionHoldingsId(".b0000024581");
        holding.setItems(Arrays.asList(getItems()));
        assertNotNull(holding.getContent());
        assertNotNull(holding.getItems());
        assertNotNull(holding.getOwningInstitutionHoldingsId());
        assertNotNull(holding.getItems().get(0).getContent());
    }

    public Items getItems(){
        Items items = new Items();
        items.setContent(getContentType());
        return items;
    }

    public SubfieldatafieldType getSubfieldatafieldType(){
        SubfieldatafieldType subfieldatafieldType = new SubfieldatafieldType();
        subfieldatafieldType.setValue("2016002744");
        subfieldatafieldType.setCode("a");
        subfieldatafieldType.setId("1");
        return subfieldatafieldType;
    }

    public DataFieldType getDataFieldType(){
        DataFieldType dataFieldType = new DataFieldType();
        dataFieldType.setSubfield(Arrays.asList(getSubfieldatafieldType()));
        dataFieldType.setId("1");
        dataFieldType.setInd1("1");
        dataFieldType.setInd2("0");
        dataFieldType.setTag("245");
        return dataFieldType;
    }

    public ControlFieldType getControlFieldType(){
        ControlFieldType controlFieldType = new ControlFieldType();
        controlFieldType.setValue("9919400");
        controlFieldType.setTag("001");
        controlFieldType.setId("1");
        return controlFieldType;
    }

    public LeaderFieldType getLeaderFieldType(){
        LeaderFieldType leaderFieldType = new LeaderFieldType();
        leaderFieldType.setId("1");
        leaderFieldType.setValue("01750cam a2200493 i 4500");
        return leaderFieldType;
    }

    public RecordType getRecordType(){
        RecordType recordType = new RecordType();
        recordType.setId("1");
        recordType.setDatafield(Arrays.asList(getDataFieldType()));
        recordType.setType(RecordTypeType.BIBLIOGRAPHIC);
        recordType.setLeader(getLeaderFieldType());
        return recordType;
    }

    public CollectionType getCollectionType(){
        CollectionType collectionType = new CollectionType();
        collectionType.setId("1");
        collectionType.setRecord(Arrays.asList(getRecordType()));
        return collectionType;
    }

    public ContentType getContentType(){
        ContentType contentType = new ContentType();
        contentType.setCollection(getCollectionType());
        return contentType;
    }

}