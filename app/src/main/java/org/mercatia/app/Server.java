package org.mercatia.app;

import static org.mercatia.bazaar.Transport.Actions.GET_AGENT;
import static org.mercatia.bazaar.Transport.Actions.GET_ALL_AGENTS;
import static org.mercatia.bazaar.Transport.Actions.GET_MARKET;
import static org.mercatia.bazaar.Transport.Actions.LIST_MARKETS;

import java.util.Map;

import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.Transport;
import org.mercatia.bazaar.Transport.BusMsgOptions;
import org.mercatia.bazaar.Transport.IntraMessage;
import org.mercatia.bazaar.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

public class Server extends AbstractVerticle {

    static Logger logger = LoggerFactory.getLogger(Server.class);

    Map<String, Economy> world;

    private EventBus eventBus;

    public Server(Map<String, Economy> world) {
        super();
        this.world = world;

    }

    @Override
    public void start() throws Exception {
        logger.info("Vertx Router");
        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create(".*.")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowCredentials(true)
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Authorization")
                .allowedHeader("Access-Control-Allow-Method")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Content-Type"));

        router.route().handler(BodyHandler.create());
        router.get("/economy/:econ").handler(this::handleGetEconomy);
        router.post("/economy/:econ").handler(this::tick);
        router.get("/economy/:econ/market/").handler(this::handleListMarkets);

        router.get("/economy/:econ/market/:market").handler(this::handleGetMarket);
        router.get("/economy/:econ/market/:market/agent/").handler(this::handleGetAllAgent);
        router.get("/economy/:econ/market/:market/agent/:agentid").handler(this::handleGetAgent);

        vertx.createHttpServer().requestHandler(router).listen(3000);

        eventBus = vertx.eventBus();

    }

    private void handleListMarkets(RoutingContext routingContext) {
        String economdyID = routingContext.request().getParam("econ");

        var addr = String.format("economy/%s", economdyID);
        var m = IntraMessage.actionMessage(LIST_MARKETS, 10000);

        handleRequest(addr, m, routingContext);
    }

    private void handleGetMarket(RoutingContext routingContext) {
        String economdyID = routingContext.request().getParam("econ");
        String marketID = routingContext.request().getParam("market");
        HttpServerResponse response = routingContext.response();

        if (economdyID == null || marketID == null) {
            sendError(400, response);
        } else {
            var addr = String.format("economy/%s/market/%s", economdyID, marketID);
            var m = IntraMessage.actionMessage(GET_MARKET, 10000);

            handleRequest(addr, m, routingContext);
        }

    }

    private void tick(RoutingContext routingContext) {
        String econID = routingContext.request().getParam("econ");
        HttpServerResponse response = routingContext.response();

        if (econID == null) {
            sendError(400, response);
        } else {
            var m = IntraMessage.actionMessage(Transport.Actions.TICK);
            var addr = String.format("economy/%s", econID);

            handleRequest(addr, m, routingContext);
        }
    }

    private void handleGetEconomy(RoutingContext routingContext) {
        String productID = routingContext.request().getParam("econ");

        if (productID == null) {
            routingContext.fail(400);
        } else {
            var addr = String.format("economy/%s/", productID);
            var m = IntraMessage.actionMessage(GET_ALL_AGENTS, 10000);
            handleRequest(addr, m, routingContext);

        }

    }

    private void handleGetAllAgent(RoutingContext routingContext) {
        String economdyID = routingContext.request().getParam("econ");
        String marketID = routingContext.request().getParam("market");

        if (economdyID == null || marketID == null) {
            routingContext.fail(400);
        } else {
            var addr = String.format("economy/%s/market/%s", economdyID, marketID);
            var m = IntraMessage.actionMessage(GET_ALL_AGENTS, 10000);
            handleRequest(addr, m, routingContext);
        }

    }

    private void handleGetAgent(RoutingContext routingContext) {
        String economdyID = routingContext.request().getParam("econ");
        String marketID = routingContext.request().getParam("market");
        String agentID = routingContext.request().getParam("agentid");

        if (economdyID == null || marketID == null) {
            routingContext.fail(400);
        } else {
            var addr = String.format("economy/%s/market/%s/agent/%s", economdyID, marketID, agentID);

            var m = IntraMessage.actionMessage(GET_AGENT, 10000);
            handleRequest(addr, m, routingContext);
        }

    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    private void handleRequest(String addr, BusMsgOptions m, RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        eventBus.request(addr, m.msg(), m.options())
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        JsonObject data = (JsonObject) ar.result().body();
                        routingContext.response().putHeader("content-type", "application/json")
                                .end(data.encodePrettily());
                    } else {
                        sendError(404, response);
                    }
                });
    }

}
