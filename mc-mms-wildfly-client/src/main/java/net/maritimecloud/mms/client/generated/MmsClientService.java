package net.maritimecloud.mms.client.generated;

import net.maritimecloud.net.EndpointImplementation;
import net.maritimecloud.net.mms.MmsClient;
import net.maritimecloud.net.mms.MmsClientConfiguration;

import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * To be Auto-generated.
 * <p/>
 * Main MMS interface.
 * The underlying MMS service must be started using the {@code start(MmsClientConfiguration conf)} method.
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class MmsClientService {

    private MmsClient mmsClient;

    private List<EndpointImplementation> endpoints = new CopyOnWriteArrayList<>();

    /**
     * Registers the given MMS service endpoint
     * @param service the MMS service endpoint
     */
    @Lock(LockType.WRITE)
    public void endpointRegister(EndpointImplementation service) {
        if (!endpoints.contains(service)) {
            endpoints.add(service);
        }
        if (isStarted()) {
            mmsClient.endpointRegister(service);
        }
    }

    /**
     * Starts the MMS connection using the given configuration
     * @param conf the configuration
     */
    @Lock(LockType.WRITE)
    public void start(MmsClientConfiguration conf) {
        if (isStarted()) {
            shutdown();
        }

        // Start the client - throws a RuntimeException in case of failure
        mmsClient = conf.build();

        // Register the MMS service endpoints
        endpoints.forEach(this::endpointRegister);
    }

    /**
     * Stops the MMS connection.
     * Also called automatically when the application terminates.
     */
    @PreDestroy
    @Lock(LockType.WRITE)
    public void shutdown() {
        if (isStarted()) {
            try {
                mmsClient.shutdown();
                mmsClient.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mmsClient = null;
        }
    }

    /** Returns the MMS client. May be null, if the service has not been started yet */
    public MmsClient getMmsClient() {
        return mmsClient;
    }

    /**
     * Returns if the MMS service has been started
     * @return if the MMS service has been started
     */
    public boolean isStarted() {
        return mmsClient != null;
    }

    /** Utitlity method - Looks up the managed bean with the given class */
    @SuppressWarnings("unchecked")
    public static <T extends EndpointImplementation> T getMmsServiceBean(Class<T> clazz) {
        BeanManager beanManager = CDI.current().getBeanManager();
        Bean<T> bean = (Bean<T>) beanManager.resolve(beanManager.getBeans(clazz));
        CreationalContext<T> cc = beanManager.createCreationalContext(bean);
        return (T)beanManager.getReference(bean, clazz, cc);
    }

}
