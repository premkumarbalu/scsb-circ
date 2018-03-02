package org.recap.service.submitcollection;

import junit.framework.TestCase;
import org.junit.Test;
import org.marc4j.MarcReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.model.BibliographicEntity;
import org.recap.model.HoldingsEntity;
import org.recap.model.ItemEntity;
import org.recap.model.submitcollection.SubmitCollectionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by premkb on 20/12/16.
 */
public class SubmitCollectionServiceUT extends BaseTestCase {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionServiceUT.class);

    @Autowired
    private SubmitCollectionService submitCollectionService;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${scsb.solr.client.url}")
    private String scsbSolrClientUrl;

    private String bibMarcContentForPUL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<collection>\n" +
            "   <record>\n" +
            "      <leader>01302cas a2200361 a 4500</leader>\n" +
            "      <controlfield tag=\"001\">202304</controlfield>\n" +
            "      <controlfield tag=\"005\">20160526232735.0</controlfield>\n" +
            "      <controlfield tag=\"008\">830323c19819999iluqx p   gv  0    0eng d</controlfield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"010\">\n" +
            "         <subfield code=\"a\">82640039</subfield>\n" +
            "         <subfield code=\"z\">81640039</subfield>\n" +
            "         <subfield code=\"z\">sn 81001329</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\"0\" ind2=\" \" tag=\"022\">\n" +
            "         <subfield code=\"a\">0276-9948</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "         <subfield code=\"a\">(OCoLC)7466281</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "         <subfield code=\"a\">(CStRLIN)NJPG83-S372</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "         <subfield code=\"9\">ABB7255TS-test</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"040\">\n" +
            "         <subfield code=\"a\">NSDP</subfield>\n" +
            "         <subfield code=\"d\">NjP</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"042\">\n" +
            "         <subfield code=\"a\">nsdp</subfield>\n" +
            "         <subfield code=\"a\">lc</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"043\">\n" +
            "         <subfield code=\"a\">n-us-il</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\"0\" ind2=\"0\" tag=\"050\">\n" +
            "         <subfield code=\"a\">K25</subfield>\n" +
            "         <subfield code=\"b\">.N63</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\"0\" tag=\"222\">\n" +
            "         <subfield code=\"a\">University of Illinois law review</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\"0\" ind2=\"0\" tag=\"245\">\n" +
            "         <subfield code=\"a\">University of Michigan.</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\"3\" ind2=\"0\" tag=\"246\">\n" +
            "         <subfield code=\"a\">Law review</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"260\">\n" +
            "         <subfield code=\"a\">Champaign, IL :</subfield>\n" +
            "         <subfield code=\"b\">University of Illinois at Urbana-Champaign, College of Law,</subfield>\n" +
            "         <subfield code=\"c\">c1981-</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"300\">\n" +
            "         <subfield code=\"a\">v. ;</subfield>\n" +
            "         <subfield code=\"c\">27 cm.</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"310\">\n" +
            "         <subfield code=\"a\">5 times a year,</subfield>\n" +
            "         <subfield code=\"b\">2001-&amp;lt;2013&amp;gt;</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"321\">\n" +
            "         <subfield code=\"a\">Quarterly,</subfield>\n" +
            "         <subfield code=\"b\">1981-2000</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\"0\" ind2=\" \" tag=\"362\">\n" +
            "         <subfield code=\"a\">Vol. 1981, no. 1-</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"588\">\n" +
            "         <subfield code=\"a\">Title from cover.</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"588\">\n" +
            "         <subfield code=\"a\">Latest issue consulted: Vol. 2013, no. 5.</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\"0\" tag=\"650\">\n" +
            "         <subfield code=\"a\">Law reviews</subfield>\n" +
            "         <subfield code=\"z\">Illinois.</subfield>\n" +
            "         <subfield code=\"0\">(uri)http://id.loc.gov/authorities/subjects/sh2009129243</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\"2\" ind2=\" \" tag=\"710\">\n" +
            "         <subfield code=\"a\">University of Illinois at Urbana-Champaign.</subfield>\n" +
            "         <subfield code=\"b\">College of Law.</subfield>\n" +
            "         <subfield code=\"0\">(uri)http://id.loc.gov/authorities/names/n50049213</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\"0\" ind2=\"0\" tag=\"780\">\n" +
            "         <subfield code=\"t\">University of Illinois law forum</subfield>\n" +
            "         <subfield code=\"x\">0041-963X</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"998\">\n" +
            "         <subfield code=\"a\">09/09/94</subfield>\n" +
            "         <subfield code=\"s\">9110</subfield>\n" +
            "         <subfield code=\"n\">NjP</subfield>\n" +
            "         <subfield code=\"w\">DCLC82640039S</subfield>\n" +
            "         <subfield code=\"d\">03/23/83</subfield>\n" +
            "         <subfield code=\"c\">DLJ</subfield>\n" +
            "         <subfield code=\"b\">SZF</subfield>\n" +
            "         <subfield code=\"i\">940909</subfield>\n" +
            "         <subfield code=\"l\">NJPG</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"911\">\n" +
            "         <subfield code=\"a\">19940916</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\" \" tag=\"912\">\n" +
            "         <subfield code=\"a\">19970731060735.8</subfield>\n" +
            "      </datafield>\n" +
            "   </record>\n" +
            "</collection>";

    private String holdingMarcContentForPUL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<collection>\n" +
            "   <record>\n" +
            "      <datafield ind1=\"0\" ind2=\"1\" tag=\"852\">\n" +
            "         <subfield code=\"b\">rcppa</subfield>\n" +
            "         <subfield code=\"h\">K25.xN6</subfield>\n" +
            "      </datafield>\n" +
            "      <datafield ind1=\" \" ind2=\"0\" tag=\"866\">\n" +
            "         <subfield code=\"z\">Subscription cancelled with the last issue of 2013</subfield>\n" +
            "      </datafield>\n" +
            "   </record>\n" +
            "</collection>";

    private String updatedMarcForPUL = "<collection xmlns=\"http://www.loc.gov/MARC21/slim\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\">\n" +
            "<record>\n" +
            "<leader>01302cas a2200361 a 4500</leader>\n" +
            "<controlfield tag=\"001\">202304</controlfield>\n" +
            "<controlfield tag=\"005\">20160526232735.0</controlfield>\n" +
            "<controlfield tag=\"008\">830323c19819999iluqx p gv 0 0eng d</controlfield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"010\">\n" +
            "<subfield code=\"a\">82640039</subfield>\n" +
            "<subfield code=\"z\">81640039</subfield>\n" +
            "<subfield code=\"z\">sn 81001329</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"0\" ind2=\" \" tag=\"022\">\n" +
            "<subfield code=\"a\">0276-9948</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "<subfield code=\"a\">(OCoLC)7466281</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "<subfield code=\"a\">(CStRLIN)NJPG83-S372</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "<subfield code=\"9\">ABB7255TS</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"040\">\n" +
            "<subfield code=\"a\">NSDP</subfield>\n" +
            "<subfield code=\"d\">NjP</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"042\">\n" +
            "<subfield code=\"a\">nsdp</subfield>\n" +
            "<subfield code=\"a\">lc</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"043\">\n" +
            "<subfield code=\"a\">n-us-il</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"0\" ind2=\"0\" tag=\"050\">\n" +
            "<subfield code=\"a\">K25</subfield>\n" +
            "<subfield code=\"b\">.N63</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\"0\" tag=\"222\">\n" +
            "<subfield code=\"a\">University of Illinois law review</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"0\" ind2=\"0\" tag=\"245\">\n" +
            "<subfield code=\"a\">University of Illinois law review.</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"3\" ind2=\"0\" tag=\"246\">\n" +
            "<subfield code=\"a\">Law review</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"260\">\n" +
            "<subfield code=\"a\">Champaign, IL :</subfield>\n" +
            "<subfield code=\"b\">\n" +
            "University of Illinois at Urbana-Champaign, College of Law,\n" +
            "</subfield>\n" +
            "<subfield code=\"c\">c1981-</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"300\">\n" +
            "<subfield code=\"a\">v. ;</subfield>\n" +
            "<subfield code=\"c\">27 cm.</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"310\">\n" +
            "<subfield code=\"a\">5 times a year,</subfield>\n" +
            "<subfield code=\"b\">2001-2013</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"321\">\n" +
            "<subfield code=\"a\">Quarterly,</subfield>\n" +
            "<subfield code=\"b\">1981-2000</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"0\" ind2=\" \" tag=\"362\">\n" +
            "<subfield code=\"a\">Vol. 1981, no. 1-</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"588\">\n" +
            "<subfield code=\"a\">Title from cover.</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"588\">\n" +
            "<subfield code=\"a\">Latest issue consulted: Vol. 2013, no. 5.</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\"0\" tag=\"650\">\n" +
            "<subfield code=\"a\">Law reviews</subfield>\n" +
            "<subfield code=\"z\">Illinois.</subfield>\n" +
            "<subfield code=\"0\">\n" +
            "(uri)http://id.loc.gov/authorities/subjects/sh2009129243\n" +
            "</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"2\" ind2=\" \" tag=\"710\">\n" +
            "<subfield code=\"a\">University of Illinois at Urbana-Champaign.</subfield>\n" +
            "<subfield code=\"b\">College of Law.</subfield>\n" +
            "<subfield code=\"0\">(uri)http://id.loc.gov/authorities/names/n50049213</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"0\" ind2=\"0\" tag=\"780\">\n" +
            "<subfield code=\"t\">University of Illinois law forum</subfield>\n" +
            "<subfield code=\"x\">0041-963X</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"998\">\n" +
            "<subfield code=\"a\">09/09/94</subfield>\n" +
            "<subfield code=\"s\">9110</subfield>\n" +
            "<subfield code=\"n\">NjP</subfield>\n" +
            "<subfield code=\"w\">DCLC82640039S</subfield>\n" +
            "<subfield code=\"d\">03/23/83</subfield>\n" +
            "<subfield code=\"c\">DLJ</subfield>\n" +
            "<subfield code=\"b\">SZF</subfield>\n" +
            "<subfield code=\"i\">940909</subfield>\n" +
            "<subfield code=\"l\">NJPG</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"911\">\n" +
            "<subfield code=\"a\">19940916</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"912\">\n" +
            "<subfield code=\"a\">19970731060735.0</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\"0\" tag=\"866\">\n" +
            "<subfield code=\"0\">222420</subfield>\n" +
            "<subfield code=\"a\">Vol. 1981, no. 1-v. 2013, no. 5</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\"0\" tag=\"866\">\n" +
            "<subfield code=\"0\">222420</subfield>\n" +
            "<subfield code=\"z\">LACKS: 2012, no. 1</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\"0\" tag=\"866\">\n" +
            "<subfield code=\"0\">222420</subfield>\n" +
            "<subfield code=\"x\">DESIGNATOR: year, no.</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\"0\" tag=\"866\">\n" +
            "<subfield code=\"0\">222420</subfield>\n" +
            "<subfield code=\"z\">Subscription cancelled with the last issue of 2013</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"959\">\n" +
            "<subfield code=\"a\">2000-06-13 00:00:00 -0500</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"0\" ind2=\"1\" tag=\"852\">\n" +
            "<subfield code=\"0\">222420</subfield>\n" +
            "<subfield code=\"b\">rcppa</subfield>\n" +
            "<subfield code=\"h\">K25 .xN5</subfield>\n" +
            "<subfield code=\"t\">1</subfield>\n" +
            "<subfield code=\"x\">tr fr f</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"0\" ind2=\"0\" tag=\"876\">\n" +
            "<subfield code=\"0\">222420</subfield>\n" +
            "<subfield code=\"a\">1110846</subfield>\n" +
            "<subfield code=\"h\"/>\n" +
            "<subfield code=\"j\">Not Charged</subfield>\n" +
            "<subfield code=\"p\">32101062128309</subfield>\n" +
            "<subfield code=\"t\">1</subfield>\n" +
            "<subfield code=\"x\">Open</subfield>\n" +
            "<subfield code=\"z\">PA</subfield>\n" +
            "</datafield>\n" +
            "</record>\n" +
            "</collection>";

    private String updatedMarcForPULWtihNewItem = "<collection xmlns=\"http://www.loc.gov/MARC21/slim\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\">\n" +
            "<record>\n" +
            "<leader>01302cas a2200361 a 4500</leader>\n" +
            "<controlfield tag=\"001\">202305</controlfield>\n" +
            "<controlfield tag=\"005\">20160526232735.0</controlfield>\n" +
            "<controlfield tag=\"008\">830323c19819999iluqx p gv 0 0eng d</controlfield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"010\">\n" +
            "<subfield code=\"a\">82640039</subfield>\n" +
            "<subfield code=\"z\">81640039</subfield>\n" +
            "<subfield code=\"z\">sn 81001329</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"0\" ind2=\" \" tag=\"022\">\n" +
            "<subfield code=\"a\">0276-9948</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "<subfield code=\"a\">(OCoLC)7466281</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "<subfield code=\"a\">(CStRLIN)NJPG83-S372</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "<subfield code=\"9\">ABB7255TS</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"040\">\n" +
            "<subfield code=\"a\">NSDP</subfield>\n" +
            "<subfield code=\"d\">NjP</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"042\">\n" +
            "<subfield code=\"a\">nsdp</subfield>\n" +
            "<subfield code=\"a\">lc</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"043\">\n" +
            "<subfield code=\"a\">n-us-il</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"0\" ind2=\"0\" tag=\"050\">\n" +
            "<subfield code=\"a\">K25</subfield>\n" +
            "<subfield code=\"b\">.N63</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\"0\" tag=\"222\">\n" +
            "<subfield code=\"a\">University of Illinois law review</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"0\" ind2=\"0\" tag=\"245\">\n" +
            "<subfield code=\"a\">University of Illinois law review.</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"3\" ind2=\"0\" tag=\"246\">\n" +
            "<subfield code=\"a\">Law review</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"260\">\n" +
            "<subfield code=\"a\">Champaign, IL :</subfield>\n" +
            "<subfield code=\"b\">\n" +
            "University of Illinois at Urbana-Champaign, College of Law,\n" +
            "</subfield>\n" +
            "<subfield code=\"c\">c1981-</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"300\">\n" +
            "<subfield code=\"a\">v. ;</subfield>\n" +
            "<subfield code=\"c\">27 cm.</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"310\">\n" +
            "<subfield code=\"a\">5 times a year,</subfield>\n" +
            "<subfield code=\"b\">2001-2013</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"321\">\n" +
            "<subfield code=\"a\">Quarterly,</subfield>\n" +
            "<subfield code=\"b\">1981-2000</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"0\" ind2=\" \" tag=\"362\">\n" +
            "<subfield code=\"a\">Vol. 1981, no. 1-</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"588\">\n" +
            "<subfield code=\"a\">Title from cover.</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"588\">\n" +
            "<subfield code=\"a\">Latest issue consulted: Vol. 2013, no. 5.</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\"0\" tag=\"650\">\n" +
            "<subfield code=\"a\">Law reviews</subfield>\n" +
            "<subfield code=\"z\">Illinois.</subfield>\n" +
            "<subfield code=\"0\">\n" +
            "(uri)http://id.loc.gov/authorities/subjects/sh2009129243\n" +
            "</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"2\" ind2=\" \" tag=\"710\">\n" +
            "<subfield code=\"a\">University of Illinois at Urbana-Champaign.</subfield>\n" +
            "<subfield code=\"b\">College of Law.</subfield>\n" +
            "<subfield code=\"0\">(uri)http://id.loc.gov/authorities/names/n50049213</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"0\" ind2=\"0\" tag=\"780\">\n" +
            "<subfield code=\"t\">University of Illinois law forum</subfield>\n" +
            "<subfield code=\"x\">0041-963X</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"998\">\n" +
            "<subfield code=\"a\">09/09/94</subfield>\n" +
            "<subfield code=\"s\">9110</subfield>\n" +
            "<subfield code=\"n\">NjP</subfield>\n" +
            "<subfield code=\"w\">DCLC82640039S</subfield>\n" +
            "<subfield code=\"d\">03/23/83</subfield>\n" +
            "<subfield code=\"c\">DLJ</subfield>\n" +
            "<subfield code=\"b\">SZF</subfield>\n" +
            "<subfield code=\"i\">940909</subfield>\n" +
            "<subfield code=\"l\">NJPG</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"911\">\n" +
            "<subfield code=\"a\">19940916</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"912\">\n" +
            "<subfield code=\"a\">19970731060735.0</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\"0\" tag=\"866\">\n" +
            "<subfield code=\"0\">222420</subfield>\n" +
            "<subfield code=\"a\">Vol. 1981, no. 1-v. 2013, no. 5</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\"0\" tag=\"866\">\n" +
            "<subfield code=\"0\">222420</subfield>\n" +
            "<subfield code=\"z\">LACKS: 2012, no. 1</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\"0\" tag=\"866\">\n" +
            "<subfield code=\"0\">222420</subfield>\n" +
            "<subfield code=\"x\">DESIGNATOR: year, no.</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\"0\" tag=\"866\">\n" +
            "<subfield code=\"0\">222420</subfield>\n" +
            "<subfield code=\"z\">Subscription cancelled with the last issue of 2013</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\" \" ind2=\" \" tag=\"959\">\n" +
            "<subfield code=\"a\">2000-06-13 00:00:00 -0500</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"0\" ind2=\"1\" tag=\"852\">\n" +
            "<subfield code=\"0\">222420</subfield>\n" +
            "<subfield code=\"b\">rcppa</subfield>\n" +
            "<subfield code=\"h\">K25 .xN5</subfield>\n" +
            "<subfield code=\"t\">1</subfield>\n" +
            "<subfield code=\"x\">tr fr f</subfield>\n" +
            "</datafield>\n" +
            "<datafield ind1=\"0\" ind2=\"0\" tag=\"876\">\n" +
            "<subfield code=\"0\">222420</subfield>\n" +
            "<subfield code=\"a\">1110847</subfield>\n" +
            "<subfield code=\"h\"/>\n" +
            "<subfield code=\"j\">Not Charged</subfield>\n" +
            "<subfield code=\"p\">32101barcode</subfield>\n" +
            "<subfield code=\"t\">1</subfield>\n" +
            "<subfield code=\"x\">Open</subfield>\n" +
            "<subfield code=\"z\">PA</subfield>\n" +
            "</datafield>\n" +
            "</record>\n" +
            "</collection>";

    private String bibMarcContentForNYPL1 =  "<collection xmlns=\"http://www.loc.gov/MARC21/slim\">\n" +
            "               <record>\n" +
            "                  <controlfield tag=\"001\">NYPG001000005-B</controlfield>\n" +
            "                  <controlfield tag=\"005\">20001116192418.8</controlfield>\n" +
            "                  <controlfield tag=\"008\">841106s1970 le b 000 0bara dcam i</controlfield>\n" +
            "                  <datafield ind1=\"1\" ind2=\" \" tag=\"100\">\n" +
            "                     <subfield code=\"a\">Ḥāwī, Īlīyā Salīm.</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\"8\" ind2=\" \" tag=\"952\">\n" +
            "                     <subfield code=\"h\">*OFS 84-1997</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\"1\" ind2=\"0\" tag=\"600\">\n" +
            "                     <subfield code=\"a\">Ḥuṭayʼah, Jarwal ibn Aws,</subfield>\n" +
            "                     <subfield code=\"d\">d. 650?</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\" \" ind2=\" \" tag=\"504\">\n" +
            "                     <subfield code=\"a\">Bibliography: p.221.</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\" \" ind2=\" \" tag=\"546\">\n" +
            "                     <subfield code=\"a\">In English.</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\" \" ind2=\" \" tag=\"260\">\n" +
            "                     <subfield code=\"a\">Bayrūt :</subfield>\n" +
            "                     <subfield code=\"b\">Dār al-Thaqāfah,</subfield>\n" +
            "                     <subfield code=\"c\">1970.</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\"8\" ind2=\" \" tag=\"952\">\n" +
            "                     <subfield code=\"h\">*OFS 84-1997</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\" \" ind2=\" \" tag=\"300\">\n" +
            "                     <subfield code=\"a\">223 p. ;</subfield>\n" +
            "                     <subfield code=\"c\">25cm.</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\"1\" ind2=\"3\" tag=\"245\">\n" +
            "                     <subfield code=\"a\">al-Ḥuṭayʼah :</subfield>\n" +
            "                     <subfield code=\"b\">fī sīratihi wa-nafsīyatihi wa-shiʻrihi /</subfield>\n" +
            "                     <subfield code=\"c\">bi-qalam Īlīyā Ḥāwī.</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\" \" ind2=\" \" tag=\"959\">\n" +
            "                     <subfield code=\"a\">.b10000136</subfield>\n" +
            "                     <subfield code=\"b\">07-18-08</subfield>\n" +
            "                     <subfield code=\"c\">07-29-91</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\" \" ind2=\" \" tag=\"040\">\n" +
            "                     <subfield code=\"a\">NN</subfield>\n" +
            "                     <subfield code=\"c\">NN</subfield>\n" +
            "                     <subfield code=\"d\">WaOLN</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\" \" ind2=\" \" tag=\"997\">\n" +
            "                     <subfield code=\"a\">ho</subfield>\n" +
            "                     <subfield code=\"b\">12-15-00</subfield>\n" +
            "                     <subfield code=\"c\">m</subfield>\n" +
            "                     <subfield code=\"d\">a</subfield>\n" +
            "                     <subfield code=\"e\">-</subfield>\n" +
            "                     <subfield code=\"f\">ara</subfield>\n" +
            "                     <subfield code=\"g\">le</subfield>\n" +
            "                     <subfield code=\"h\">3</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\"0\" ind2=\"0\" tag=\"907\">\n" +
            "                     <subfield code=\"a\">.b100000125</subfield>\n" +
            "                  </datafield>\n" +
            "                  <leader>00000cam 2200217 i 4500</leader>\n" +
            "               </record>\n" +
            "            </collection>\n" ;

    private String holdingContentForNYPL1 =             "               <collection xmlns=\"http://www.loc.gov/MARC21/slim\">\n" +
            "                  <record>\n" +
            "                     <datafield ind1=\"8\" ind2=\" \" tag=\"852\">\n" +
            "                        <subfield code=\"b\">rcma2</subfield>\n" +
            "                        <subfield code=\"h\">*OFS 84-1998</subfield>\n" +
            "                     </datafield>\n" +
            "                     <datafield ind1=\" \" ind2=\" \" tag=\"866\">\n" +
            "                        <subfield code=\"a\" />\n" +
            "                     </datafield>\n" +
            "                  </record>\n" +
            "               </collection>\n";

    private String updatedContentForNYPL1 ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<bibRecords>\n" +
            "   <bibRecord>\n" +
            "      <bib>\n" +
            "         <owningInstitutionId>NYPL</owningInstitutionId>\n" +
            "         <owningInstitutionBibId>.b100000125</owningInstitutionBibId>\n" +
            "         <content>\n" +
            "            <collection xmlns=\"http://www.loc.gov/MARC21/slim\">\n" +
            "               <record>\n" +
            "                  <controlfield tag=\"001\">NYPG001000005-B</controlfield>\n" +
            "                  <controlfield tag=\"005\">20001116192418.8</controlfield>\n" +
            "                  <controlfield tag=\"008\">841106s1970 le b 000 0bara dcam i</controlfield>\n" +
            "                  <datafield ind1=\"1\" ind2=\" \" tag=\"100\">\n" +
            "                     <subfield code=\"a\">Ḥāwī, Īlīyā Salīm.</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\"8\" ind2=\" \" tag=\"952\">\n" +
            "                     <subfield code=\"h\">*OFS 84-1997</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\"1\" ind2=\"0\" tag=\"600\">\n" +
            "                     <subfield code=\"a\">Ḥuṭayʼah, Jarwal ibn Aws,</subfield>\n" +
            "                     <subfield code=\"d\">d. 650?</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\" \" ind2=\" \" tag=\"504\">\n" +
            "                     <subfield code=\"a\">Bibliography: p.221.</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\" \" ind2=\" \" tag=\"546\">\n" +
            "                     <subfield code=\"a\">In Arabic.</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\" \" ind2=\" \" tag=\"260\">\n" +
            "                     <subfield code=\"a\">Bayrūt :</subfield>\n" +
            "                     <subfield code=\"b\">Dār al-Thaqāfah,</subfield>\n" +
            "                     <subfield code=\"c\">1970.</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\"8\" ind2=\" \" tag=\"952\">\n" +
            "                     <subfield code=\"h\">*OFS 84-1997</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\" \" ind2=\" \" tag=\"300\">\n" +
            "                     <subfield code=\"a\">223 p. ;</subfield>\n" +
            "                     <subfield code=\"c\">25cm.</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\"1\" ind2=\"3\" tag=\"245\">\n" +
            "                     <subfield code=\"a\">al-Ḥuṭayʼah :</subfield>\n" +
            "                     <subfield code=\"b\">fī sīratihi wa-nafsīyatihi wa-shiʻrihi /</subfield>\n" +
            "                     <subfield code=\"c\">bi-qalam Īlīyā Ḥāwī.</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\" \" ind2=\" \" tag=\"959\">\n" +
            "                     <subfield code=\"a\">.b10000136</subfield>\n" +
            "                     <subfield code=\"b\">07-18-08</subfield>\n" +
            "                     <subfield code=\"c\">07-29-91</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\" \" ind2=\" \" tag=\"040\">\n" +
            "                     <subfield code=\"a\">NN</subfield>\n" +
            "                     <subfield code=\"c\">NN</subfield>\n" +
            "                     <subfield code=\"d\">WaOLN</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\" \" ind2=\" \" tag=\"997\">\n" +
            "                     <subfield code=\"a\">ho</subfield>\n" +
            "                     <subfield code=\"b\">12-15-00</subfield>\n" +
            "                     <subfield code=\"c\">m</subfield>\n" +
            "                     <subfield code=\"d\">a</subfield>\n" +
            "                     <subfield code=\"e\">-</subfield>\n" +
            "                     <subfield code=\"f\">ara</subfield>\n" +
            "                     <subfield code=\"g\">le</subfield>\n" +
            "                     <subfield code=\"h\">3</subfield>\n" +
            "                  </datafield>\n" +
            "                  <datafield ind1=\"0\" ind2=\"0\" tag=\"907\">\n" +
            "                     <subfield code=\"a\">.b100000125</subfield>\n" +
            "                  </datafield>\n" +
            "                  <leader>00000cam a2200217 i 4500</leader>\n" +
            "               </record>\n" +
            "            </collection>\n" +
            "         </content>\n" +
            "      </bib>\n" +
            "      <holdings>\n" +
            "         <holding>\n" +
            "            <owningInstitutionHoldingsId />\n" +
            "            <content>\n" +
            "               <collection xmlns=\"http://www.loc.gov/MARC21/slim\">\n" +
            "                  <record>\n" +
            "                     <datafield ind1=\"8\" ind2=\" \" tag=\"852\">\n" +
            "                        <subfield code=\"b\">rcma2</subfield>\n" +
            "                        <subfield code=\"h\">*OFS 84-1997</subfield>\n" +
            "                     </datafield>\n" +
            "                     <datafield ind1=\" \" ind2=\" \" tag=\"866\">\n" +
            "                        <subfield code=\"a\" />\n" +
            "                     </datafield>\n" +
            "                  </record>\n" +
            "               </collection>\n" +
            "            </content>\n" +
            "            <items>\n" +
            "               <content>\n" +
            "                  <collection xmlns=\"http://www.loc.gov/MARC21/slim\">\n" +
            "                     <record>\n" +
            "                        <datafield ind1=\" \" ind2=\" \" tag=\"876\">\n" +
            "                           <subfield code=\"p\">33433014514719</subfield>\n" +
            "                           <subfield code=\"h\">In Library Use</subfield>\n" +
            "                           <subfield code=\"a\">.i100000034</subfield>\n" +
            "                           <subfield code=\"j\">Available</subfield>\n" +
            "                           <subfield code=\"t\">1</subfield>\n" +
            "                        </datafield>\n" +
            "                        <datafield ind1=\" \" ind2=\" \" tag=\"900\">\n" +
            "                           <subfield code=\"a\">Open</subfield>\n" +
            "                           <subfield code=\"b\">NA</subfield>\n" +
            "                        </datafield>\n" +
            "                     </record>\n" +
            "                  </collection>\n" +
            "               </content>\n" +
            "            </items>\n" +
            "         </holding>\n" +
            "      </holdings>\n" +
            "   </bibRecord>\n" +
            "</bibRecords>";

    private String dummyBibMarcContent = "<collection>\n" +
            "    <record>\n" +
            "        <leader>01893cam a2200361 a 4500</leader>\n" +
            "        <controlfield tag=\"001\">Dummy</controlfield>\n" +
            "        <controlfield tag=\"005\">dummydummydumm.y</controlfield>\n" +
            "        <controlfield tag=\"008\">dummydummydummydummydummydummydummydummy</controlfield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"010\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"020\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"020\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "            <subfield code=\"a\">(OCoLC)Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"040\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"042\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"043\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\"0\" ind2=\"0\" tag=\"050\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "            <subfield code=\"b\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\"0\" ind2=\"0\" tag=\"245\">\n" +
            "            <subfield code=\"a\">Dummy Title</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"260\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"300\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"500\">\n" +
            "            <subfield code=\"a\">\"Dummy\"</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"500\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"504\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\"2\" ind2=\"0\" tag=\"610\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "            <subfield code=\"x\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\"2\" ind2=\"0\" tag=\"610\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "            <subfield code=\"x\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\"0\" tag=\"650\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "            <subfield code=\"z\">Dummy</subfield>\n" +
            "            <subfield code=\"x\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\"1\" ind2=\" \" tag=\"700\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\"2\" ind2=\" \" tag=\"710\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "            <subfield code=\"0\">(uri)http://id.loc.gov/authorities/names/no</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"999\">\n" +
            "            <subfield code=\"a\">Dummy</subfield>\n" +
            "        </datafield>\n" +
            "    </record>\n" +
            "</collection>";

    private String dummyHoldingMarcContent = "<collection>\n" +
            "    <record>\n" +
            "        <datafield ind1=\"0\" ind2=\"0\" tag=\"852\">\n" +
            "            <subfield code=\"b\">dummy</subfield>\n" +
            "            <subfield code=\"h\">dummydummydummy</subfield>\n" +
            "        </datafield>\n" +
            "    </record>\n" +
            "</collection>";
    @Test
    public void processForPUL() throws JAXBException {
        BibliographicEntity savedBibliographicEntity = getBibliographicEntity(1,"202304","222420","1110846",1,"32101062128309",bibMarcContentForPUL,holdingMarcContentForPUL, ReCAPConstants.INCOMPLETE_STATUS);
        Set<Integer> processedBibIds = new HashSet<>();
        Map<String,String> idMapToRemoveIndex = new HashMap<>();
        Map<String,String> bibIdMapToRemoveIndex = new HashMap<>();
        List<Integer> reportRecordNumList = new ArrayList<>();
        List<SubmitCollectionResponse>  submitCollectionResponseList = submitCollectionService.process("PUL",updatedMarcForPUL,processedBibIds,Arrays.asList(idMapToRemoveIndex), Arrays.asList(bibIdMapToRemoveIndex),ReCAPConstants.REST,reportRecordNumList, true,false,null);
        String response = submitCollectionResponseList.get(0).getMessage();
        assertEquals(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_RECORD,response);
        List<BibliographicEntity> fetchedBibliographicEntityList = bibliographicDetailsRepository.findByOwningInstitutionBibId("202304");
        String updatedBibMarcXML = new String(fetchedBibliographicEntityList.get(0).getContent(), StandardCharsets.UTF_8);
        List<Record> bibRecordList = readMarcXml(updatedBibMarcXML);
        assertNotNull(bibRecordList);
        DataField field912 = (DataField)bibRecordList.get(0).getVariableField("912");
        assertEquals("19970731060735.0", field912.getSubfield('a').getData());
        HoldingsEntity holdingsEntity = fetchedBibliographicEntityList.get(0).getHoldingsEntities().get(0);
        String updatedHoldingMarcXML = new String(holdingsEntity.getContent(),StandardCharsets.UTF_8);
        List<Record> holdingRecordList = readMarcXml(updatedHoldingMarcXML);
        logger.info("updatedHoldingMarcXML-->"+updatedHoldingMarcXML);
        TestCase.assertNotNull(holdingRecordList);
        DataField field852 = (DataField)holdingRecordList.get(0).getVariableField("852");
        assertEquals("K25 .xN5", field852.getSubfield('h').getData());
        String callNumber = fetchedBibliographicEntityList.get(0).getItemEntities().get(0).getCallNumber();
        assertEquals("K25 .xN5",callNumber);
        Integer collectionGroupId = fetchedBibliographicEntityList.get(0).getItemEntities().get(0).getCollectionGroupId();
        assertEquals(new Integer(2),collectionGroupId);
    }

    @Test
    public void processForPULUpdateIncompleteRecord() throws JAXBException {
        BibliographicEntity savedBibliographicEntity = getBibliographicEntity(1,"202304","222420","1110846",1,"32101062128309",dummyBibMarcContent,dummyHoldingMarcContent, ReCAPConstants.INCOMPLETE_STATUS);
        Set<Integer> processedBibIds = new HashSet<>();
        Map<String,String> idMapToRemoveIndex = new HashMap<>();
        Map<String,String> bibIdMapToRemoveIndex = new HashMap<>();
        List<Integer> reportRecordNumList = new ArrayList<>();
        List<SubmitCollectionResponse>  submitCollectionResponseList = submitCollectionService.process("PUL",updatedMarcForPUL,processedBibIds,Arrays.asList(idMapToRemoveIndex),Arrays.asList(bibIdMapToRemoveIndex), ReCAPConstants.REST,reportRecordNumList, true,false,null);
        String response = submitCollectionResponseList.get(0).getMessage();
        assertEquals(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_RECORD,response);
        List<BibliographicEntity> fetchedBibliographicEntityList = bibliographicDetailsRepository.findByOwningInstitutionBibId("202304");
        String updatedBibMarcXML = new String(fetchedBibliographicEntityList.get(0).getContent(), StandardCharsets.UTF_8);
        List<Record> bibRecordList = readMarcXml(updatedBibMarcXML);
        assertNotNull(bibRecordList);
        DataField field912 = (DataField)bibRecordList.get(0).getVariableField("912");
        assertEquals("19970731060735.0", field912.getSubfield('a').getData());
        HoldingsEntity holdingsEntity = fetchedBibliographicEntityList.get(0).getHoldingsEntities().get(0);
        String updatedHoldingMarcXML = new String(holdingsEntity.getContent(),StandardCharsets.UTF_8);
        List<Record> holdingRecordList = readMarcXml(updatedHoldingMarcXML);
        logger.info("updatedHoldingMarcXML-->"+updatedHoldingMarcXML);
        TestCase.assertNotNull(holdingRecordList);
        DataField field852 = (DataField)holdingRecordList.get(0).getVariableField("852");
        assertEquals("K25 .xN5", field852.getSubfield('h').getData());
        String callNumber = fetchedBibliographicEntityList.get(0).getItemEntities().get(0).getCallNumber();
        assertEquals("K25 .xN5",callNumber);
        Integer collectionGroupId = fetchedBibliographicEntityList.get(0).getItemEntities().get(0).getCollectionGroupId();
        assertEquals(new Integer(2),collectionGroupId);
        String catalogingStatus = fetchedBibliographicEntityList.get(0).getItemEntities().get(0).getCatalogingStatus();
        assertEquals("Complete",catalogingStatus);
    }

    @Test
    public void processForPULExceptionRecordForExisitinBibNewItem() throws JAXBException {
        BibliographicEntity savedBibliographicEntity = getBibliographicEntity(1,"202304","222420","1110846",1,"32101062128309",bibMarcContentForPUL,holdingMarcContentForPUL, ReCAPConstants.INCOMPLETE_STATUS);
        Set<Integer> processedBibIds = new HashSet<>();
        Map<String,String> idMapToRemoveIndex = new HashMap<>();
        Map<String,String> bibIdMapToRemoveIndex = new HashMap<>();
        List<Integer> reportRecordNumList = new ArrayList<>();
        List<SubmitCollectionResponse>  submitCollectionResponseList = submitCollectionService.process("PUL",updatedMarcForPULWtihNewItem,processedBibIds,Arrays.asList(idMapToRemoveIndex),Arrays.asList(bibIdMapToRemoveIndex), ReCAPConstants.REST,reportRecordNumList, true,false,null);
        String response = submitCollectionResponseList.get(0).getMessage();
        assertEquals(ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_RECORD,response);
        List<BibliographicEntity> fetchedBibliographicEntityList = bibliographicDetailsRepository.findByOwningInstitutionBibId("202304");
        String updatedBibMarcXML = new String(fetchedBibliographicEntityList.get(0).getContent(), StandardCharsets.UTF_8);
        List<Record> bibRecordList = readMarcXml(updatedBibMarcXML);
        assertNotNull(bibRecordList);
        assertNotEquals(new Integer(2),new Integer(fetchedBibliographicEntityList.get(0).getItemEntities().size()));
        assertEquals(new Integer(1),new Integer(fetchedBibliographicEntityList.get(0).getItemEntities().size()));
    }

    @Test
    public void processForPULRejectedRecord() throws JAXBException {
        BibliographicEntity savedBibliographicEntity = getBibliographicEntity(1,"202304","222420","1110846",2,"32101062128309",bibMarcContentForPUL,holdingMarcContentForPUL, ReCAPConstants.INCOMPLETE_STATUS);
        Set<Integer> processedBibIds = new HashSet<>();
        Map<String,String> idMapToRemoveIndex = new HashMap<>();
        Map<String,String> bibIdMapToRemoveIndex = new HashMap<>();
        List<Integer> reportRecordNumList = new ArrayList<>();
        List<SubmitCollectionResponse>  submitCollectionResponseList = submitCollectionService.process("PUL",updatedMarcForPUL,processedBibIds,Arrays.asList(idMapToRemoveIndex),Arrays.asList(bibIdMapToRemoveIndex), ReCAPConstants.REST,reportRecordNumList, true,false,null);
        String response = submitCollectionResponseList.get(0).getMessage();
        assertEquals(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_RECORD,response);
        List<BibliographicEntity> fetchedBibliographicEntityList = bibliographicDetailsRepository.findByOwningInstitutionBibId("202304");
        String updatedBibMarcXML = new String(fetchedBibliographicEntityList.get(0).getContent(), StandardCharsets.UTF_8);
        List<Record> bibRecordList = readMarcXml(updatedBibMarcXML);
        assertNotNull(bibRecordList);
        DataField field912 = (DataField)bibRecordList.get(0).getVariableField("912");
        assertEquals("19970731060735.0", field912.getSubfield('a').getData());
        HoldingsEntity holdingsEntity = fetchedBibliographicEntityList.get(0).getHoldingsEntities().get(0);
        String updatedHoldingMarcXML = new String(holdingsEntity.getContent(),StandardCharsets.UTF_8);
        List<Record> holdingRecordList = readMarcXml(updatedHoldingMarcXML);
        logger.info("updatedHoldingMarcXML-->"+updatedHoldingMarcXML);
        TestCase.assertNotNull(holdingRecordList);
        DataField field852 = (DataField)holdingRecordList.get(0).getVariableField("852");
        assertEquals("K25 .xN5", field852.getSubfield('h').getData());
        String callNumber = fetchedBibliographicEntityList.get(0).getItemEntities().get(0).getCallNumber();
        assertEquals("K25 .xN5",callNumber);
        Integer collectionGroupId = fetchedBibliographicEntityList.get(0).getItemEntities().get(0).getCollectionGroupId();
        assertEquals(new Integer(1),collectionGroupId);
    }

    @Test
    public void processForNYPL(){
        BibliographicEntity savedBibliographicEntity = getBibliographicEntity(3,".b100000125","123",".i100000034",1,"33433014514719",bibMarcContentForNYPL1,holdingContentForNYPL1, ReCAPConstants.INCOMPLETE_STATUS);
        String originalXML = new String(savedBibliographicEntity.getContent());
        Set<Integer> processedBibIds = new HashSet<>();
        Map<String,String> idMapToRemoveIndex = new HashMap<>();
        Map<String,String> bibIdMapToRemoveIndex = new HashMap<>();
        List<Integer> reportRecordNumList = new ArrayList<>();
        List<SubmitCollectionResponse>  submitCollectionResponseList = submitCollectionService.process("NYPL",updatedContentForNYPL1,processedBibIds,Arrays.asList(idMapToRemoveIndex),Arrays.asList(bibIdMapToRemoveIndex), ReCAPConstants.REST,reportRecordNumList, true,false,null);
        String response = submitCollectionResponseList.get(0).getMessage();
        List<BibliographicEntity> fetchedBibliographicEntityList = bibliographicDetailsRepository.findByOwningInstitutionBibId(".b100000125");
        String updatedBibMarcXML = new String(fetchedBibliographicEntityList.get(0).getContent(), StandardCharsets.UTF_8);
        List<Record> bibRecordList = readMarcXml(updatedBibMarcXML);
        assertNotNull(bibRecordList);
        DataField field912 = (DataField)bibRecordList.get(0).getVariableField("546");
        assertEquals("In Arabic.", field912.getSubfield('a').getData());
        HoldingsEntity holdingsEntity = fetchedBibliographicEntityList.get(0).getHoldingsEntities().get(0);
        String updatedHoldingMarcXML = new String(holdingsEntity.getContent(),StandardCharsets.UTF_8);
        List<Record> holdingRecordList = readMarcXml(updatedHoldingMarcXML);
        logger.info("updatedHoldingMarcXML-->"+updatedHoldingMarcXML);
        assertNotNull(holdingRecordList);
        String callNumber = fetchedBibliographicEntityList.get(0).getItemEntities().get(0).getCallNumber();
        assertEquals("*OFS 84-1997",callNumber);
        DataField field852 = (DataField)holdingRecordList.get(0).getVariableField("852");
        assertEquals("*OFS 84-1997", field852.getSubfield('h').getData());
    }

    private BibliographicEntity getBibliographicEntity(Integer owningInstitutionId, String owningInstitutionBibId, String owningInstitutionHoldingsId,
                                                       String owningInstitutionItemId, Integer itemAvailabilityStatusId, String itemBarcode, String bibMarcContent , String holdingMarcContent, String catalogingStatus){
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent(bibMarcContent.getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("etl");
        bibliographicEntity.setLastUpdatedBy("etl");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionBibId(owningInstitutionBibId);
        bibliographicEntity.setOwningInstitutionId(owningInstitutionId);
        bibliographicEntity.setCatalogingStatus(catalogingStatus);

        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent(holdingMarcContent.getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("etl");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setOwningInstitutionId(owningInstitutionId);
        holdingsEntity.setLastUpdatedBy("etl");

        holdingsEntity.setOwningInstitutionHoldingsId(owningInstitutionHoldingsId);

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setCallNumberType("0");
        itemEntity.setCallNumber("K25.xN5888888888");
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("submitCollection");
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setLastUpdatedBy("submitCollection");
        itemEntity.setBarcode(itemBarcode);
        itemEntity.setOwningInstitutionItemId(owningInstitutionItemId);
        itemEntity.setOwningInstitutionId(owningInstitutionId);
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCustomerCode("PA");
        itemEntity.setItemAvailabilityStatusId(itemAvailabilityStatusId);
        itemEntity.setDeleted(false);
        itemEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        itemEntity.setCatalogingStatus(catalogingStatus);
        List<ItemEntity> itemEntityList = new ArrayList<>();
        itemEntityList.add(itemEntity);
        holdingsEntity.setItemEntities(itemEntityList);

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);
        return savedBibliographicEntity;
    }
    private List<Record> readMarcXml(String marcXmlString) {
        List<Record> recordList = new ArrayList<>();
        InputStream in = new ByteArrayInputStream(marcXmlString.getBytes());
        MarcReader reader = new MarcXmlReader(in);
        while (reader.hasNext()) {
            Record record = reader.next();
            recordList.add(record);
            logger.info(record.toString());
        }
        return recordList;
    }
}
