package com.mofadanqing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mofadanqing.entity.CartItem;
import com.mofadanqing.entity.Product;
import com.mofadanqing.mapper.CartItemMapper;
import com.mofadanqing.mapper.ProductMapper;
import com.mofadanqing.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<CartItem> getUserCart(Long userId) {
        List<CartItem> items = cartItemMapper.selectList(new QueryWrapper<CartItem>().eq("user_id", userId));
        // 填充商品详情
        for (CartItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                item.setProductName(product.getName());
                item.setProductImage(product.getCoverImg());
                item.setPrice(product.getPrice());
            }
        }
        return items;
    }

    @Override
    @Transactional
    public void addToCart(Long userId, Long productId, Integer quantity) {
        CartItem existing = cartItemMapper.selectOne(new QueryWrapper<CartItem>()
                .eq("user_id", userId)
                .eq("product_id", productId));

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setUpdateTime(LocalDateTime.now());
            cartItemMapper.updateById(existing);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            newItem.setCreateTime(LocalDateTime.now());
            newItem.setUpdateTime(LocalDateTime.now());
            cartItemMapper.insert(newItem);
        }
    }

    @Override
    public void updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        if (quantity <= 0) {
            removeCartItem(userId, cartItemId);
            return;
        }
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item != null && item.getUserId().equals(userId)) {
            item.setQuantity(quantity);
            item.setUpdateTime(LocalDateTime.now());
            cartItemMapper.updateById(item);
        }
    }

    @Override
    public void removeCartItem(Long userId, Long cartItemId) {
        cartItemMapper.delete(new QueryWrapper<CartItem>()
                .eq("id", cartItemId)
                .eq("user_id", userId));
    }

    @Override
    public void clearCart(Long userId) {
        cartItemMapper.delete(new QueryWrapper<CartItem>().eq("user_id", userId));
    }
}
