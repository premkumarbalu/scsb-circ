package org.recap.ils.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Created by pvsubrah on 6/21/16.
 */

@XmlRootElement
public class BibRecord implements Serializable{

    private Bib bib;
    private List<Holdings> holdings;

    @XmlElement
    public Bib getBib() {
        return bib;
    }

    public void setBib(Bib bib) {
        this.bib = bib;
    }

    @XmlElement
    public List<Holdings> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<Holdings> holdings) {
        this.holdings = holdings;
    }

}
