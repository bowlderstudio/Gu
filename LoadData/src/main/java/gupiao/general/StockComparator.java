package gupiao.general;

import java.util.Comparator;

public class StockComparator implements Comparator<StockDealRecord> {

    @Override
    public int compare(StockDealRecord stock1, StockDealRecord stock2) {
        if (stock1.getDate().compareTo(stock2.getDate()) > 0)
            return 1;
        else if (stock1.getDate().compareTo(stock2.getDate()) < 0)
            return -1;
        else
            return 0;
    }

}
