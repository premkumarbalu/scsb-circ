package com.pkrete.jsip2.messages.responses;

import com.pkrete.jsip2.util.MessageUtil;
import com.pkrete.jsip2.util.StringUtil;

import java.util.Iterator;

/**
 * Created by sudhishk on 9/11/16.
 */
public class SIP2CreateBibResponse  extends SIP2CirculationTransactionResponse {

    /**
     * Instantiates a new Sip 2 create bib response.
     *
     * @param data the data
     */
    public SIP2CreateBibResponse(String data) {
        super("82", data);
    }

    @Override
    public String countChecksum() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.code);
        builder.append(StringUtil.bool2Int(this.ok));
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

        builder.append("AO");
        builder.append(this.institutionId);
        builder.append("|AA");
        builder.append(this.patronIdentifier);
        if(this.itemIdentifier != null) {
            builder.append("|AB");
            builder.append(this.itemIdentifier);
        }

        if(this.titleIdentifier != null) {
            builder.append("|AJ");
            builder.append(this.titleIdentifier);
        }

        if(this.bibId != null) {
            builder.append("|MA");
            builder.append(this.bibId);
        }

        Iterator i$ = this.screenMessage.iterator();

        String msg;
        while(i$.hasNext()) {
            msg = (String)i$.next();
            builder.append("|AF");
            builder.append(msg);
        }

        i$ = this.printLine.iterator();

        while(i$.hasNext()) {
            msg = (String)i$.next();
            builder.append("|AG");
            builder.append(msg);
        }

        builder.append("|");
        if(this.isSequence()) {
            builder.append("AY");
            builder.append(this.sequence);
        }

        builder.append("AZ");
        return MessageUtil.computeChecksum(builder.toString());
    }
}
