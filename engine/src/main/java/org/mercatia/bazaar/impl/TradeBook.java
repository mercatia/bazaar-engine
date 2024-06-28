package org.mercatia.bazaar.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.Offer;

public class TradeBook implements Jsonable {

    private Map<String, List<Offer>> bids;
    private Map<String, List<Offer>> asks;


    private record J(List<Jsony> bids, List<Jsony> asks) implements Jsony {
    };

    @Override
    public Jsony jsonify() {
        var _bids = new ArrayList<Jsony>();
        bids.values().forEach(l -> l.forEach(offer->_bids.add(offer.jsonify())));
        var _asks = new ArrayList<Jsony>();
        asks.values().forEach(l -> l.forEach(offer->_asks.add(offer.jsonify())));

        // final Map<String, List<Jsony>> _bids = bids.entrySet().stream()
        //         .collect(Collectors.toMap(e -> e.getKey(), (v) -> {
        //             return v.getValue().stream().map(x -> x.jsonify()).collect(Collectors.toList());
        //         }));

        // final Map<String, List<Jsony>> _asks = asks.entrySet().stream()
        //         .collect(Collectors.toMap(e -> e.getKey(), (v) -> {
        //             return v.getValue().stream().map(x -> x.jsonify()).collect(Collectors.toList());
        //         }));

        return new J(_bids, _asks);

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
