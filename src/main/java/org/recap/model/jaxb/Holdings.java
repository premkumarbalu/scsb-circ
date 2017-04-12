package org.recap.model.jaxb;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class Holdings implements Serializable {
    private List<Holding> holding;

    /**
     * Gets holding.
     *
     * @return the holding
     */
    @XmlElement
    public List<Holding> getHolding() {
        return holding;
    }

    /**
     * Sets holding.
     *
     * @param holding the holding
     */
    public void setHolding(List<Holding> holding) {
        this.holding = holding;
    }
}
