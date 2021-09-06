package eu.domibus.core.ebms3.spi;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

public abstract class DomibusMSHOutInterceptorSPI extends AbstractPhaseInterceptor<SoapMessage> {

    public DomibusMSHOutInterceptorSPI(String phase) {
        super(phase);
    }

    public DomibusMSHOutInterceptorSPI(String i, String p) {
        super(i, p);
    }

    public DomibusMSHOutInterceptorSPI(String phase, boolean uniqueId) {
        super(phase, uniqueId);
    }

    public DomibusMSHOutInterceptorSPI(String i, String p, boolean uniqueId) {
        super(i, p, uniqueId);
    }

   abstract public String getIdentifier();
}
