package eu.domibus.core.crypto.spi.dss;

import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.core.crypto.spi.model.AuthenticationError;
import eu.domibus.core.crypto.spi.model.AuthenticationException;
import eu.domibus.ext.services.PkiExtService;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static eu.domibus.core.crypto.spi.dss.ValidationReport.BBB_XCV_CCCBB;
import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
@RunWith(JMockit.class)
public class DomibusDssCryptoSpiTest {

    @Test(expected = WSSecurityException.class)
    public void verifyEnmtpytTrustNoChain(@Mocked DomainCryptoServiceSpi defaultDomainCryptoService,
                                          @Mocked TSLRepository tslRepository,
                                          @Mocked ValidationReport validationReport,
                                          @Mocked PkiExtService pkiExtService,
                                          @Mocked CertificateVerifierService certificateVerifierService) throws WSSecurityException {
        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(defaultDomainCryptoService, tslRepository, validationReport, null, pkiExtService, null, certificateVerifierService);
        domibusDssCryptoProvider.verifyTrust(new X509Certificate[]{}, true, null, null);
        fail("WSSecurityException expected");
    }

    @Test(expected = WSSecurityException.class)
    public void verifyTrustNoLeafCertificate(@Mocked DomainCryptoServiceSpi defaultDomainCryptoService,

                                             @Mocked TSLRepository tslRepository,
                                             @Mocked ValidationReport validationReport,
                                             @Mocked X509Certificate noLeafCertificate,
                                             @Mocked X509Certificate chainCertificate,
                                             @Mocked PkiExtService pkiExtService,
                                             @Mocked DssCache dssCache,
                                             @Mocked CertificateVerifierService certificateVerifierService) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {noLeafCertificate, chainCertificate};

        new Expectations() {{
            pkiExtService.extractLeafCertificateFromChain(withAny(new ArrayList<>()));
            result = null;
        }};
        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(defaultDomainCryptoService, tslRepository, validationReport, null, pkiExtService, dssCache, certificateVerifierService);
        domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
        fail("WSSecurityException expected");
    }

    @Test
    public void verifyTrustNotValid(

            @Mocked TSLRepository tslRepository,
            @Mocked ValidationConstraintPropertyMapper constraintMapper,
            @Mocked X509Certificate untrustedCertificate,
            @Mocked X509Certificate chainCertificate,
            @Mocked CertificateValidator certificateValidator,
            @Mocked CertificateReports reports,
            @Mocked PkiExtService pkiExtService,
            @Mocked DssCache dssCache,
            @Mocked CertificateVerifierService certificateVerifierService) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {untrustedCertificate, chainCertificate};
        org.apache.xml.security.Init.init();

        ValidationReport validationReport = new ValidationReport();
        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(new FakeDefaultDssCrypto(), tslRepository, validationReport, constraintMapper, pkiExtService, dssCache, certificateVerifierService);

        new Expectations(domibusDssCryptoProvider, validationReport) {{
            untrustedCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            pkiExtService.extractLeafCertificateFromChain(withAny(new ArrayList<>()));
            result = untrustedCertificate;

            CertificateToken certificateToken = null;
            CertificateValidator.fromCertificate(withAny(certificateToken));
            result = certificateValidator;

            certificateValidator.validate();
            result = reports;

            domibusDssCryptoProvider.prepareCertificateSource((X509Certificate[])any,(X509Certificate)any);

            validationReport.extractInvalidConstraints((CertificateReports)any,(List<ConstraintInternal>)any);
            result= Collections.singletonList(BBB_XCV_CCCBB);

        }};

        try {
            domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
            fail("AuthenticationException expected");
        } catch (AuthenticationException e) {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof WSSecurityException);
        }
    }

    @Test
    public void verifyValidityNotValid(

            @Mocked TSLRepository tslRepository,
            @Mocked ValidationConstraintPropertyMapper constraintMapper,
            @Mocked X509Certificate invalidCertificate,
            @Mocked X509Certificate chainCertificate,
            @Mocked CertificateValidator certificateValidator,
            @Mocked CertificateReports reports,
            @Mocked PkiExtService pkiExtService,
            @Mocked DssCache dssCache,
            @Mocked CertificateVerifierService certificateVerifierService) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {invalidCertificate, chainCertificate};
        org.apache.xml.security.Init.init();

        ValidationReport validationReport = new ValidationReport();
        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(new FakeDefaultDssCrypto(), tslRepository, validationReport, constraintMapper, pkiExtService, dssCache, certificateVerifierService);

        new Expectations(domibusDssCryptoProvider, validationReport) {{

            invalidCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            pkiExtService.extractLeafCertificateFromChain(withAny(new ArrayList<>()));
            result = invalidCertificate;

            CertificateToken certificateToken = null;
            CertificateValidator.fromCertificate(withAny(certificateToken));
            result = certificateValidator;

            certificateValidator.validate();
            result = reports;

            domibusDssCryptoProvider.prepareCertificateSource((X509Certificate[])any,(X509Certificate)any);

            validationReport.extractInvalidConstraints((CertificateReports)any,(List<ConstraintInternal>)any);
            result= Collections.singletonList("BBB_XCV_ICTIVRSC");

        }};
        try {
            domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);
            fail("AuthenticationException expected");
        } catch (AuthenticationException e) {
            assertEquals(AuthenticationError.EBMS_0101, e.getAuthenticationError());
        }
    }

    @Test
    public void verifyTrustValid(

            @Mocked TSLRepository tslRepository,
            @Mocked ValidationConstraintPropertyMapper constraintMapper,
            @Mocked X509Certificate validLeafhCertificate,
            @Mocked X509Certificate chainCertificate,
            @Mocked CertificateValidator certificateValidator,
            @Mocked CertificateReports reports,
            @Mocked PkiExtService pkiExtService,
            @Mocked DssCache dssCache,
            @Mocked CertificateVerifierService certificateVerifierService) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {validLeafhCertificate, chainCertificate};
        org.apache.xml.security.Init.init();

        ValidationReport validationReport = new ValidationReport();
        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(new FakeDefaultDssCrypto(), tslRepository, validationReport, constraintMapper, pkiExtService, dssCache, certificateVerifierService);

        new Expectations(domibusDssCryptoProvider, validationReport) {{

            validLeafhCertificate.getSigAlgOID();
            result = "1.2.840.10040.4.3";

            CertificateToken certificateToken = null;
            CertificateValidator.fromCertificate(withAny(certificateToken));
            result = certificateValidator;

            pkiExtService.extractLeafCertificateFromChain(withAny(new ArrayList<>()));
            result = validLeafhCertificate;

            certificateValidator.validate();
            result = reports;

            domibusDssCryptoProvider.prepareCertificateSource((X509Certificate[])any,(X509Certificate)any);

            validationReport.extractInvalidConstraints((CertificateReports)any,(List<ConstraintInternal>)any);
            result= Collections.emptyList();

            validLeafhCertificate.getSerialNumber();
            result= BigInteger.ONE;

            validLeafhCertificate.getIssuerDN().getName();
            result="leafCertificate";

            chainCertificate.getSerialNumber();
            result= BigInteger.TEN;

            chainCertificate.getIssuerDN().getName();
            result="chainCertificate";

            dssCache.isChainValid("1leafCertificate10chainCertificate");
            returns(false,false);

        }};

        domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);

    }

    @Test
    public void verifyTrustValidityAddedByAnotherThread(

            @Mocked TSLRepository tslRepository,
            @Mocked ValidationConstraintPropertyMapper constraintMapper,
            @Mocked X509Certificate validLeafhCertificate,
            @Mocked X509Certificate chainCertificate,
            @Mocked PkiExtService pkiExtService,
            @Mocked DssCache dssCache,
            @Mocked CertificateVerifierService certificateVerifierService) throws WSSecurityException {
        final X509Certificate[] x509Certificates = {validLeafhCertificate, chainCertificate};
        org.apache.xml.security.Init.init();

        ValidationReport validationReport = new ValidationReport();
        final DomibusDssCryptoSpi domibusDssCryptoProvider = new DomibusDssCryptoSpi(new FakeDefaultDssCrypto(), tslRepository, validationReport, constraintMapper, pkiExtService, dssCache, certificateVerifierService);

        new Expectations(domibusDssCryptoProvider, validationReport) {{

            validLeafhCertificate.getSerialNumber();
            result= BigInteger.ONE;

            validLeafhCertificate.getIssuerDN().getName();
            result="leafCertificate";

            chainCertificate.getSerialNumber();
            result= BigInteger.TEN;

            chainCertificate.getIssuerDN().getName();
            result="chainCertificate";

            dssCache.isChainValid("1leafCertificate10chainCertificate");
            returns(false,true);

        }};

        domibusDssCryptoProvider.verifyTrust(x509Certificates, true, null, null);

        new Verifications(){{
            pkiExtService.extractLeafCertificateFromChain(withAny(new ArrayList<>()));times=0;
        }};

    }



}