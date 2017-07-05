package org.recap.controller;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.camel.EmailPayLoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Vector;

/**
 * Created by harikrishnanv on 20/6/17.
 */
@RestController
@RequestMapping("/submitCollectionJob")
public class SubmitCollectionJobController {

    private static final Logger logger = LoggerFactory.getLogger(AccessionReconcilationJobController.class);

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    private CamelContext camelContext;

    @Value("${submit.collection.email.subject}")
    private String submitCollectionEmailSubject;

    @Value("${submit.collection.email.subject.for.empty.directory}")
    private String submitCollectionEmailSubjectForEmptyDirectory;

    @Value("${ftp.submit.collection.pul.report}")
    private String submitCollectionPULReportLocation;

    @Value("${ftp.submit.collection.cul.report}")
    private String submitCollectionCULReportLocation;

    @Value("${ftp.submit.collection.nypl.report}")
    private String submitCollectionNYPLReportLocation;

    @Value("${submit.collection.email.pul.to}")
    private String emailToPUL;

    @Value("${submit.collection.email.cul.to}")
    private String emailToCUL;

    @Value("${submit.collection.email.nypl.to}")
    private String emailToNYPL;

    @Value("${ftp.knownHost}")
    private String ftpKnownHost;

    @Value("${ftp.privateKey}")
    private String ftpPrivateKey;

    @Value("${sftp.submitcollection.pul}")
    private String ftpSubmitcollectionPul;

    @Value("${sftp.submitcollection.cul}")
    private String ftpSubmitcollectionCul;

    @Value("${sftp.submitcollection.nypl}")
    private String ftpSubmitcollectionNypl;

    @Value("${sftp.sftpHost}")
    private String sftpHost;

    @Value("${sftp.sftpPort}")
    private String sftpPort;

    @Value("${ftp.userName}")
    private String sftpUser;

    @Value("${sftp.sftpPassword}")
    private String sftpPassword;

    @RequestMapping(value = "/startSubmitCollection",method = RequestMethod.POST)
    public String startSubmitCollection() throws Exception{

        Session session 	= null;
        Channel channel 	= null;
        ChannelSftp pulchannelSftp = null;
        ChannelSftp culchannelSftp = null;
        ChannelSftp nyplchannelSftp = null;

        try{
            JSch jsch = new JSch();
            session = jsch.getSession(sftpUser,sftpHost,Integer.parseInt(sftpPort));
            session.setPassword(sftpPassword);
            jsch.setKnownHosts(ftpKnownHost);
            jsch.addIdentity(ftpPrivateKey);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            pulchannelSftp = (ChannelSftp)channel;
            culchannelSftp = (ChannelSftp)channel;
            nyplchannelSftp = (ChannelSftp)channel;
            pulchannelSftp.cd(ftpSubmitcollectionPul);
            culchannelSftp.cd(ftpSubmitcollectionCul);
            nyplchannelSftp.cd(ftpSubmitcollectionNypl);
            Vector pulfilelist = pulchannelSftp.ls(ftpSubmitcollectionPul);
            Vector culfilelist = culchannelSftp.ls(ftpSubmitcollectionCul);
            Vector nyplfilelist = nyplchannelSftp.ls(ftpSubmitcollectionNypl);
            boolean flag=false;
            flag = checkIfFileExistsInPULFolderAndStart(pulfilelist, flag);
            flag = checkIfFileExistsInCULFolderAndStart(culfilelist, flag);
            checkIfFilesExistsInNYPLAndStart(nyplfilelist, flag);
        }
        catch(Exception ex){
            logger.error(ReCAPConstants.LOG_ERROR,ex);
        }
        logger.info("Submit Collection Job ends");
        return ReCAPConstants.SUCCESS;
    }

    private void checkIfFilesExistsInNYPLAndStart(Vector nyplfilelist, boolean flag) throws Exception {
        for(int i=0; i<nyplfilelist.size();i++){
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) nyplfilelist.get(i);
            if(!entry.getFilename().startsWith(".")) {
                flag=true;
                break;
            }
        }
        if (flag) {
            logger.info("Started for nypl");
            camelContext.startRoute(ReCAPConstants.SUBMIT_COLLECTION_FTP_NYPL_ROUTE);
            flag = false;
        }
        else{
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.NYPL), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
            logger.info("No files in the NYPL directory");
        }
    }

    private boolean checkIfFileExistsInCULFolderAndStart(Vector culfilelist, boolean flag) throws Exception {
        for(int i=0; i<culfilelist.size();i++){
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) culfilelist.get(i);
            if(!entry.getFilename().startsWith(".")) {
                flag=true;
                break;
            }
        }
        if (flag) {
            logger.info("Started for cul");
            camelContext.startRoute(ReCAPConstants.SUBMIT_COLLECTION_FTP_CUL_ROUTE);
            flag=false;
        }
        else{
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.COLUMBIA), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
            logger.info("No files in the CUL directory");
        }
        return flag;
    }

    private boolean checkIfFileExistsInPULFolderAndStart(Vector pulfilelist, boolean flag) throws Exception {
        for(int i=0; i<pulfilelist.size();i++){
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) pulfilelist.get(i);
            if(!entry.getFilename().startsWith(".")) {
                flag=true;
                break;
            }
        }
        if (flag) {
            logger.info("Started for pul");
            camelContext.startRoute(ReCAPConstants.SUBMIT_COLLECTION_FTP_PUL_ROUTE);
            flag=false;
        }
        else{
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.PRINCETON), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
            logger.info("No files in the PUL directory");
        }
        return flag;
    }

    private EmailPayLoad getEmailPayLoad(String institutionCode) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setSubject(submitCollectionEmailSubjectForEmptyDirectory);
        if(ReCAPConstants.PRINCETON.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToPUL);
            emailPayLoad.setLocation(submitCollectionPULReportLocation);
        } else if(ReCAPConstants.COLUMBIA.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToCUL);
            emailPayLoad.setLocation(submitCollectionCULReportLocation);
        } else if(ReCAPConstants.NYPL.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToNYPL);
            emailPayLoad.setLocation(submitCollectionNYPLReportLocation);
        }
        return  emailPayLoad;
    }
}
