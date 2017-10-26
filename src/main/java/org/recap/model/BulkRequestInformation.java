package org.recap.model;

/**
 * Created by rajeshbabuk on 11/10/17.
 */
public class BulkRequestInformation {

    private String requestingInstitution;
    private String patronBarcode;

    public String getRequestingInstitution() {
        return requestingInstitution;
    }

    public void setRequestingInstitution(String requestingInstitution) {
        this.requestingInstitution = requestingInstitution;
    }

    public String getPatronBarcode() {
        return patronBarcode;
    }

    public void setPatronBarcode(String patronBarcode) {
        this.patronBarcode = patronBarcode;
    }
}
