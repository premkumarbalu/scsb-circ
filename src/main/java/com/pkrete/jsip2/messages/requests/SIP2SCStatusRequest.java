//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pkrete.jsip2.messages.requests;

import com.pkrete.jsip2.messages.SIP2MessageRequest;
import com.pkrete.jsip2.util.MessageUtil;
import com.pkrete.jsip2.variables.StatusCode;

public class SIP2SCStatusRequest extends SIP2MessageRequest {
    private StatusCode statusCode;
    private String maxPrintWidth;
    private String protocolVersion;

    public SIP2SCStatusRequest() {
        super("99");
        this.protocolVersion = "2.00";
        this.statusCode = StatusCode.OK;
        this.maxPrintWidth = "080";

    }

    public SIP2SCStatusRequest(StatusCode status) {
        this();
        this.statusCode = status;
    }

    public SIP2SCStatusRequest(StatusCode status, String maxPrintWidth) {
        this(status);
        this.maxPrintWidth = maxPrintWidth;
    }

    public StatusCode getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public String getMaxPrintWidth() {
        return this.maxPrintWidth;
    }

    public void setMaxPrintWidth(String maxPrintWidth) {
        this.maxPrintWidth = maxPrintWidth;
    }

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
