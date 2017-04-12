//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pkrete.jsip2.messages.requests;

import com.pkrete.jsip2.messages.SIP2MessageRequest;
import com.pkrete.jsip2.util.MessageUtil;
import com.pkrete.jsip2.variables.StatusCode;

/**
 * The type Sip 2 sc status request.
 */
public class SIP2SCStatusRequest extends SIP2MessageRequest {
    private StatusCode statusCode;
    private String maxPrintWidth;
    private String protocolVersion;

    /**
     * Instantiates a new Sip 2 sc status request.
     */
    public SIP2SCStatusRequest() {
        super("99");
        this.protocolVersion = "2.00";
        this.statusCode = StatusCode.OK;
        this.maxPrintWidth = "080";

    }

    /**
     * Instantiates a new Sip 2 sc status request.
     *
     * @param status the status
     */
    public SIP2SCStatusRequest(StatusCode status) {
        this();
        this.statusCode = status;
    }

    /**
     * Instantiates a new Sip 2 sc status request.
     *
     * @param status        the status
     * @param maxPrintWidth the max print width
     */
    public SIP2SCStatusRequest(StatusCode status, String maxPrintWidth) {
        this(status);
        this.maxPrintWidth = maxPrintWidth;
    }

    /**
     * Gets status code.
     *
     * @return the status code
     */
    public StatusCode getStatusCode() {
        return this.statusCode;
    }

    /**
     * Sets status code.
     *
     * @param statusCode the status code
     */
    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Gets max print width.
     *
     * @return the max print width
     */
    public String getMaxPrintWidth() {
        return this.maxPrintWidth;
    }

    /**
     * Sets max print width.
     *
     * @param maxPrintWidth the max print width
     */
    public void setMaxPrintWidth(String maxPrintWidth) {
        this.maxPrintWidth = maxPrintWidth;
    }

    /**
     *
     * @return
     */
    @Override
    public String getData() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.code);
        builder.append(this.statusCode);
        builder.append(this.maxPrintWidth);
        builder.append(this.protocolVersion);
        builder.append("Y");

        builder.append("|");
        if(errorDetectionEnabled) {
            builder.append("AY");
            builder.append(this.getSequence());
            builder.append("AZ");
            this.checkSum = MessageUtil.computeChecksum(builder.toString());
            builder.append(this.checkSum);
        }

        return builder.toString() + '\r';
    }
}
