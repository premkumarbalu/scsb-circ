package com.pkrete.jsip2.parser;

import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseException;
import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseValueException;
import com.pkrete.jsip2.messages.SIP2MessageResponse;
import com.pkrete.jsip2.messages.responses.SIP2DeleteBibResponse;
import org.recap.ils.jsipmessages.SIP2CreateBibResponse;

/**
 * Created by sudhishk on 9/11/16.
 */
public class SIP2DeleteBibResponseParser extends  SIP2ResponseParser{

    @Override
    public SIP2MessageResponse parse(String data) throws InvalidSIP2ResponseValueException, InvalidSIP2ResponseException {
        SIP2DeleteBibResponse response = new SIP2DeleteBibResponse(data);
        try {
            response.setOk(this.intToBool(data.charAt(2)));
//            response.setBibId(parseVariable("MA", data.substring(8,15), false));
            String msg= data.substring(data.indexOf("|"));
//            msg =msg.replaceAll(,"");
            response.setScreenMessage(parseVariableMulti("AF", msg.substring(2)));
            if (!parseSequence(data).isEmpty()) {
                response.setSequence(Integer.parseInt(parseSequence(data)));
            }
            response.setCheckSum(parseChecksum(data));
        } catch (InvalidSIP2ResponseValueException e) {
            throw new InvalidSIP2ResponseValueException(e.getMessage() + " Response message string: \"" + data + "\"");
        }
        return response;
    }
}
