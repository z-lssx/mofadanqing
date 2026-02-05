<template>
  <div class="products">
    <div class="header">
      <h1>商品列表</h1>
      <div class="filters">
        <select v-model="selectedCategory" @change="loadProducts">
          <option value="">全部分类</option>
          <option v-for="category in categories" :key="category.id" :value="category.id">
            {{ category.name }}
          </option>
        </select>
        <input 
          v-model="keyword" 
          @input="loadProducts"
          placeholder="搜索商品..." 
          class="search-input"
        />
      </div>
    </div>

    <div class="product-grid">
      <div v-for="product in products" :key="product.id" class="product-card">
        <img :src="product.coverImg || 'https://via.placeholder.com/300x200'" :alt="product.name" />
        <div class="product-info">
          <h3>{{ product.name }}</h3>
          <p class="price">¥{{ product.price }}</p>
          <p class="stock">库存: {{ product.stock }}</p>
          <p class="description">{{ product.description }}</p>
          <div class="actions">
            <button @click="viewProduct(product.id)" class="btn-view">查看详情</button>
            <button @click="addToCart(product)" class="btn-cart">加入购物车</button>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination">
      <button @click="changePage(currentPage - 1)" :disabled="currentPage <= 1">上一页</button>
      <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页</span>
      <button @click="changePage(currentPage + 1)" :disabled="currentPage >= totalPages">下一页</button>
    </div>
  </div>
</template>

<script>
import axios from '../utils/axios'

export default {
  name: 'Products',
  data() {
    return {
      products: [],
      categories: [],
      selectedCategory: '',
      keyword: '',
      currentPage: 1,
      pageSize: 20,
      totalPages: 1,
      totalElements: 0
    }
  },
  mounted() {
    this.loadProducts()
    this.loadCategories()
  },
  methods: {
    async loadProducts() {
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize,
          keyword: this.keyword,
          categoryId: this.selectedCategory
        }
        
        const response = await axios.get('/products', { params })
        if (response.code === 200) {
          const data = response.data
          this.products = data.content || data.records || []
          this.totalPages = data.pages || Math.ceil((data.total || 0) / this.pageSize)
          this.totalElements = data.total || 0
        }
      } catch (error) {
        console.error('加载商品失败:', error)
        alert('加载商品失败')
      }
    },
    
    async loadCategories() {
      try {
        const response = await axios.get('/products/categories')
        if (response.code === 200) {
          this.categories = response.data
        }
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    },
    
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page
        this.loadProducts()
      }
    },
    
    viewProduct(id) {
      this.$router.push(`/product/${id}`)
    },
    
    addToCart(product) {
      import('../utils/cartState').then(({ cartState }) => {
        cartState.addItem(product)
      })
    }
  }
}
</script>

<style scoped>
.products {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.filters {
  display: flex;
  gap: 15px;
}

.filters select,
.filters input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.search-input {
  width: 200px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.product-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  background: white;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.product-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
  margin: 5px 0;
}

.stock {
  font-size: 12px;
  color: #666;
  margin: 5px 0;
}

.description {
  font-size: 14px;
  color: #666;
  margin: 10px 0;
  line-height: 1.4;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.actions button {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s ease;
}

.btn-view {
  background-color: #3498db;
  color: white;
}

.btn-view:hover {
  background-color: #2980b9;
}

.btn-cart {
  background-color: #2ecc71;
  color: white;
}

.btn-cart:hover {
  background-color: #27ae60;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 40px;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination button:hover:not(:disabled) {
  background-color: #f8f9fa;
  border-color: #007bff;
}

.pagination button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
