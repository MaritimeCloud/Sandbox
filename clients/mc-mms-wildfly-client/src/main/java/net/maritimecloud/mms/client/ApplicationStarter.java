package net.maritimecloud.mms.client;

import net.maritimecloud.core.id.MmsiId;
import net.maritimecloud.mms.client.generated.MmsClientService;
import net.maritimecloud.net.mms.MmsClientConfiguration;
import net.maritimecloud.util.geometry.PositionReader;
import net.maritimecloud.util.geometry.PositionTime;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 * Starts the MMS service
 */
@Startup
@Singleton
public class ApplicationStarter {

    public static final int MMSI = 777111222;
    public static final String MMS_HOST = "ws://mms.sandbox04.maritimecloud.net:80";

    public static final double ERROR_RATE = 0.0;

    @Inject
    MmsClientService mmsClientService;

    @Inject
    MmsTextingService textingService;

    @PostConstruct
    public void startMmsService() {

        textingService.setErrorRate(ERROR_RATE);

        MmsClientConfiguration conf = MmsClientConfiguration.create(new MmsiId(MMSI))
                .setHost(MMS_HOST)
                .setPositionReader(new PositionReader() {
                    @Override
                    public PositionTime getCurrentPosition() {
                        return PositionTime.create(55, 11, System.currentTimeMillis());
                    }
                });
        mmsClientService.start(conf);

    }
}
