package org.mercatia.bazaar;

import java.util.concurrent.TimeUnit;

import org.mercatia.transport.Confirmation;
import org.mercatia.transport.EconomyReport;
import org.mercatia.transport.TransportGrpc;
import org.w3c.dom.events.Event;

import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

public class Transport implements AutoCloseable, MarketListener {
    private final TransportGrpc.TransportBlockingStub blockingStub;
    private Channel channel;

    /** Construct client for accessing HelloWorld server using the existing channel. */
    public Transport(Channel channel) {
        this.channel = channel;
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
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

        // Create a communication channel to the server, known as a Channel. Channels are thread-safe
        // and reusable. It is common to create channels at the beginning of your application and reuse
        // them until the application shuts down.
        //
        // For the example we use plaintext insecure credentials to avoid needing TLS certificates. To
        // use TLS, use TlsChannelCredentials instead.
        var target = "127.0.0.1:50051";
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        var transport = new Transport(channel);
        return transport;
    }

    public void close() {
        // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
        // resources the channel should be shut down when it will no longer be used. If it may be used
        // again leave it running.
        // channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Override
    public void handleEvent(Event evt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleEvent'");
    }

    @Override
    public void agentBankurpt(AgentBankruptEvent evt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'agentBankurpt'");
    }

}
