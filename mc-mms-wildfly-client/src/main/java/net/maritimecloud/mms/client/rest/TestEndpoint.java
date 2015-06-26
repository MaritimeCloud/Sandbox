package net.maritimecloud.mms.client.rest;

import net.maritimecloud.mms.client.model.ChatMessage;
import net.maritimecloud.mms.client.MmsTextingService;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * REST endpoint.
 */
@Path("/chat")
public class TestEndpoint {

    @PersistenceContext
    private EntityManager em;

    @Inject
    MmsTextingService textingService;

    @GET
    @Path("/broadcast")
    @Produces("text/html")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String broadcastMessage(@QueryParam("msg") String msg,
                                   @QueryParam("iterations") @DefaultValue("1") int iterations) {
        long t0 = System.currentTimeMillis();
        int cnt = 0;
        for (int i = 0; i < iterations; i++) {
            try {
                cnt += textingService.broadcastMessage(msg);
            } catch (Exception e) {
                System.out.println("Error " + e);
            }
        }
        StringBuilder result = new StringBuilder("<btml><body>")
                .append(String.format("<h2>Sent %d messages</h2>", cnt))
                .append(String.format("<p><b>Sent in %d ms</b></p>", System.currentTimeMillis() - t0));
        em.createNamedQuery("ChatMessage.selectAll", ChatMessage.class).getResultList().forEach(
                m -> result.append(String.format("<small>date: %s, msg: %s</small><br/>", m.getDate(), m.getMsg()))
        );
        result.append("</body></html>");
        return result.toString();

    }

}
