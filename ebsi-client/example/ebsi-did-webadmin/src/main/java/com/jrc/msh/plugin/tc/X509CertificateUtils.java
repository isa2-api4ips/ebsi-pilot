package com.jrc.msh.plugin.tc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class X509CertificateUtils {
    public static X509Certificate getCertificateFromByteArray(byte[] buff) throws CertificateException {
        return getCertificateFromInputStream(new ByteArrayInputStream(buff));
    }

    public static X509Certificate getCertificateFromInputStream(InputStream inputStream) throws CertificateException {
        CertificateFactory certFactory = null;
        certFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certFactory.generateCertificate(inputStream);

    }
}
