package org.recap.ils.model.nypl;

import org.junit.Test;
import org.recap.BaseTestCase;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 3/4/17.
 */
public class NoticeUT extends BaseTestCase{

    @Test
    public void testNotice(){
        Notice notice = new Notice();
        notice.setCreatedDate(new Date().toString());
        notice.setData("test");
        notice.setText("test");

        assertNotNull(notice.getCreatedDate());
        assertNotNull(notice.getData());
        assertNotNull(notice.getText());
    }

}