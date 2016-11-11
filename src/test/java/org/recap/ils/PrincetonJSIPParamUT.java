package org.recap.ils;

import com.pkrete.jsip2.messages.responses.SIP2ItemInformationResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Created by sudhishk on 10/11/16.
 */
@RunWith(Parameterized.class)
public class PrincetonJSIPParamUT extends BaseTestCase {

    @Autowired
    private PrincetonJSIPConnector princetonESIPConnector;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "32101077423406", "32101061738587"}
        });
    }

    private String fInput;

    private String fExpected;


    public PrincetonJSIPParamUT(String input, String expected) {
        fInput= input;
        fExpected= expected;


    }

    private String fetch(String input){

        SIP2ItemInformationResponse itemInformationResponse = princetonESIPConnector.lookupItem(fInput);

        return itemInformationResponse.getItemIdentifier();
    }

    @Test
    public void lookupItem() throws Exception {

//        SIP2ItemInformationResponse itemInformationResponse = princetonESIPConnector.lookupItem(fInput);

        assertEquals(fExpected,fetch(fInput));
//        assertEquals("Bolshevism, by an eye-witness from Wisconsin, by Lieutenant A. W. Kliefoth ...",itemInformationResponse.getTitleIdentifier());
    }
}
