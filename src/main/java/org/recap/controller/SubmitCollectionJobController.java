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

    @Value("${sftp.submitcollection.pul.cgdprotected}")
    private String ftpSubmitcollectionPulCgdProtected;

    @Value("${sftp.submitcollection.cul.cgdprotected}")
    private String ftpSubmitcollectionCulCgdProtected;

    @Value("${sftp.submitcollection.nypl.cgdprotected}")
    private String ftpSubmitcollectionNyplCgdProtected;

    @Value("${sftp.submitcollection.pul.cgdnotprotected}")
    private String ftpSubmitcollectionPulCgdNotProtected;

    @Value("${sftp.submitcollection.cul.cgdnotprotected}")
    private String ftpSubmitcollectionCulCgdNotProtected;

    @Value("${sftp.submitcollection.nypl.cgdnotprotected}")
    private String ftpSubmitcollectionNyplCgdNotProtected;

    @Value("${sftp.sftpHost}")
    private String sftpHost;

    @Value("${sftp.sftpPort}")
    private String sftpPort;

    @Value("${ftp.userName}")
    private String sftpUser;

    @Value("${sftp.sftpPassword}")
    private String sftpPassword;


    /**
     * This method is initiated from the scheduler to start the submit collection process for each institution
     * if file exists in their respective folders.
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/startSubmitCollection",method = RequestMethod.POST)
    public String startSubmitCollection() throws Exception{

        Session session 	= null;
        Channel channel 	= null;
        ChannelSftp pulCgdProtectedchannelSftp = null;
        ChannelSftp culCgdProtectedchannelSftp = null;
        ChannelSftp nyplCgdProtectedchannelSftp = null;
        ChannelSftp pulCgdNotProtectedchannelSftp = null;
        ChannelSftp culCgdNotProtectedchannelSftp = null;
        ChannelSftp nyplCgdNotProtectedchannelSftp = null;


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
            pulCgdProtectedchannelSftp = (ChannelSftp)channel;
            culCgdProtectedchannelSftp = (ChannelSftp)channel;
            nyplCgdProtectedchannelSftp = (ChannelSftp)channel;
            pulCgdNotProtectedchannelSftp = (ChannelSftp)channel;
            culCgdNotProtectedchannelSftp = (ChannelSftp)channel;
            nyplCgdNotProtectedchannelSftp = (ChannelSftp)channel;
            pulCgdProtectedchannelSftp.cd(ftpSubmitcollectionPulCgdProtected);
            culCgdProtectedchannelSftp.cd(ftpSubmitcollectionCulCgdProtected);
            nyplCgdProtectedchannelSftp.cd(ftpSubmitcollectionNyplCgdProtected);
            pulCgdNotProtectedchannelSftp.cd(ftpSubmitcollectionPulCgdNotProtected);
            culCgdNotProtectedchannelSftp.cd(ftpSubmitcollectionCulCgdNotProtected);
            nyplCgdNotProtectedchannelSftp.cd(ftpSubmitcollectionNyplCgdNotProtected);
            Vector pulCgdProtectedFileList = pulCgdProtectedchannelSftp.ls(ftpSubmitcollectionPulCgdProtected);
            Vector culCgdProtectedFileList = culCgdProtectedchannelSftp.ls(ftpSubmitcollectionCulCgdProtected);
            Vector nyplCgdProtectedFileList = nyplCgdProtectedchannelSftp.ls(ftpSubmitcollectionNyplCgdProtected);
            Vector pulCgdNotProtectedFileList = pulCgdProtectedchannelSftp.ls(ftpSubmitcollectionPulCgdNotProtected);
            Vector culCgdNotProtectedFileList = culCgdProtectedchannelSftp.ls(ftpSubmitcollectionCulCgdNotProtected);
            Vector nyplCgdNotProtectedFileList = nyplCgdProtectedchannelSftp.ls(ftpSubmitcollectionNyplCgdNotProtected);
            boolean flag=false;
            flag = checkIfFileExistsOrNot(pulCgdProtectedFileList, flag,ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_PUL_ROUTE,ReCAPConstants.PRINCETON);
            flag = checkIfFileExistsOrNot(culCgdProtectedFileList, flag,ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_CUL_ROUTE,ReCAPConstants.COLUMBIA);
            flag = checkIfFileExistsOrNot(nyplCgdProtectedFileList, flag,ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_NYPL_ROUTE,ReCAPConstants.NYPL);
            flag = checkIfFileExistsOrNot(pulCgdNotProtectedFileList, flag,ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_PUL_ROUTE,ReCAPConstants.PRINCETON);
            flag = checkIfFileExistsOrNot(culCgdNotProtectedFileList, flag,ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_CUL_ROUTE,ReCAPConstants.COLUMBIA);
            flag = checkIfFileExistsOrNot(nyplCgdNotProtectedFileList, flag,ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_NYPL_ROUTE,ReCAPConstants.NYPL);

        }
        catch(Exception ex){
            logger.error(ReCAPConstants.LOG_ERROR,ex);
        }
        logger.info("Submit Collection Job ends");
        return ReCAPConstants.SUCCESS;
    }

    private boolean checkIfFileExistsOrNot(Vector fileList, boolean flag, String routeId,String institution) throws Exception {
        for(int i=0; i<fileList.size();i++){
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) fileList.get(i);
            if(!entry.getFilename().startsWith(".")) {
                flag=true;
                break;
            }
        }
        if (flag) {
            logger.info("Started for : {} ",routeId);
            camelContext.startRoute(routeId);
            flag=false;
        }
        else{
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(institution), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
            logger.info("No files in the {} directoty",routeId);
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
