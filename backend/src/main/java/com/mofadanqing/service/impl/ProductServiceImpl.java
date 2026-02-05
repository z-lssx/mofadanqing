package com.mofadanqing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.Category;
import com.mofadanqing.entity.Product;
import com.mofadanqing.mapper.CategoryMapper;
import com.mofadanqing.mapper.ProductMapper;
import com.mofadanqing.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    @Cacheable(value = "products", key = "#page.current + '-' + #page.size + '-' + #keyword + '-' + #categoryId")
    public IPage<Product> getProductPage(Page<Product> page, String keyword, Long categoryId) {
        QueryWrapper<Product> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.like("name", keyword).or().like("description", keyword);
        }
        
        // 约定：如果 categoryId 为 -1，则查所有状态（管理员模式）；否则默认查 ACTIVE（用户模式）
        if (categoryId == null || categoryId != -1L) {
             qw.eq("status", "ACTIVE");
        }
        
        if (categoryId != null && categoryId == -1L) {
            // 管理员查询所有，忽略 categoryId=-1 的条件
        } else if (categoryId != null) {
             qw.eq("category_id", categoryId);
        }

        qw.orderByDesc("create_time");
        return productMapper.selectPage(page, qw);
    }

    @Override
    @Cacheable(value = "product", key = "#id")
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product createProduct(Product product) {
        if (product.getStatus() == null) {
            product.setStatus("ACTIVE");
        }
        if (product.getStock() == null) {
            product.setStock(0);
        }
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product);
        return product;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"product", "products"}, key = "#product.id")
    public Product updateProduct(Product product) {
        Product existingProduct = productMapper.selectById(product.getId());
        if (existingProduct == null) {
            throw new RuntimeException("商品不存在");
        }
        
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        return product;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"product", "products"}, key = "#id")
    public void deleteProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        product.setStatus("INACTIVE");
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"product", "products"}, key = "#id")
    public void updateProductStatus(Long id, String status) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"product", "products"}, key = "#id")
    public void updateProductStock(Long id, Integer stock) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (stock < 0) {
            throw new RuntimeException("库存不能为负数");
        }
        product.setStock(stock);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    @Cacheable(value = "categories")
    public List<Category> getAllCategories() {
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "ACTIVE").orderByAsc("sort_order");
        return categoryMapper.selectList(wrapper);
    }
}
