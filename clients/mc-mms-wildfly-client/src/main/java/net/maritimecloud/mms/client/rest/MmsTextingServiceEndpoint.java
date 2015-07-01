package net.maritimecloud.mms.client.rest;

import net.maritimecloud.mms.client.MmsTextingService;
import net.maritimecloud.mms.client.model.ChatMessage;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * REST endpoint used for testing the {@code MmsTextingService}.
 */
@Path("/chat")
public class MmsTextingServiceEndpoint {

    @Inject
    private EntityManager em;

    @Inject
    MmsTextingService textingService;

    /**
     * Test endpoint.
     *
     * @param msg the text message
     * @param iterations the number of iterations
     * @return a HTML status page
     */
    @GET
    @Path("/broadcast")
    @Produces("text/html")
    public String broadcastMessage(@QueryParam("msg") String msg,
                                   @QueryParam("iterations") @DefaultValue("1") int iterations) {
        long t0 = System.currentTimeMillis();
        int cnt = 0;

        // If error conditions are introduced via the {@code ApplicationStarter} configuration class,
        // the iteration parameter can be used to verify that the new-transaction requirement of the
        // broadcastMessage method is adhered to.
        for (int i = 0; i < iterations; i++) {
            try {
                cnt += textingService.broadcastMessage(msg);
            } catch (Exception e) {
                System.out.println("Error " + e);
            }
        }

        // Generate a status page with the list of persisted chat messages.
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
