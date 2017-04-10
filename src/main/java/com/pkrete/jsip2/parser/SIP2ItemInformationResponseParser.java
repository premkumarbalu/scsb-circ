//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pkrete.jsip2.parser;

import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseException;
import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseValueException;
import com.pkrete.jsip2.messages.responses.SIP2ItemInformationResponse;
import com.pkrete.jsip2.variables.*;
import org.recap.ReCAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Sip 2 item information response parser.
 */
public class SIP2ItemInformationResponseParser extends SIP2ResponseParser {

    private static final Logger logger = LoggerFactory.getLogger(SIP2ItemInformationResponseParser.class);

    @Override
    public SIP2ItemInformationResponse parse(String data) throws InvalidSIP2ResponseValueException, InvalidSIP2ResponseException {
        SIP2ItemInformationResponse response = new SIP2ItemInformationResponse(data);

        try {
            response.setOk(this.intToBool(data.charAt(2)));
            response.setCirculationStatus(CirculationStatusFactory.getInstance().getCirculationStatus(data.substring(3, 5)));
            response.setSecurityMarker(SecurityMarkerFactory.getInstance().getSecurityMarker(data.substring(5, 7)));
            response.setFeeType(FeeTypeFactory.getInstance().getFeeType(data.substring(7, 9)));
            response.setTransactionDate(data.substring(9, 27));
            response.setHoldQueueLength(this.parseVariableWithoutDelimiter("CF", data.substring(26), false));
            response.setDueDate(this.parseVariableWithoutDelimiter("AH", data.substring(26), false));
            response.setRecallDate(this.parseVariableWithoutDelimiter("CJ", data.substring(26), false));
            response.setHoldPickupDate(this.parseVariableWithoutDelimiter("CM", data.substring(26), false));
            response.setItemIdentifier(this.parseVariableWithoutDelimiter("AB", data.substring(26)));
            response.setTitleIdentifier(this.parseVariable("AJ", data.substring(26)));
            response.setOwner(this.parseVariable("BG", data.substring(26), false));
            if (this.existsAndNotEmpty("BH", data.substring(26))) {
                response.setCurrencyType(CurrencyTypeFactory.getInstance().getCurrencyType(this.parseVariable("BH", data.substring(26))));
            }

            response.setFeeAmount(this.parseVariable("BV", data.substring(26), false));
            if (this.existsAndNotEmpty("CK", data.substring(26))) {
                response.setMediaType(MediaTypeFactory.getInstance().getMediaType(this.parseVariable("CK", data.substring(26))));
            }

            response.setPermanentLocation(this.parseVariable("AQ", data.substring(26), false));
            response.setCurrentLocation(this.parseVariable("AP", data.substring(26), false));
            response.setItemProperties(this.parseVariable("CH", data.substring(26), false));
            response.setScreenMessage(this.parseVariableMulti("AF", data.substring(26)));
            response.setPrintLine(this.parseVariableMulti("AG", data.substring(26)));
            if(!this.parseVariableMulti("MA", data.substring(26)).isEmpty()) {
                response.setBibId(this.parseVariableMulti("MA", data.substring(26)).get(0));
            }
            if (!this.parseSequence(data).isEmpty()) {
                response.setSequence(Integer.parseInt(this.parseSequence(data)));
            }

            response.setCheckSum(this.parseChecksum(data));
            return response;
        } catch (InvalidSIP2ResponseValueException var4) {
            logger.error(ReCAPConstants.LOG_ERROR,var4);
            throw new InvalidSIP2ResponseValueException(var4.getMessage() + " Response message string: \"" + data + "\"");
        }
    }
}
