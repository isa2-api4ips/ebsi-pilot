/*
 * Copyright 2016, Supreme Court Republic of Slovenia
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package com.jrc.msh.plugin.tc.web;

import com.as4mail.ebsi.did.DIDDocument;
import com.as4mail.ebsi.did.DIDDocumentEntryType;
import com.jrc.msh.plugin.tc.EBSIUtils;
import com.jrc.msh.plugin.tc.X509CertificateUtils;
import com.jrc.msh.plugin.tc.XMLUtils;
import com.jrc.msh.plugin.tc.exception.EbsiException;
import eu.europa.ec.edelivery.ebsi.did.entities.Did;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceContext;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.StandardCopyOption;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("ebsiWebPluginData")
public class EbsiWebPluginData implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(EbsiWebPluginData.class);

    DIDDocument didDocument = null;

    @Resource
    WebServiceContext context;

    String domainOwnerAddress = "0x2cf81263cc679c9132d3375cefd82d4f72c183e5";
    String didSmartContractAddress = "0xFd1d90Ae81547E06079459dCaFCFdB2c01795214";

    String didDocumentURl = "https://api.ebsi.xyz/did/v1/identifiers/did%3Aebsi%3A0x2cf81263cc679c9132d3375cefd82d4f72c183e5";
    String ebsiURl = "https://api.ebsi.xyz";


    DIDDocumentEntryType selectedDidEntry;
    UploadedFile currentCertificateFile;

    EBSIUtils ebsiUtils = new EBSIUtils(didSmartContractAddress);

    public EbsiWebPluginData() {

        // save DID
        File didDocumentFile = getDIDDocumentFile();
        LOG.info("Load  did document: ["+didDocumentFile.getAbsolutePath()+"]");
        if (didDocumentFile.exists()) {
            try {
                this.didDocument = (DIDDocument) XMLUtils.deserialize(didDocumentFile, DIDDocument.class);
            } catch (JAXBException e) {
                LOG.error("Error occurred while saving data", e);
            }
        }
        if (didDocument == null) {
            didDocument = new DIDDocument();
        }
    }


    public String getDidDocumentURl() {
        return didDocumentURl;
    }

    public void setDidDocumentURl(String didDocumentURl) {
        this.didDocumentURl = didDocumentURl;
    }

    public String getEbsiURl() {
        return ebsiURl;
    }

    public void setEbsiURl(String ebsiURl) {
        this.ebsiURl = ebsiURl;
    }

    public int getSelectedEntryValidDays() {
        return selectedDidEntry != null && selectedDidEntry.getValidInSeconds() != null
                ? selectedDidEntry.getValidInSeconds().intValue() / 86400 : 1;

    }

    public void setSelectedEntryValidDays(int value) {
        selectedDidEntry.setValidInSeconds(BigInteger.valueOf(value * 86400));
    }

    public String generatedHash() {
        if (selectedDidEntry == null
                || selectedDidEntry.getCertificate() == null
                || selectedDidEntry.getCertificate().getBin() == null
                || selectedDidEntry.getPartySchema() == null
                || selectedDidEntry.getPartyID() == null) {
            return "";
        }
        return ebsiUtils.generateHash(selectedDidEntry.getPartySchema(), selectedDidEntry.getPartyID(), selectedDidEntry.getCertificate().getBin());
    }

    public String getDomainOwnerAddress() {
        return domainOwnerAddress;
    }

    public void setDomainOwnerAddress(String domainOwnerAddress) {
        this.domainOwnerAddress = domainOwnerAddress;
    }

    public String getDidSmartContractAddress() {
        return didSmartContractAddress;
    }

    public void setDidSmartContractAddress(String didSmartContractAddress) {
        this.didSmartContractAddress = didSmartContractAddress;
    }

    /**
     * @return
     */
    protected ExternalContext externalContext() {
        return facesContext().getExternalContext();
    }


    public List<DIDDocumentEntryType> getDidDocumentEntries() {
        return didDocument == null ? java.util.Collections.emptyList() : didDocument.getDIDDocumentEntries();
    }

    public DIDDocumentEntryType getSelectedDidEntry() {
        return selectedDidEntry;
    }

    public void setSelectedDidEntry(DIDDocumentEntryType selectedProduct) {
        this.selectedDidEntry = selectedProduct;
    }

    public void openNew() {
        this.selectedDidEntry = new DIDDocumentEntryType();
    }

    public void closeDidEntryDialog() {
        PrimeFaces.current().ajax().update("tabView:tcTableform");
        PrimeFaces.current().executeScript("PF('DidEntryDialog').hide()");
        PrimeFaces.current().executeScript("PF('BlockDidEntryDialog').hide()");
    }

    public void closeDidEntryDialogWithMessage(FacesMessage.Severity severity, String summary) {
        closeDidEntryDialogWithMessage(severity, summary, null);
    }

    public void closeDidEntryDialogWithMessage(FacesMessage.Severity severity, String summary, String details) {
        closeDidEntryDialog();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, summary, details));
    }

    public void saveDidEntry() throws JAXBException {

        String hexEntry = generatedHash();
        // test if entries already exists
        Optional<DIDDocumentEntryType> entry = this.didDocument.getDIDDocumentEntries().stream().filter(didDocumentEntryType -> didDocumentEntryType.getDidEntry().equals(hexEntry)).findFirst();

        if (entry.isPresent()) {
            LOG.info("Entry with id ["+hexEntry+"] already exists!");
            closeDidEntryDialogWithMessage(FacesMessage.SEVERITY_ERROR, "Entry with id ["+hexEntry+"] already exists!");
            return;
        }
        LOG.info("Save did entry");
        if (this.selectedDidEntry == null) {
            LOG.info("Selected entry is null!");
            closeDidEntryDialogWithMessage(FacesMessage.SEVERITY_ERROR, "Could not find new did entry!");
            return;
        }

        if (this.selectedDidEntry.getCertificate() == null ||
                this.selectedDidEntry.getCertificate().getBin() == null) {
            LOG.info("Missing certificate data");
            closeDidEntryDialogWithMessage(FacesMessage.SEVERITY_ERROR, "Missing certificate data!");
            return;
        }

        String transaction;
        String message;
        try {
            transaction = ebsiUtils.saveDidEntry(this.selectedDidEntry, domainOwnerAddress);
            this.selectedDidEntry.setTransactionHash(transaction);
            message = "Added new DID entry [" + this.selectedDidEntry.getDidEntry() + "] in transaction [" + transaction + "]!";
            LOG.info(message);
        } catch (EbsiException | RuntimeException e) {
            LOG.error("Error occurred while adding entry to DID document: [" + e.getMessage() + "]", e);
            closeDidEntryDialogWithMessage(FacesMessage.SEVERITY_ERROR,
                    "Error occurred while adding entry to DID document", e.getMessage());
            return;
        }


        if (this.selectedDidEntry.getId() == null) {
            this.selectedDidEntry.setId(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 9));
            this.didDocument.getDIDDocumentEntries().add(this.selectedDidEntry);
        }
        // save DID
        try {
            XMLUtils.serialize(this.didDocument, getDIDDocumentFile());
        } catch (FileNotFoundException e) {
            LOG.error("Error occurred while saving data", e);
        }
        this.selectedDidEntry = null;
        closeDidEntryDialogWithMessage(FacesMessage.SEVERITY_INFO, message);
    }

    public void deleteSelectedDidEntry() {

        String transaction;
        String message;
        try {
            transaction = ebsiUtils.deleteDidEntry(this.selectedDidEntry.getDidEntry(), domainOwnerAddress);
            message = "Did entry [" + this.selectedDidEntry.getDidEntry() + "] was removed in transaction [" + transaction + "]!";
            LOG.info(message);
        } catch (EbsiException e) {
            LOG.error("Error occurred while removing entry from DID document: [" + e.getMessage() + "]", e);
            closeDidEntryDialogWithMessage(FacesMessage.SEVERITY_ERROR,
                    "Error occurred while removing entry from DID document", e.getMessage());
            return;
        }

        this.getDidDocumentEntries().remove(this.selectedDidEntry);
        this.selectedDidEntry = null;
        // save DID
        try {
            XMLUtils.serialize(this.didDocument, getDIDDocumentFile());
        } catch (FileNotFoundException | JAXBException e) {
            LOG.error("Error occurred while saving data", e);
        }
        closeDidEntryDialogWithMessage(FacesMessage.SEVERITY_INFO, message);
    }

    public void synhronizeDidDocument(){
        Did did = null;

        try {
            did = ebsiUtils.getDidDocument(getDidDocumentURl());
        } catch (EbsiException e) {
            String message = "Error occurred while fetching did document: [" + e.getMessage() + "]";
            LOG.error(message, e);
            closeDidEntryDialogWithMessage(FacesMessage.SEVERITY_ERROR, message);
        }


        List<String> hasValues = getDidDocumentEntries().stream().map(didDocumentEntryType -> didDocumentEntryType.getDidEntry()).collect(Collectors.toList());
        List<String> hasDidValues = did.getPublicKey().stream().filter(publicKey -> publicKey.getValue()!=null).map(publicKey -> publicKey.getValue()).collect(Collectors.toList());

        List<String> removeEntries =
                did.getPublicKey().stream().filter(publicKey -> {
                    return publicKey.getValue() != null && !hasValues.contains(publicKey.getValue() );
                }).map(publicKey -> publicKey.getValue()).collect(Collectors.toList());

        List<DIDDocumentEntryType> addEntries =
                getDidDocumentEntries().stream().filter(entryType -> {
                    return entryType.getDidEntry() != null && !hasDidValues.contains(entryType.getDidEntry() );
                }).collect(Collectors.toList());


        String errMessages = "";
        for (String removeEntry:removeEntries) {
            try {
                ebsiUtils.deleteDidEntry(removeEntry, domainOwnerAddress);
            } catch (EbsiException e) {
                String message = "Error occurred while deleting did document: [" +removeEntry + "]";
                LOG.error(message, e);
                errMessages += message + "\n";
            }
        }

        List<String> addedEntries = new ArrayList<>();
        for (DIDDocumentEntryType addEntry:addEntries) {
            try {
                ebsiUtils.saveDidEntry(addEntry, domainOwnerAddress);
                addedEntries.add(addEntry.getDidEntry());
            } catch (EbsiException e) {
                String message = "Error occurred while adding did document: [" +addEntry.getDidEntry() + "]";
                LOG.error(message, e);
                errMessages += message + "\n";
            }
        }
        String message = null;
        if (addedEntries.isEmpty() && removeEntries.isEmpty()) {
            message = "Did document is synchronized!";
        } else {
            message = "DID entries: ";
            if (!addedEntries.isEmpty()) {
                message +="  Added ["
                        +String.join(",",addedEntries)+"]";
            }
            if (!removeEntries.isEmpty()) {
                message +="  Removed ["
                        +String.join(",",removeEntries)+"]";
            }
        }

        closeDidEntryDialogWithMessage(FacesMessage.SEVERITY_INFO, message);
        if (!errMessages.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,errMessages, null));
        }
    }

    public void updateDialogDataBlue() {
        LOG.info("Prefill domibus blue data!");
        prefillDialogDomibus("/keystore/blue_gw.cer",
                "urn:oasis:names:tc:ebcore:partyid-type:unregistered",
                "domibus-blue");
    }

    public void updateDialogDataRed() {
        LOG.info("Prefill domibus red data!");
        prefillDialogDomibus("/keystore/red_gw.cer",
                "urn:oasis:names:tc:ebcore:partyid-type:unregistered",
                "domibus-red");
    }

    public void prefillDialogDomibus(String certificatePath, String participantSchema, String participantId) {
        LOG.info("Prefill data from cert path: [" + certificatePath + "], participantSchema: [" + participantSchema + "], participantId: [" + participantId + "]!");
        InputStream inputStream = EbsiWebPluginData.class.getResourceAsStream(certificatePath);


        try {
            X509Certificate cert = X509CertificateUtils.getCertificateFromInputStream(inputStream);
            if (this.selectedDidEntry.getCertificate() == null) {
                this.selectedDidEntry.setCertificate(new DIDDocumentEntryType.Certificate());
            }
            this.selectedDidEntry.getCertificate().setBin(cert.getEncoded());
            this.selectedDidEntry.getCertificate().setIssuer(cert.getIssuerDN().getName());
            this.selectedDidEntry.getCertificate().setSubject(cert.getSubjectDN().getName());
            this.selectedDidEntry.getCertificate().setValidTo(cert.getNotAfter());
            this.selectedDidEntry.getCertificate().setValidFrom(cert.getNotBefore());

            long validTime = cert.getNotAfter().getTime() - Calendar.getInstance().getTimeInMillis();
            validTime = validTime > 0 ? validTime / 1000 : 86400;
            this.selectedDidEntry.setValidInSeconds(BigInteger.valueOf(validTime));
            this.selectedDidEntry.setPartySchema(participantSchema);
            this.selectedDidEntry.setPartyID(participantId);

            PrimeFaces.current().ajax().update("dlgDidDetails:dlgFormDidDetails:did-entry-content");
        } catch (CertificateException e) {
            LOG.error("Error occurred while reading from certificate path: [" + certificatePath + "]!", e);
            FacesMessage message = new FacesMessage("Error occurred while updating the values! Err:" + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }
    }


    public UploadedFile getCurrentCertificateFile() {
        return currentCertificateFile;
    }

    public void setCurrentCertificateFile(UploadedFile currentCertificateFile) {
        this.currentCertificateFile = currentCertificateFile;
    }


    public void handleCertificateUpload(FileUploadEvent event) {
        LOG.info("handleCertificateUpload");
        InputStream inputStream = null;
        try {
            inputStream = event.getFile().getInputStream();
        } catch (IOException e) {
            FacesMessage message = new FacesMessage("Can not read file:", event.getFile().getFileName() + " !");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        try {
            X509Certificate cert = X509CertificateUtils.getCertificateFromInputStream(inputStream);
            if (this.selectedDidEntry.getCertificate() == null) {
                this.selectedDidEntry.setCertificate(new DIDDocumentEntryType.Certificate());
            }
            this.selectedDidEntry.getCertificate().setBin(cert.getEncoded());
            this.selectedDidEntry.getCertificate().setIssuer(cert.getIssuerDN().getName());
            this.selectedDidEntry.getCertificate().setSubject(cert.getSubjectDN().getName());
            this.selectedDidEntry.getCertificate().setValidTo(cert.getNotAfter());
            this.selectedDidEntry.getCertificate().setValidFrom(cert.getNotBefore());

            long validTime = cert.getNotAfter().getTime() - Calendar.getInstance().getTimeInMillis();
            validTime = validTime > 0 ? validTime / 1000 : 86400;
            this.selectedDidEntry.setValidInSeconds(BigInteger.valueOf(validTime));

            PrimeFaces.current().ajax().update("dlgDidDetails:dlgFormDidDetails:did-entry-content");

        } catch (CertificateException e) {
            FacesMessage message = new FacesMessage("Error occurred while reading file:", event.getFile().getFileName() + " ! Err:" + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }
    }


    public boolean hasSelectedDidEntry() {
        return this.selectedDidEntry != null;
    }


    public void upload() {
        LOG.info("Uploaded");
        if (currentCertificateFile != null) {
            FacesMessage message = new FacesMessage("Successful", currentCertificateFile.getFileName() + " is uploaded!");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }


    /**
     * @return
     */
    protected FacesContext facesContext() {
        return FacesContext.getCurrentInstance();
    }


    public File getDIDDocumentFile() {
        File didFile = new File("did-document.xml");
        if (!didFile.exists()) {
            // copy example from resource
            InputStream inputStream = EbsiWebPluginData.class.getResourceAsStream("/examples/did-document.xml");

            try {
                java.nio.file.Files.copy(
                        inputStream,
                        didFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOG.error("Error occurred while writing did example to ["+didFile.getAbsolutePath()+"]!", e);
            }
        }
        return didFile ;
    }


}
