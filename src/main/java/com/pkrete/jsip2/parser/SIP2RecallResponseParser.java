package com.pkrete.jsip2.parser;

import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseException;
import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseValueException;
import com.pkrete.jsip2.messages.SIP2MessageResponse;
import com.pkrete.jsip2.messages.responses.SIP2RecallResponse;
import org.recap.ReCAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sudhishk on 9/11/16.
 */
public class SIP2RecallResponseParser extends  SIP2ResponseParser{

    private static final Logger logger = LoggerFactory.getLogger(SIP2RecallResponseParser.class);

    @Override
    public SIP2MessageResponse parse(String data) throws InvalidSIP2ResponseValueException, InvalidSIP2ResponseException {
        SIP2RecallResponse response= new SIP2RecallResponse(data);
        try {
            response.setOk(this.intToBool(data.charAt(2)));
            response.setTransactionDate(data.substring(4, 22));
            response.setExpirationDate(this.parseVariableWithoutDelimiter("BW", data.substring(22), false));
            response.setPickupLocation(this.parseVariableWithoutDelimiter("BS", data.substring(22), false));
            response.setInstitutionId(this.parseVariableWithoutDelimiter("AO", data.substring(22)));
            response.setPatronIdentifier(this.parseVariable("AA", data.substring(22)));
            response.setItemIdentifier(this.parseVariable("AB", data.substring(22), false));
            response.setTitleIdentifier(this.parseVariable("AJ", data.substring(22), false));
            response.setBibId(this.parseVariable("MA", data.substring(22), false));
            response.setScreenMessage(this.parseVariableMulti("AF", data.substring(22)));
            response.setPrintLine(this.parseVariableMulti("AG", data.substring(22)));
            if(!this.parseSequence(data).isEmpty()) {
                response.setSequence(Integer.parseInt(this.parseSequence(data)));
            }

            response.setCheckSum(this.parseChecksum(data));
        } catch (InvalidSIP2ResponseValueException e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION,e);
        }
        return response;
    }
}
