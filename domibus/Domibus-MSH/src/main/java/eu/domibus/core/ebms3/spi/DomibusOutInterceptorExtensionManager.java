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
public class DomibusOutInterceptorExtensionManager extends AbstractPhaseInterceptor<SoapMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusOutInterceptorExtensionManager.class);

    protected static final String INTERCEPTOR_OUT_CLASSES = DOMIBUS_EXTENSION_INTERCEPTOR_OUT_CLASSES;

    private List<DomibusMSHOutInterceptorSPI> outInterceptorSPIList;
    protected DomibusPropertyProvider domibusPropertyProvider;

    public DomibusOutInterceptorExtensionManager(DomibusPropertyProvider domibusPropertyProvider,
                                                 List<DomibusMSHOutInterceptorSPI> outInterceptorSPIList,
                                                 List<DomibusMSHInInterceptorSPI> inInterceptorSPIList) {
        super(Phase.SETUP);
        this.domibusPropertyProvider= domibusPropertyProvider;
        this.outInterceptorSPIList= outInterceptorSPIList;
    }


    @Override
    public void handleMessage(SoapMessage soapMessage) throws Fault {

        registerOutInterceptors(soapMessage);
    }

    public void registerOutInterceptors(SoapMessage soapMessage){
        if(outInterceptorSPIList.isEmpty()) {
            LOG.info("Not interceptors registered to the Domibus");
            return;
        }
        String interceptors  =domibusPropertyProvider.getProperty(INTERCEPTOR_OUT_CLASSES);
        if (StringUtils.isBlank(interceptors)){
            LOG.info("Not interceptors defined in domibus property: [{}]", INTERCEPTOR_OUT_CLASSES);
            return;
        }

        LOG.info("Interceptors defined in domibus property: [{}]",interceptors);

        // read out interceptor properties
        List<String> interceptorIdentifiers = Arrays.stream(interceptors.split(","))
                .map(String::trim).filter(StringUtils::isNotBlank).collect(Collectors.toList());

        LOG.info("Interceptors defined in domibus property list: [{}]",String.join(",", interceptorIdentifiers));

        final List<DomibusMSHOutInterceptorSPI> interceptorSPIList = this.outInterceptorSPIList.stream().
                filter(outInterceptorSPI -> {
                    boolean res =interceptorIdentifiers.contains(outInterceptorSPI.getIdentifier());
                    LOG.info("Test if interceptor [{}] (class, [{}]) is defined and exists [{}] in domibus property list:[{}]",
                            outInterceptorSPI.getIdentifier(),
                            outInterceptorSPI.getClass().getName(),
                            res,
                            String.join(",", interceptorIdentifiers), res);
                    return res;}).
                collect(Collectors.toList());
        LOG.info("Got [{}] from list with size: [{}]",interceptorSPIList.size(),outInterceptorSPIList.size() );

        if (LOG.isDebugEnabled()) {
            LOG.debug("Authorization spi:");
            interceptorSPIList.forEach(interceptorSpi -> LOG.debug(" identifier:[{}] for classes:[{}]", interceptorSpi.getIdentifier(), interceptorSpi.getClass()));
        }

        // register the interceptorSPIList
        interceptorSPIList.forEach(interceptorSpi -> soapMessage.getInterceptorChain().add(interceptorSpi));
    }
}
