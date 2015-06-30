package net.maritimecloud.mms.client;

import dma.messaging.MaritimeText;
import dma.messaging.MaritimeTextingNotificationSeverity;
import dma.messaging.MaritimeTextingService;
import net.maritimecloud.mms.client.generated.AbstractMaritimeTextingServiceEJB;
import net.maritimecloud.mms.client.model.ChatMessage;
import net.maritimecloud.net.MessageHeader;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.QueryParam;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MMS service interface.
 */
@Startup
@Singleton
public class MmsTextingService extends AbstractMaritimeTextingServiceEJB {

    @Inject
    private EntityManager em;

    @Resource
    SessionContext sessionContext;

    public double errorRate = 0.0;

    /** Called when an MMS client calls this registered MMS chat service */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendMessage(MessageHeader header, MaritimeText msg) {
        System.out.println("*** Message Received [" + header.getSender() + "]: " + msg.getMsg());
        persistMessage(msg);
    }

    /** Broadcast the message to all registered chat services */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public int broadcastMessage(@QueryParam("msg") String msg) throws Exception {
        if (!mmsClientService.isStarted()) {
            return 0;
        }

        MaritimeText txt = new MaritimeText();
        txt.setMsg(msg);
        txt.setSeverity(MaritimeTextingNotificationSeverity.MESSAGE);
        persistMessage(txt);

        AtomicInteger cnt = new AtomicInteger(0);
        try {
            mmsClientService.getMmsClient().endpointLocate(MaritimeTextingService.class)
                    .findAll()
                    .get(5, TimeUnit.SECONDS)
                    .forEach(s -> {
                        try {
                            s.sendMessage(txt).get(4, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.println("*** Message Sent [" + s.getRemoteId() + "]: " + msg);
                        cnt.incrementAndGet();
                    });

            // Test DB access
            //System.out.println("There are " + greetingService.findAll().size() + " in the DB");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cnt.intValue();
    }

    /** Persist the message */
    private void persistMessage(MaritimeText msg) {
        ChatMessage m = new ChatMessage(msg.getSeverity().toString(), msg.getMsg());
        em.persist(m);

        // Introduce some errors into the system
        if (Math.random() < errorRate) {
            sessionContext.setRollbackOnly();
            throw new RuntimeException("An error occurred");
        }

        System.out.println("DB contains " +
                em.createNamedQuery("ChatMessage.selectAll").getResultList().size()
                + " chat messages");
    }
}
