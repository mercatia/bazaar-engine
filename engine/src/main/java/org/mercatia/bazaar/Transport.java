package org.mercatia.bazaar;

import org.mercatia.events.AgentBankruptEvent;
import org.mercatia.events.MarketEventListener;
import org.mercatia.transport.Confirmation;
import org.mercatia.transport.EconomyReport;
import org.mercatia.transport.TransportGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class Transport implements AutoCloseable, MarketEventListener {

    public static record BusMsgOptions(JsonObject msg, DeliveryOptions options) {
    };

    public static class IntraMessage {

        private JsonObject body;
        private MultiMap headers;

        public static IntraMessage busmsg(Message<JsonObject> message) {
            return new IntraMessage(message);
        }

        private IntraMessage(Message<JsonObject> message) {
            this.body = message.body();
            this.headers = message.headers();
        }

        public Actions getAction() {
            var a = this.body.getString(MSG_KEYS.ACTION_KEY.name());
            if (a != null) {
                return Actions.valueOf(a);
            } else {
                return Actions.UNDEFINED;
            }
        }

        public static BusMsgOptions actionMessage(Actions action) {
            var msg = new JsonObject();
            msg.put(MSG_KEYS.ACTION_KEY.name(), action.name());
            return new BusMsgOptions(msg, deliveryWithAction(MSG_TYPE.ACTION));
        }

        public static BusMsgOptions actionMessage(Actions action, long timeout) {
            var msg = new JsonObject();
            msg.put(MSG_KEYS.ACTION_KEY.name(), action.name());
            return new BusMsgOptions(msg, deliveryWithAction(MSG_TYPE.ACTION).setSendTimeout(timeout));

        }

        public static DeliveryOptions deliveryWithAction(MSG_TYPE type) {
            return new DeliveryOptions().addHeader(MSG_KEYS.TYPE_KEY.name(), type.name());
        }

        public boolean isAction() {
            return headers.contains(MSG_KEYS.TYPE_KEY.name()) &&
                    headers.get(MSG_KEYS.TYPE_KEY.name()).equals(MSG_TYPE.ACTION.name());
        }

        public boolean isEvent() {
            return headers.contains(MSG_KEYS.TYPE_KEY.name()) &&
                    headers.get(MSG_KEYS.TYPE_KEY.name()).equals(MSG_TYPE.EVENT.name());
        }
    }

    public static enum Actions {
        LIST_MARKETS, TICK, UNDEFINED, GET_MARKET, GET_ALL_AGENTS, GET_AGENT
    }

    /**
     * Action send to things to get back an event
     */
    public static enum MSG_TYPE {
        ACTION, EVENT
    }

    public static enum MSG_KEYS {
        ACTION_KEY, TYPE_KEY
    }

    static Logger logger = LoggerFactory.getLogger(Transport.class);
    private final TransportGrpc.TransportBlockingStub blockingStub;
    private Channel channel;

    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    public Transport(Channel channel) {
        this.channel = channel;
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's
        // responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to
        // reuse Channels.
        blockingStub = TransportGrpc.newBlockingStub(channel);
    }

    /** Say hello to server. */
    public void greet(String name) {
        EconomyReport report = EconomyReport.newBuilder().setName(name).build();
        Confirmation response;
        try {
            response = blockingStub.sendEconomyReport(report);
        } catch (StatusRuntimeException e) {
            return;
        }
    }

    public static Transport configure() {

        logger.info("Configure....");
        // Create a communication channel to the server, known as a Channel. Channels
        // are thread-safe
        // and reusable. It is common to create channels at the beginning of your
        // application and reuse
        // them until the application shuts down.
        //
        // For the example we use plaintext insecure credentials to avoid needing TLS
        // certificates. To
        // use TLS, use TlsChannelCredentials instead.
        var target = "127.0.0.1:50051";
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        var transport = new Transport(channel);
        return transport;
    }

    public void close() {
        // ManagedChannels use resources like threads and TCP connections. To prevent
        // leaking these
        // resources the channel should be shut down when it will no longer be used. If
        // it may be used
        // again leave it running.
        // channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }


    @Override
    public void agentBankrupt(AgentBankruptEvent event) {

    }

}
