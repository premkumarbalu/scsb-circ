package org.recap.model.jaxb.marc;

import org.recap.model.jaxb.BibRecord;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Created by chenchulakshmig on 5/8/16.
 */
@XmlRootElement
public class BibRecords implements Serializable {

    private List<BibRecord> bibRecords;

    /**
     * Gets bib records.
     *
     * @return the bib records
     */
    @XmlElement(name = "bibRecord")
    public List<BibRecord> getBibRecords() {
        return bibRecords;
    }

    /**
     * Sets bib records.
     *
     * @param bibRecords the bib records
     */
    public void setBibRecords(List<BibRecord> bibRecords) {
        this.bibRecords = bibRecords;
    }
}
