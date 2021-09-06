package eu.domibus.ebsi.ebms3.interceptor;

import eu.domibus.core.ebms3.spi.DomibusMSHInInterceptorSPI;
import eu.domibus.ebsi.TimestampService;
import eu.domibus.ebsi.conf.EBSIConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.stereotype.Service;

/**
 * EBSI pilot project. Signature timestamp in interceptor.
 *
 * @author Joze Rihtarsic
 * @since 4.2.1
 */

@Service
public class EBSISignatureTimestampInInterceptor extends DomibusMSHInInterceptorSPI {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EBSISignatureTimestampInInterceptor.class);


    EBSIConfiguration configuration;
    TimestampService timestampService;

    public EBSISignatureTimestampInInterceptor(EBSIConfiguration configuration,
                                                TimestampService timestampService) {
        super(Phase.PRE_PROTOCOL);
        getAfter().add(SAAJInInterceptor.class.getName());
        this.configuration = configuration;
        this.timestampService = timestampService;
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {

        // Validate if timestamp is enabled for incomming messages.
        if (!configuration.isTimestampForIncomingMessagesEnabled()) {
            LOG.debug("Timestamp of outgoing messages is not enabled!");
            return;
        }

        timestampService.timestampSoapMessage(message);
    }


    public String getIdentifier() {
        return EBSISignatureTimestampInInterceptor.class.getName();
    }

    protected DomibusLogger getLogger() {
        return LOG;
    }

}
