package com.rebound.callback;

import com.rebound.models.Cart.Address;

public interface AddressCallback {
    void onAddressLoaded(Address address);
    void onError(String error);
}

