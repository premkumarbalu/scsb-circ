package com.pkrete.jsip2.messages.requests;

import com.pkrete.jsip2.util.MessageUtil;
import com.pkrete.jsip2.util.StringUtil;
import com.pkrete.jsip2.variables.HoldMode;
import com.pkrete.jsip2.variables.HoldType;

/**
 * Created by sudhishk on 9/11/16.
 */
public class SIP2RecallRequest extends SIP2CirculationTransactionRequest {
    private HoldMode holdMode;
    private HoldType holdType;

    private SIP2RecallRequest(){
        super("87");
        this.itemIdentifier = "";
        this.patronIdentifier ="";
        this.currentLocation = "";
        this.institutionId = "";
        this.titleIdentifier ="";
        this.terminalPassword = "";
    }

    /**
     * Instantiates a new Sip 2 recall request.
     *
     * @param staffIdentifier  the staff identifier
     * @param itemIdIdentifier the item id identifier
     */
    public SIP2RecallRequest(String staffIdentifier,  String itemIdIdentifier) {
        this();
        this.itemIdentifier = itemIdIdentifier;
        this.patronIdentifier=staffIdentifier;
    }

    /**
     * Instantiates a new Sip 2 recall request.
     *
     * @param staffIdentifier   the staff identifier
     * @param titleIdentififier the title identififier
     * @param itemIdIdentifier  the item id identifier
     * @param bibId             the bib id
     */
    public SIP2RecallRequest(String staffIdentifier, String titleIdentififier, String itemIdIdentifier, String bibId) {
        this();
        this.itemIdentifier = itemIdIdentifier;
        this.titleIdentifier =titleIdentififier;
        this.patronIdentifier=staffIdentifier;
        this.bibId=bibId;
    }

    /**
     * Gets hold mode.
     *
     * @return the hold mode
     */
    public HoldMode getHoldMode() {
        return this.holdMode;
    }

    /**
     * Sets hold mode.
     *
     * @param holdMode the hold mode
     */
    public void setHoldMode(HoldMode holdMode) {
        this.holdMode = holdMode;
    }

    /**
     * Gets hold type.
     *
     * @return the hold type
     */
    public HoldType getHoldType() {
        return this.holdType;
    }

    /**
     * Sets hold type.
     *
     * @param holdType the hold type
     */
    public void setHoldType(HoldType holdType) {
        this.holdType = holdType;
    }

    /**
     *
     * @return
     */
    @Override
    public String getData() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.code);
        builder.append(this.holdMode);
        builder.append(this.transactionDate);
        if(this.expirationDate != null) {
            builder.append("BW");
            builder.append(this.expirationDate);
            builder.append("|");
        }

        if(this.pickupLocation != null) {
            builder.append("BS");
            builder.append(this.pickupLocation);
            builder.append("|");
        }

        if(this.holdType != null) {
            builder.append("BY");
            builder.append(this.holdType);
            builder.append("|");
        }

        builder.append("AO");
        builder.append(this.institutionId);
        builder.append("|AA");
        builder.append(this.patronIdentifier);
        if(this.patronPassword != null) {
            builder.append("|AD");
            builder.append(this.patronPassword);
        }

        if(this.itemIdentifier != null) {
            builder.append("|AB");
            builder.append(this.itemIdentifier);
        }

        if(this.titleIdentifier != null) {
            builder.append("|AJ");
            builder.append(this.titleIdentifier);
        }

        if(this.terminalPassword != null) {
            builder.append("|AC");
            builder.append(this.terminalPassword);
        }

        if(this.useFeeAcknowledged) {
            builder.append("|BO");
            builder.append(StringUtil.bool2Char(this.feeAcknowledged));
        }

        if(this.bibId != null) {
            builder.append("|MA");
            builder.append(this.bibId);
        }

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
