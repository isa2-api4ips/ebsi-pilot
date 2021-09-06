package eu.domibus.core.ebms3.spi;

import eu.domibus.core.ebms3.spi.model.SoapValidationResult;

import java.io.InputStream;

public abstract class SoapMessageValidationSPI {

    public abstract SoapValidationResult validateSoapMessage(InputStream is);
}
