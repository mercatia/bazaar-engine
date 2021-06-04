package org.proterra.bazaarbot;

import java.util.List;

public interface Market {

    String getName();
    List<Good> getGoods();
    void onBankruptcy(Economy economy);
    void simulate(int rounds);
    

}