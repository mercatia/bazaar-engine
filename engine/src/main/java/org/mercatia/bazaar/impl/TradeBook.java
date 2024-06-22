package org.mercatia.bazaar.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.Offer;

public class TradeBook implements Jsonable {

    private Map<String, List<Offer>> bids;
    private Map<String, List<Offer>> asks;

    private record J(Map<String, List<Offer>> bids, Map<String, List<Offer>> asks) implements Jsony {
    };

    @Override
    public Jsony jsonify() {
        Map<String,Jsony> bidss =  bids.entrySet().stream().collect<String,String>(Collectors.toMap(Map.Entry::getKey,
                        e -> e.jsonify()));
        return new J(bids, asks);
    }

    public TradeBook() {
        bids = new HashMap<>();
        asks = new HashMap<>();
    }

    public boolean ask(Offer offer) {
        if (!bids.containsKey(offer.good)) { // ?
            return false;
        }

        asks.get(offer.good).add(offer);
        return true;
    }

    public boolean bid(Offer offer) {
        if (!bids.containsKey(offer.good)) { // ?
            return false;
        }

        bids.get(offer.good).add(offer);
        return true;
    }

    public void register(String name) {
        bids.put(name, new ArrayList<>());
        asks.put(name, new ArrayList<>());
    }

    public List<Offer> getBids(String good) {
        return this.bids.get(good);
    }

    public List<Offer> getAsks(String good) {
        return this.asks.get(good);
    }

}
