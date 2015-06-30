package net.maritimecloud.mms.client.generated;

import net.maritimecloud.net.EndpointImplementation;
import net.maritimecloud.net.mms.MmsClient;
import net.maritimecloud.net.mms.MmsClientConfiguration;

import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * To be Auto-generated.
 * MMS service interface.
 * Requires that the application code has a @Producer for the MmsClientConfiguration
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class MmsClientService {

    private MmsClient mmsClient;

    private MmsClientConfiguration conf;

    private boolean started;

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
        if (isConnected()) {
            mmsClient.endpointRegister(service);
        }
    }

    /**
     * Called every minute to check the MMS status
     */
    @Schedule(persistent=false, second="50", minute="*", hour="*", dayOfWeek="*", year="*")
    @Lock(LockType.WRITE)
    public void checkMmsConnection() {
        if (started && !isConnected()) {
            initMmsClient();
        } else if (!started && isConnected()) {
            closeMmsClient();
        }
    }

    /** Returns the MMS client */
    public MmsClient getMmsClient() {
        return mmsClient;
    }

    /**
     * Starts the MMS connection using the given configuration
     * @param conf the configuration
     */
    @Lock(LockType.WRITE)
    public void start(MmsClientConfiguration conf) {
        this.conf = Objects.requireNonNull(conf);
        started = true;
        checkMmsConnection();
    }

    /**
     * Stops the MMS connection
     */
    @PreDestroy
    @Lock(LockType.WRITE)
    public void shutdown() {
        this.conf = null;
        started = false;
        checkMmsConnection();
    }

    /**
     * Returns if the MMS connections has been started
     * @return if the MMS connections has been started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Returns if the MMS connections is started and connected
     * @return if the MMS connections is started and connected
     */
    public boolean isConnected() {
        return started && mmsClient != null;
    }

    /** Starts the MMS client */
    private boolean initMmsClient() {
        mmsClient = conf.build();
        if (mmsClient != null) {
            // Register the MMS service endpoints
            endpoints.forEach(this::endpointRegister);

            return true;
        } else {
            return false;
        }
    }

    /** Terminate the MMS client */
    private void closeMmsClient() {
        if (mmsClient != null) {
            try {
                mmsClient.shutdown();
                mmsClient.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mmsClient = null;
        }
    }

    /** Utitlity method - Looks up the managed bean with the given class */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        BeanManager beanManager = CDI.current().getBeanManager();
        Bean<T> bean = (Bean<T>) beanManager.resolve(beanManager.getBeans(clazz));
        CreationalContext<T> cc = beanManager.createCreationalContext(bean);
        return (T)beanManager.getReference(bean, clazz, cc);
    }

}
