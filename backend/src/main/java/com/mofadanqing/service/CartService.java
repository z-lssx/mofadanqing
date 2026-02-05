package com.mofadanqing.service;

import com.mofadanqing.entity.CartItem;
import java.util.List;

public interface CartService {
    List<CartItem> getUserCart(Long userId);
    void addToCart(Long userId, Long productId, Integer quantity);
    void updateQuantity(Long userId, Long cartItemId, Integer quantity);
    void removeCartItem(Long userId, Long cartItemId);
    void clearCart(Long userId);
}
