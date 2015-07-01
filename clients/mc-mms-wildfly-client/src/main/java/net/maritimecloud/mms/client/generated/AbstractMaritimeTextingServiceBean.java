package net.maritimecloud.mms.client.generated;

import dma.messaging.AbstractMaritimeTextingService;
import dma.messaging.MaritimeText;
import net.maritimecloud.net.MessageHeader;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static net.maritimecloud.mms.client.generated.MmsClientService.getMmsServiceBean;

/**
 * To be Auto-generated.
 * <p/>
 * Base class for Singleton implementation of the AbstractMaritimeTextingService MMS service.
 * <p/>
 * Concrete implementation should be annotated with @Singleton and @Startup annotations to
 * ensure that the service is registered properly upon start-up.
 */
public abstract class AbstractMaritimeTextingServiceBean extends AbstractMaritimeTextingService {

    @Inject
    protected MmsClientService mmsClientService;

    /** Called when the Singleton is instantiated. Register the MMS service */
    @PostConstruct
    public void registerService() {
        final Class<? extends AbstractMaritimeTextingServiceBean> ejbClass = getClass();
        if (!ejbClass.isAnnotationPresent(javax.ejb.Singleton.class) &&
                !ejbClass.isAnnotationPresent(javax.inject.Singleton.class)) {
            throw new RuntimeException("The " + ejbClass + " must be a @Singleton");
        }

        mmsClientService.endpointRegister(new AbstractMaritimeTextingService() {
            @Override
            protected void sendMessage(MessageHeader header, MaritimeText msg) {
                getMmsServiceBean(ejbClass).sendMessage(header, msg);
            }
        });
    }
}
