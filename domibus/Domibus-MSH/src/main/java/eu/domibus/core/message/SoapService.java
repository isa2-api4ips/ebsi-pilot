package eu.domibus.core.message;

import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.ebms3.common.model.Messaging;
import org.apache.cxf.binding.soap.SoapMessage;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface SoapService {

    Messaging getMessage(final SoapMessage message) throws IOException, JAXBException, EbMS3Exception;

    String getMessagingAsRAWXml(final SoapMessage message) throws IOException, EbMS3Exception, TransformerException;

    String getSoapEnvelopeAsRAWXml(final String messageId);
}
