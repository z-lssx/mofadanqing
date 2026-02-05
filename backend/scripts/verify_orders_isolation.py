import requests
import sys

BASE = 'http://localhost:8080/api'

def login(username, password='123456'):
    # 优先使用 /users/login，兼容 /auth/login
    r = requests.post(f'{BASE}/users/login', json={'username': username, 'password': password})
    r.raise_for_status()
    data = r.json()
    # 兼容两种返回结构
    if 'token' in data and 'user' in data:
        token = data['token']
        user = data['user']
    else:
        token = data['data']['token']
        user = data['data']['user']
    return token, user

def get_orders(token):
    h = {'Authorization': f'Bearer {token}'}
    r = requests.get(f'{BASE}/orders', headers=h)
    r.raise_for_status()
    return r.json()['data']

def get_admin_orders(token):
    h = {'Authorization': f'Bearer {token}'}
    r = requests.get(f'{BASE}/orders/admin', headers=h)
    return r.status_code, (r.json().get('data') if r.headers.get('Content-Type','').startswith('application/json') else None)

def main():
    admin_tok, admin_user = login('admin')
    luo_tok, luo_user = login('luo')

    admin_orders = get_orders(admin_tok)
    luo_orders = get_orders(luo_tok)

    print('admin /orders count:', admin_orders['total'])
    print('luo   /orders count:', luo_orders['total'])
    print('admin user id:', admin_user['id'])
    print('luo   user id:', luo_user['id'])
    print('luo   order userIds:', [o.get('userId') for o in luo_orders['records']])

    # /orders 仅返回当前用户订单
    assert all(str(o['userId']) == str(admin_user['id']) for o in admin_orders['records']), 'admin /orders 泄露他人订单'
    assert all(str(o['userId']) == str(luo_user['id']) for o in luo_orders['records']), 'luo /orders 泄露他人订单'

    # 管理端接口权限
    code_admin, admin_all = get_admin_orders(admin_tok)
    code_luo, _ = get_admin_orders(luo_tok)
    print('admin /orders/admin status:', code_admin)
    print('luo   /orders/admin status:', code_luo)
    assert code_admin == 200 and admin_all is not None, 'admin 无法访问全量订单'
    assert code_luo == 403, 'luo 不应访问全量订单'

    print('验证通过：用户订单数据已按用户隔离；管理端接口权限正确')

if __name__ == '__main__':
    try:
        main()
    except Exception as e:
        print('验证失败:', e)
        sys.exit(1)
