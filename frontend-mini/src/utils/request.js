// 封装 uni.request
// 使用局域网 IP，确保真机调试时手机和电脑在同一 WiFi 下
// 如果使用 cpolar 内网穿透，请替换为公网域名
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://192.168.1.26:8080/api';

const request = (options) => {
  return new Promise((resolve, reject) => {
    // 获取 Token
    const token = uni.getStorageSync('token');
    
    // 构建请求头
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    };
    
    if (token) {
      header['Authorization'] = `Bearer ${token}`;
    }

    // 发起请求
    uni.request({
      url: baseURL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: header,
      success: (res) => {
        // 统一处理响应
        if (res.statusCode === 200) {
           // 假设后端标准结构是 { code: 200, data: ... }
           // 但这里我们先返回 res.data 让调用方处理，或者模拟 axios 的 response.data
           resolve(res.data);
        } else if (res.statusCode === 401) {
          // 未授权，跳转登录
          uni.removeStorageSync('token');
          uni.navigateTo({ url: '/pages/login/login' });
          reject(new Error('Unauthorized'));
        } else {
          reject(new Error(res.data.message || '请求失败'));
        }
      },
      fail: (err) => {
        console.error('Request Fail:', err);
        reject(err);
      }
    });
  });
};

// 模拟 Axios 的便捷方法
export default {
  get(url, data) {
    return request({ url, method: 'GET', data });
  },
  post(url, data, options = {}) {
    return request({ url, method: 'POST', data, ...options });
  },
  put(url, data) {
    return request({ url, method: 'PUT', data });
  },
  delete(url, data) {
    return request({ url, method: 'DELETE', data });
  },
  // 文件上传特殊处理
  upload(url, filePath, name = 'file', formData = {}) {
      return new Promise((resolve, reject) => {
          const token = uni.getStorageSync('token');
          uni.uploadFile({
              url: baseURL + url,
              filePath: filePath,
              name: name,
              formData: formData,
              header: {
                  'Authorization': token ? `Bearer ${token}` : ''
              },
              success: (uploadFileRes) => {
                  try {
                      // uploadFile 返回的 data 是字符串，需要 parse
                      const data = JSON.parse(uploadFileRes.data);
                      resolve(data);
                  } catch (e) {
                      resolve(uploadFileRes.data);
                  }
              },
              fail: (err) => {
                  reject(err);
              }
          });
      });
  }
};
