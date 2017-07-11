package org.recap.converter;

import org.junit.Assert;
import org.junit.Test;
import org.marc4j.marc.Record;
import org.recap.BaseTestCase;
import org.recap.model.BibliographicEntity;
import org.recap.model.InstitutionEntity;
import org.recap.repository.InstitutionDetailsRepository;
import org.recap.util.MarcUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by premkb on 21/12/16.
 */
public class MarcToBibEntityConverterUT extends BaseTestCase {

    @Autowired
    private MarcToBibEntityConverter marcToBibEntityConverter;

    @Autowired
    private MarcUtil marcUtil;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    private String marcXmlContent = "<collection xmlns=\"http://www.loc.gov/MARC21/slim\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\">\n" +
            "<record>\n" +
            "    <leader>01011cam a2200289 a 4500</leader>\n" +
            "    <controlfield tag=\"001\">115115</controlfield>\n" +
            "    <controlfield tag=\"005\">20160503221017.0</controlfield>\n" +
            "    <controlfield tag=\"008\">820315s1982 njua b 00110 eng</controlfield>\n" +
            "    <datafield ind1=\" \" ind2=\" \" tag=\"010\">\n" +
            "        <subfield code=\"a\">81008543</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\" \" ind2=\" \" tag=\"020\">\n" +
            "        <subfield code=\"a\">0132858908</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "        <subfield code=\"a\">(OCoLC)7555877</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "        <subfield code=\"a\">(CStRLIN)NJPG82-B5675</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "        <subfield code=\"9\">AAS9821TS</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\"0\" ind2=\" \" tag=\"039\">\n" +
            "        <subfield code=\"a\">2</subfield>\n" +
            "        <subfield code=\"b\">3</subfield>\n" +
            "        <subfield code=\"c\">3</subfield>\n" +
            "        <subfield code=\"d\">3</subfield>\n" +
            "        <subfield code=\"e\">3</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\"0\" ind2=\" \" tag=\"050\">\n" +
            "        <subfield code=\"a\">QE28.3</subfield>\n" +
            "        <subfield code=\"b\">.S76 1982</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\"0\" ind2=\" \" tag=\"082\">\n" +
            "        <subfield code=\"a\">551.7</subfield>\n" +
            "        <subfield code=\"2\">19</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\"1\" ind2=\" \" tag=\"100\">\n" +
            "        <subfield code=\"a\">Stokes, William Lee,</subfield>\n" +
            "        <subfield code=\"d\">1915-1994.</subfield>\n" +
            "        <subfield code=\"0\">(uri)http://id.loc.gov/authorities/names/n50011514</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\"1\" ind2=\"0\" tag=\"245\">\n" +
            "        <subfield code=\"a\">Essentials of earth history :</subfield>\n" +
            "        <subfield code=\"b\">an introduction to historical geology /</subfield>\n" +
            "        <subfield code=\"c\">W. Lee Stokes.</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\" \" ind2=\" \" tag=\"250\">\n" +
            "        <subfield code=\"a\">4th ed.</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\" \" ind2=\" \" tag=\"260\">\n" +
            "        <subfield code=\"a\">Englewood Cliffs, N.J. :</subfield>\n" +
            "        <subfield code=\"b\">Prentice-Hall,</subfield>\n" +
            "        <subfield code=\"c\">c1982.</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\" \" ind2=\" \" tag=\"300\">\n" +
            "        <subfield code=\"a\">xiv, 577 p. :</subfield>\n" +
            "        <subfield code=\"b\">ill. ;</subfield>\n" +
            "        <subfield code=\"c\">24 cm.</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\" \" ind2=\" \" tag=\"504\">\n" +
            "        <subfield code=\"a\">Includes bibliographies and index.</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\" \" ind2=\"0\" tag=\"650\">\n" +
            "        <subfield code=\"a\">Historical geology.</subfield>\n" +
            "        <subfield code=\"0\">\n" +
            "            (uri)http://id.loc.gov/authorities/subjects/sh85061190\n" +
            "        </subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\" \" ind2=\" \" tag=\"998\">\n" +
            "        <subfield code=\"a\">03/15/82</subfield>\n" +
            "        <subfield code=\"s\">9110</subfield>\n" +
            "        <subfield code=\"n\">NjP</subfield>\n" +
            "        <subfield code=\"w\">DCLC818543B</subfield>\n" +
            "        <subfield code=\"d\">03/15/82</subfield>\n" +
            "        <subfield code=\"c\">ZG</subfield>\n" +
            "        <subfield code=\"b\">WZ</subfield>\n" +
            "        <subfield code=\"i\">820315</subfield>\n" +
            "        <subfield code=\"l\">NJPG</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\" \" ind2=\" \" tag=\"948\">\n" +
            "        <subfield code=\"a\">AACR2</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\" \" ind2=\" \" tag=\"911\">\n" +
            "        <subfield code=\"a\">19921028</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\" \" ind2=\" \" tag=\"912\">\n" +
            "        <subfield code=\"a\">19900820000000.0</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\" \" ind2=\" \" tag=\"959\">\n" +
            "        <subfield code=\"a\">2000-06-13 00:00:00 -0500</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\"0\" ind2=\"0\" tag=\"852\">\n" +
            "        <subfield code=\"0\">128532</subfield>\n" +
            "        <subfield code=\"b\">rcppa</subfield>\n" +
            "        <subfield code=\"h\">QE28.3 .S76 1982</subfield>\n" +
            "        <subfield code=\"t\">1</subfield>\n" +
            "        <subfield code=\"x\">tr fr sci</subfield>\n" +
            "    </datafield>\n" +
            "    <datafield ind1=\"0\" ind2=\"0\" tag=\"876\">\n" +
            "        <subfield code=\"0\">128532</subfield>\n" +
            "        <subfield code=\"a\">123431</subfield>\n" +
            "        <subfield code=\"h\"/>\n" +
            "        <subfield code=\"j\">Not Charged</subfield>\n" +
            "        <subfield code=\"p\">32101068878931</subfield>\n" +
            "        <subfield code=\"t\">1</subfield>\n" +
            "        <subfield code=\"x\">Shared</subfield>\n" +
            "        <subfield code=\"z\">PA</subfield>\n" +
            "    </datafield>\n" +
            "</record>\n" +
            "</collection>\n";

    @Test
    public void convert(){
        List<Record> recordList = marcUtil.convertMarcXmlToRecord(marcXmlContent);
        InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionCode("PUL");
        Map convertedMap = marcToBibEntityConverter.convert(recordList.get(0),institutionEntity);
        BibliographicEntity bibliographicEntity = (BibliographicEntity)convertedMap.get("bibliographicEntity");
        assertNotNull(bibliographicEntity);
        assertEquals("115115",bibliographicEntity.getOwningInstitutionBibId());
        assertEquals(new Integer(1),bibliographicEntity.getOwningInstitutionId());
        assertEquals("128532",bibliographicEntity.getHoldingsEntities().get(0).getOwningInstitutionHoldingsId());
        assertEquals("PA",bibliographicEntity.getItemEntities().get(0).getCustomerCode());
    }

    @Test
    public void testDataFieldValueStartsWith(){
        List<Record> recordList = marcUtil.convertMarcXmlToRecord(marcXmlContent);
        String response = marcUtil.getDataFieldValueStartsWith(recordList.get(0),"100");
        assertNotNull(response);
        assertEquals(response,"Stokes, William Lee, 1915-1994. (uri)http://id.loc.gov/authorities/names/n50011514");
    }

    @Test
    public void testDataFieldValueSubFieldValueStartsWith(){
        List<Record> recordList = marcUtil.convertMarcXmlToRecord(marcXmlContent);
        List<Character> subfieldTag = new ArrayList<>();
        subfieldTag.add('a');
        String response = marcUtil.getDataFieldValueStartsWith(recordList.get(0),"100",subfieldTag);
        assertNotNull(response);
        assertEquals(response,"Stokes, William Lee,");
    }

    @Test
    public void testListDataFieldValueSubFieldValueStartsWith(){
        List<Record> recordList = marcUtil.convertMarcXmlToRecord(marcXmlContent);
        List<Character> subfieldTag = new ArrayList<>();
        subfieldTag.add('a');
        subfieldTag.add('d');
        List<String> response = marcUtil.getListOfDataFieldValuesStartsWith(recordList.get(0),"100",subfieldTag);
        assertNotNull(response);
        assertEquals(response.size(),2);
    }

    @Test
    public void testGetDataFieldValue(){
        List<Record> recordList = marcUtil.convertMarcXmlToRecord(marcXmlContent);
        String response = marcUtil.getDataFieldValue(recordList.get(0),"100",null,null,"a");
        assertNotNull(response);
        Assert.assertEquals(response,"Stokes, William Lee,");
    }

    @Test
    public void testGetMultipleDataFieldValue(){
        List<Record> recordList = marcUtil.convertMarcXmlToRecord(marcXmlContent);
        List<String> response = marcUtil.getMultiDataFieldValues(recordList.get(0),"100",null,null,"a,d");
        assertNotNull(response);
        Assert.assertEquals(response.size(),2);
        Assert.assertEquals(response.get(0),"Stokes, William Lee,");
        Assert.assertEquals(response.get(1),"1915-1994.");
    }


}
