package com.mofadanqing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    @Select("SELECT p.*, c.name as category_name " +
            "FROM pms_product p " +
            "LEFT JOIN pms_category c ON p.category_id = c.id " +
            "WHERE p.status = 'ACTIVE' " +
            "AND (#{keyword} IS NULL OR p.name LIKE CONCAT('%', #{keyword}, '%') OR p.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{categoryId} IS NULL OR p.category_id = #{categoryId}) " +
            "ORDER BY p.create_time DESC")
    IPage<Product> selectProductPage(Page<Product> page, @Param("keyword") String keyword, @Param("categoryId") Long categoryId);
    
    @Select("SELECT p.*, c.name as category_name " +
            "FROM pms_product p " +
            "LEFT JOIN pms_category c ON p.category_id = c.id " +
            "WHERE p.id = #{id} AND p.status = 'ACTIVE'")
    Product selectProductWithCategory(@Param("id") Long id);
}