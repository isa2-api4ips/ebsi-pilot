package eu.domibus.core.ebms3.spi;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

public abstract class DomibusMSHInInterceptorSPI extends AbstractPhaseInterceptor<SoapMessage> {

    public DomibusMSHInInterceptorSPI(String phase) {
        super(phase);
    }

    public DomibusMSHInInterceptorSPI(String i, String p) {
        super(i, p);
    }

    public DomibusMSHInInterceptorSPI(String phase, boolean uniqueId) {
        super(phase, uniqueId);
    }

    public DomibusMSHInInterceptorSPI(String i, String p, boolean uniqueId) {
        super(i, p, uniqueId);
    }

   abstract public String getIdentifier();
}
