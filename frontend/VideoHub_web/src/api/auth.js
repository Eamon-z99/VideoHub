import request from '@/utils/request'

/**
 * 用户登录
 * @param {string} account - 账号
 * @param {string} password - 密码
 * @returns {Promise} 返回登录响应，包含 token、userId、username、loginAccount
 */
export const loginApi = (account, password) => {
  return request.post('/api/auth/login', {
    account: account.trim(),
    password: password
  })
}

/**
 * 用户注册（如果后端实现了注册接口）
 * @param {Object} data - 注册数据
 * @returns {Promise}
 */
export const registerApi = (data) => {
  return request.post('/api/auth/register', data)
}

/**
 * 用户退出登录
 * @returns {Promise} 返回退出登录响应
 */
export const logoutApi = () => {
  return request.post('/api/auth/logout')
}

