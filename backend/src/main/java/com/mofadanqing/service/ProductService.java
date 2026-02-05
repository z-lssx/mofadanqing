package com.mofadanqing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.Product;

import java.util.List;

public interface ProductService {
    
    /**
     * 获取商品列表（分页）
     */
    IPage<Product> getProductPage(Page<Product> page, String keyword, Long categoryId);
    
    /**
     * 获取商品详情
     */
    Product getProductById(Long id);
    
    /**
     * 创建商品
     */
    Product createProduct(Product product);
    
    /**
     * 更新商品
     */
    Product updateProduct(Product product);
    
    /**
     * 删除商品
     */
    void deleteProduct(Long id);
    
    /**
     * 更新商品状态
     */
    void updateProductStatus(Long id, String status);
    
    /**
     * 更新商品库存
     */
    void updateProductStock(Long id, Integer stock);
    
    /**
     * 获取所有分类
     */
    List<com.mofadanqing.entity.Category> getAllCategories();
}