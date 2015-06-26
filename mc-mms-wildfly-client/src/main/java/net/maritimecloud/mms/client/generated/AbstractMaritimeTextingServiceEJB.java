package net.maritimecloud.mms.client.generated;

import dma.messaging.AbstractMaritimeTextingService;
import dma.messaging.MaritimeText;
import net.maritimecloud.net.MessageHeader;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * To be Auto-generated.
 * Base class for Singleton implementation of the AbstractMaritimeTextingService MMS service
 */
public abstract class AbstractMaritimeTextingServiceEJB extends AbstractMaritimeTextingService {

    @Inject
    protected MmsClientService mmsClientService;

    /** Called when the Singleton is instantiated. Register the MMS service */
    @PostConstruct
    public void registerService() {
        final Class<? extends AbstractMaritimeTextingServiceEJB> ejbClass = getClass();
        if (!ejbClass.isAnnotationPresent(javax.ejb.Singleton.class) &&
                !ejbClass.isAnnotationPresent(javax.inject.Singleton.class)) {
            throw new RuntimeException("The " + ejbClass + " must be a @Singleton");
        }

        mmsClientService.endpointRegister(new AbstractMaritimeTextingService() {
            @Override
            protected void sendMessage(MessageHeader header, MaritimeText msg) {
                MmsClientService.getBean(ejbClass).sendMessage(header, msg);
            }
        });
    }
}
