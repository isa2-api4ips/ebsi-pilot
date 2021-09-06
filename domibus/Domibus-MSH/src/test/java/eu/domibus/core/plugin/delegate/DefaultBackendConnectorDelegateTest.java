package eu.domibus.core.plugin.delegate;

import eu.domibus.api.util.ClassUtil;
import eu.domibus.common.MessageDeletedBatchEvent;
import eu.domibus.common.MessageDeletedEvent;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.BackendConnectorService;
import eu.domibus.core.plugin.notification.AsyncNotificationConfigurationService;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.NotificationListenerService;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@RunWith(JMockit.class)
public class DefaultBackendConnectorDelegateTest {

    @Injectable
    ClassUtil classUtil;

    @Injectable
    protected RoutingService routingService;

    @Injectable
    protected BackendConnectorProvider backendConnectorProvider;

    @Injectable
    protected BackendConnectorService backendConnectorService;

    @Injectable
    protected AsyncNotificationConfigurationService asyncNotificationConfigurationService;

    @Tested
    DefaultBackendConnectorDelegate defaultBackendConnectorDelegate;

    @Test
    public void testMessageReceive(@Injectable final BackendConnector backendConnector,
                                   @Injectable final MessageReceiveFailureEvent event) throws Exception {
        defaultBackendConnectorDelegate.messageReceiveFailed(backendConnector, event);

        new Verifications() {{
            backendConnector.messageReceiveFailed(event);
        }};
    }

    @Test
    public void messageDeletedEvent(@Injectable MessageDeletedEvent event,
                                    @Injectable BackendConnector backendConnector) {
        String backend = "mybackend";

        new Expectations(defaultBackendConnectorDelegate) {{
            backendConnectorProvider.getBackendConnector(backend);
            result = backendConnector;

            defaultBackendConnectorDelegate.callNotificationListerForMessageDeletedEvent(backendConnector, event);
            times = 1;
        }};

        defaultBackendConnectorDelegate.messageDeletedEvent(backend, event);

        new Verifications() {{
            backendConnector.messageDeletedEvent(event);
        }};
    }

    @Test
    public void messageDeletedBatchEvent(@Injectable MessageDeletedBatchEvent event,
                                    @Injectable BackendConnector backendConnector) {
        String backend = "mybackend";

        new Expectations(defaultBackendConnectorDelegate) {{
            backendConnectorProvider.getBackendConnector(backend);
            result = backendConnector;
        }};

        defaultBackendConnectorDelegate.messageDeletedBatchEvent(backend, event);

        new Verifications() {{
            backendConnector.messageDeletedBatchEvent(event);
        }};
    }

    @Test
    public void callNotificationListerForMessageDeletedEvent(@Injectable BackendConnector<?, ?> backendConnector,
                                                             @Injectable MessageDeletedEvent event,
                                                             @Injectable NotificationListenerService asyncNotificationConfiguration) {
        new Expectations(defaultBackendConnectorDelegate) {{
            defaultBackendConnectorDelegate.shouldCallNotificationListerForMessageDeletedEvent(backendConnector);
            result = true;

            asyncNotificationConfigurationService.getAsyncPluginConfiguration(backendConnector.getName());
            result = asyncNotificationConfiguration;

            backendConnectorService.isInstanceOfNotificationListener(asyncNotificationConfiguration);
            result = true;

            event.getMessageId();
            result = "abc";

        }};

        defaultBackendConnectorDelegate.callNotificationListerForMessageDeletedEvent(backendConnector, event);

        new Verifications() {{
            asyncNotificationConfiguration.deleteMessageCallback(anyString);
        }};
    }

    @Test
    public void shouldCallNotificationListerForMessageDeletedEvent(@Injectable BackendConnector<?, ?> backendConnector) {
        new Expectations() {{
            backendConnectorService.isListerAnInstanceOfAsyncPluginConfiguration(backendConnector);
            result = true;
        }};

        assertFalse(defaultBackendConnectorDelegate.shouldCallNotificationListerForMessageDeletedEvent(backendConnector));
    }
}
