package eu.domibus.ebsi.ebms3.interceptor;

import eu.domibus.core.ebms3.spi.DomibusMSHOutInterceptorSPI;
import eu.domibus.ebsi.TimestampService;
import eu.domibus.ebsi.conf.EBSIConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.stereotype.Service;

/**
 * EBSI pilot project. Signature timestamp out interceptor.
 *
 * @author Joze Rihtarsic
 * @since 4.2.1
 */

@Service
public class EBSISignatureTimestampOutInterceptor extends DomibusMSHOutInterceptorSPI {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EBSISignatureTimestampOutInterceptor.class);

    EBSIConfiguration configuration;
    TimestampService timestampService;

    public EBSISignatureTimestampOutInterceptor(EBSIConfiguration configuration,
                                                TimestampService timestampService) {
        super(Phase.USER_STREAM);
        this.configuration = configuration;
        this.timestampService = timestampService;
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        LOG.info("Handle timestamp for outgoing message!");
        // Validate if timestamp is enabled for outgoing message.
        if (!configuration.isTimestampForOutgoingMessagesEnabled()) {
            LOG.info("Timestamping is not enabled for outgoing messages!");
            return;
        }

        timestampService.timestampSoapMessage(message);
    }

    public String getIdentifier() {
        return EBSISignatureTimestampOutInterceptor.class.getName();
    }

    protected DomibusLogger getLogger() {
        return LOG;
    }
}
