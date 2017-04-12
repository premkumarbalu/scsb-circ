package com.pkrete.jsip2.parser;

import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseException;
import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseValueException;
import com.pkrete.jsip2.messages.SIP2MessageResponse;
import com.pkrete.jsip2.messages.responses.SIP2CreateBibResponse;
import org.recap.ReCAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by sudhishk on 9/11/16.
 */
public class SIP2CreateBibResponseParser extends  SIP2ResponseParser{

    private static final Logger logger = LoggerFactory.getLogger(SIP2CreateBibResponseParser.class);

    /**
     *
     * @param data
     * @return
     * @throws InvalidSIP2ResponseValueException
     * @throws InvalidSIP2ResponseException
     */
    @Override
    public SIP2MessageResponse parse(String data) throws InvalidSIP2ResponseValueException, InvalidSIP2ResponseException {
        SIP2CreateBibResponse response = new SIP2CreateBibResponse(data);
        try {
            String[] strmsg = data.split("\\|");
            response.setOk(this.intToBool(data.charAt(2)));
            response.setItemIdentifier("");
            response.setBibId(strmsg[1].substring(2));
            response.setScreenMessage(Arrays.asList(strmsg[2].substring(2)));

            if (!parseSequence(data).isEmpty()) {
                response.setSequence(Integer.parseInt(parseSequence(data)));
            }
            response.setCheckSum(parseChecksum(data));
        } catch (InvalidSIP2ResponseValueException e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
            throw new InvalidSIP2ResponseValueException(e.getMessage() + " Response message string: \"" + data + "\"");
        }
        return response;
    }
}
