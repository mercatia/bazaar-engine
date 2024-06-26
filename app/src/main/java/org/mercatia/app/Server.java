package org.mercatia.app;

import java.util.Map;

import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.Transport;
import org.mercatia.bazaar.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
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
        this.world = world;
        eventBus = vertx.eventBus();

    }
    DeliveryOptions options = new DeliveryOptions().setSendTimeout(10000);
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

    }

    private void handleListMarkets(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String economdyID = routingContext.request().getParam("econ");
        var addr = String.format("economy/%s", economdyID);
        eventBus.request(addr, Transport.Actions.LIST_MARKETS, options)
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        JsonObject data = (JsonObject) ar.result().body();
                      
                        // JsonArray arr = new JsonArray();
                        // econ.getMarketNames().forEach((v) -> arr.add(v));
                        routingContext.response().putHeader("content-type", "application/json").end(data.encodePrettily());
            

                    } else {
                        sendError(404, response);
                    }
                });

    }

    private void tick(RoutingContext routingContext) {
        String econID = routingContext.request().getParam("econ");
        HttpServerResponse response = routingContext.response();      

        if (econID == null) {
            sendError(400, response);
        } else {

            var addr = String.format("economy/%s", econID);
            eventBus.request(addr, Transport.Actions.TICK, options)
            .onComplete(ar -> {
                if (ar.succeeded()) {
                    JsonObject data = (JsonObject) ar.result().body();
                    routingContext.response().putHeader("content-type", "application/json").end(data.encodePrettily());
                } else {
                    sendError(404, response);
                }
            });
        }
    }

    private void handleGetEconomy(RoutingContext routingContext) {
        String productID = routingContext.request().getParam("econ");
        HttpServerResponse response = routingContext.response();
        if (productID == null) {
            sendError(400, response);
        } else {
            var econ = this.world.get(productID);
            if (econ == null) {
                sendError(404, response);
            } else {
                var text = econ.getClass().toString();
                response.putHeader("content-type", "application/json").end(text);
            }
        }
    }

    private void handleGetMarket(RoutingContext routingContext) {
        String economdyID = routingContext.request().getParam("econ");
        String marketID = routingContext.request().getParam("market");
        HttpServerResponse response = routingContext.response();
        if (economdyID == null || marketID == null) {
            sendError(400, response);
        } else {
            var econ = this.world.get(economdyID);
            if (econ == null) {
                sendError(404, response);
            } else {
                var market = econ.getMarket(marketID);
                if (market == null) {
                    sendError(404, response);
                }

                var obj = JsonObject.mapFrom(market.jsonify());
                response.putHeader("content-type", "application/json").end(obj.encodePrettily());
            }
        }
    }

    private void handleGetAllAgent(RoutingContext routingContext) {
        String economdyID = routingContext.request().getParam("econ");
        String marketID = routingContext.request().getParam("market");

        HttpServerResponse response = routingContext.response();
        if (economdyID == null || marketID == null) {
            routingContext.fail(400);
        }

        var econ = this.world.get(economdyID);
        if (econ == null) {
            sendError(404, response);
        } else {
            var market = econ.getMarket(marketID);
            if (market == null) {
                sendError(404, response);
            }

            var arr = new JsonArray();
            market.getAgents().forEach(v -> {
                var agent = market.getAgent(v);
                var json = JsonObject.mapFrom(agent.jsonify());
                arr.add(json);
            });

            response.putHeader("content-type", "application/json").end(arr.encodePrettily());
        }

    }

    private void handleGetAgent(RoutingContext routingContext) {
        String economdyID = routingContext.request().getParam("econ");
        String marketID = routingContext.request().getParam("market");
        String agentID = routingContext.request().getParam("agentid");

        HttpServerResponse response = routingContext.response();
        if (economdyID == null || marketID == null) {
            routingContext.fail(400);
        }

        var econ = this.world.get(economdyID);
        if (econ == null) {
            sendError(404, response);
        } else {
            var market = econ.getMarket(marketID);
            if (market == null) {
                sendError(404, response);
            }

            var aid = Agent.ID.from(agentID);
            logger.info(aid.toString());

            var agent = market.getAgent(aid);

            var obj = JsonObject.mapFrom(agent.jsonify());

            response.putHeader("content-type", "application/json").end(obj.encodePrettily());
        }

    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }
}
