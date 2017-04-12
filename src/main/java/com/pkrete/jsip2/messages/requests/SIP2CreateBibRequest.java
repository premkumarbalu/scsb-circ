package com.pkrete.jsip2.messages.requests;

import com.pkrete.jsip2.util.MessageUtil;
import com.pkrete.jsip2.util.StringUtil;

/**
 * Created by sudhishk on 9/11/16.
 */
public class SIP2CreateBibRequest extends SIP2CirculationTransactionRequest {

    private SIP2CreateBibRequest(){
        super("81");
        this.itemIdentifier = "";
        this.patronIdentifier ="";
        this.currentLocation = "";
        this.institutionId = "";
        this.titleIdentifier ="";
        this.terminalPassword = "";
    }

    /**
     * Instantiates a new Sip 2 create bib request.
     *
     * @param staffIdentifier   the staff identifier
     * @param titleIdentififier the title identififier
     * @param itemIdIdentifier  the item id identifier
     */
    public SIP2CreateBibRequest(String staffIdentifier,String titleIdentififier, String itemIdIdentifier) {
        this();
        this.itemIdentifier = itemIdIdentifier;
        this.titleIdentifier =titleIdentififier;
        this.patronIdentifier=staffIdentifier;
    }

    /**
     * Instantiates a new Sip 2 create bib request.
     *
     * @param staffIdentifier   the staff identifier
     * @param titleIdentififier the title identififier
     * @param itemIdIdentifier  the item id identifier
     * @param bibId             the bib id
     */
    public SIP2CreateBibRequest(String staffIdentifier,String titleIdentififier, String itemIdIdentifier,String bibId) {
        this();
        this.itemIdentifier = itemIdIdentifier;
        this.titleIdentifier =titleIdentififier;
        this.patronIdentifier=staffIdentifier;
        this.bibId=bibId;
    }

    /**
     *
     * @return
     */
    @Override
    public String getData() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.code);
        builder.append(StringUtil.bool2Char(this.noBlock));
        builder.append(this.transactionDate);
        builder.append("|AO");
        builder.append(this.institutionId);
        builder.append("|AA");
        builder.append(this.patronIdentifier);
        builder.append("AP");
        builder.append(this.currentLocation);
        builder.append("|AB");
        builder.append(this.itemIdentifier);
        builder.append("|AC");
        builder.append(this.terminalPassword);
        if (titleIdentifier != null) {
            builder.append("|AJ");
            builder.append(titleIdentifier);
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
