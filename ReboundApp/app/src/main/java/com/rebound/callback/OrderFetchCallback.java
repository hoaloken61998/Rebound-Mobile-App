package com.rebound.callback;

import com.rebound.models.Orders.Order;
import java.util.List;

public interface OrderFetchCallback {
    void onOrdersFetched(List<Order> orders);
}

