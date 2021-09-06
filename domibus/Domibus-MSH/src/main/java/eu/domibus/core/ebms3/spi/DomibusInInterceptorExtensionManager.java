package eu.domibus.core.ebms3.spi;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EXTENSION_INTERCEPTOR_IN_CLASSES;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EXTENSION_INTERCEPTOR_OUT_CLASSES;

@Service
public class DomibusInInterceptorExtensionManager extends AbstractPhaseInterceptor<SoapMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusInInterceptorExtensionManager.class);

    protected static final String INTERCEPTOR_IN_CLASSES = DOMIBUS_EXTENSION_INTERCEPTOR_IN_CLASSES;

    private List<DomibusMSHInInterceptorSPI> inInterceptorSPIList;

    protected DomibusPropertyProvider domibusPropertyProvider;

    public DomibusInInterceptorExtensionManager(DomibusPropertyProvider domibusPropertyProvider,
                                                List<DomibusMSHOutInterceptorSPI> outInterceptorSPIList,
                                                List<DomibusMSHInInterceptorSPI> inInterceptorSPIList) {
        super(Phase.RECEIVE);
        this.domibusPropertyProvider= domibusPropertyProvider;
        this.inInterceptorSPIList= inInterceptorSPIList;
    }


    @Override
    public void handleMessage(SoapMessage soapMessage) throws Fault {

        registerInInterceptors(soapMessage);
    }


    public void registerInInterceptors(SoapMessage soapMessage){
        if(inInterceptorSPIList.isEmpty()) {
            LOG.info("No IN interceptors registered to the Domibus");
            return;
        }
        String interceptors  =domibusPropertyProvider.getProperty(INTERCEPTOR_IN_CLASSES);
        if (StringUtils.isBlank(interceptors)){
            LOG.info("Not In interceptors defined in domibus property: [{}]", INTERCEPTOR_IN_CLASSES);
            return;
        }

        LOG.info("Interceptors defined in domibus property: [{}]",interceptors);


        // read out interceptor properties
        List<String> interceptorIdentifiers = Arrays.stream(interceptors.split(","))
                .map(String::trim).filter(StringUtils::isNotBlank).collect(Collectors.toList());

        LOG.info("Interceptors defined in domibus property list: [{}]",String.join(",", interceptorIdentifiers));

        final List<DomibusMSHInInterceptorSPI> interceptorSPIList = this.inInterceptorSPIList.stream().
                filter(inInterceptorSPI -> {
                    boolean res =interceptorIdentifiers.contains(inInterceptorSPI.getIdentifier());
                    LOG.info("Test if interceptor [{}] (class, [{}]) is defined and exists [{}] in domibus property list:[{}]",
                            inInterceptorSPI.getIdentifier(),
                            inInterceptorSPI.getClass().getName(),
                            res,
                            String.join(",", interceptorIdentifiers), res);
                    return res;}).
                collect(Collectors.toList());
        LOG.info("Got [{}] from list with size: [{}]",interceptorSPIList.size(),inInterceptorSPIList.size() );

        if (LOG.isDebugEnabled()) {
            LOG.debug("Authorization spi:");
            interceptorSPIList.forEach(interceptorSpi -> LOG.debug(" identifier:[{}] for classes:[{}]", interceptorSpi.getIdentifier(), interceptorSpi.getClass()));
        }

        // register the interceptorSPIList
        interceptorSPIList.forEach(interceptorSpi -> soapMessage.getInterceptorChain().add(interceptorSpi));
    }

}
