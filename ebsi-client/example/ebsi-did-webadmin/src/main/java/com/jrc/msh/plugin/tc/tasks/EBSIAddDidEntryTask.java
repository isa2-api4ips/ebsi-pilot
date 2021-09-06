package com.jrc.msh.plugin.tc.tasks;

import com.as4mail.ebsi.did.DIDDocument;
import com.as4mail.ebsi.did.DIDDocumentEntryType;
import com.jrc.msh.plugin.tc.EBSIUtils;
import com.jrc.msh.plugin.tc.XMLUtils;
import com.jrc.msh.plugin.tc.exception.EbsiException;
import com.jrc.msh.plugin.tc.web.EbsiWebPluginData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import java.io.FileNotFoundException;
import java.util.UUID;

public class EBSIAddDidEntryTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(EBSIAddDidEntryTask.class);

    EBSIUtils ebsiUtils;
    DIDDocumentEntryType didEntry; 
    String domainOwnerAddress;
    DIDDocument didDocument;

    public EBSIAddDidEntryTask(DIDDocument didDocument, EBSIUtils ebsiUtils, DIDDocumentEntryType didEntry, String domainOwnerAddress) {
        this.didDocument = didDocument;
        this.ebsiUtils = ebsiUtils;
        this.didEntry = didEntry;
        this.domainOwnerAddress = domainOwnerAddress;
    }

    public void run() {
      /*  String transaction;
        String message;
        try {
            transaction =  ebsiUtils.saveDidEntry(didEntry, domainOwnerAddress);
            didEntry.setTransactionHash(transaction);
            message = "Added new DID entry ["+didEntry.getDidEntry()+"] in transaction ["+transaction+"]!";
            LOG.info(message);
        } catch (EbsiException | RuntimeException e) {
            LOG.error("Error occurred while adding entry to DID document: ["+e.getMessage()+"]",e);
            closeDidEntryDialogWithMessage(FacesMessage.SEVERITY_ERROR,
                    "Error occurred while adding entry to DID document", e.getMessage());
            return;
        }


        if (didEntry.getId() == null) {
            didEntry.setId(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 9));
            this.didDocument.getDIDDocumentEntries().add(didEntry);
        }
        // save DID
        try {
            XMLUtils.serialize(this.didDocument, getDIDDocumentFile());
        } catch (FileNotFoundException e) {
            LOG.error("Error occurred while saving data", e);
        }
        didEntry = null;*/
    }
}
