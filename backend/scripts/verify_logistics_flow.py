import requests
import sys

BASE = 'http://localhost:8080/api'

def login(username, password='123456'):
    r = requests.post(f'{BASE}/users/login', json={'username': username, 'password': password})
    r.raise_for_status()
    data = r.json()
    token = data['data']['token']
    user = data['data']['user']
    return token, user

def get(url, token, params=None):
    h = {'Authorization': f'Bearer {token}'}
    r = requests.get(f'{BASE}{url}', headers=h, params=params or {})
    r.raise_for_status()
    return r.json()

def post(url, token, json=None):
    h = {'Authorization': f'Bearer {token}'}
    r = requests.post(f'{BASE}{url}', headers=h, json=json or {})
    r.raise_for_status()
    return r.json()

def main():
    admin_tok, admin_user = login('admin')
    luo_tok, luo_user = login('luo')

    products = requests.get(f'{BASE}/products', params={'page':1,'size':1}).json()
    prod_page = products.get('data', {})
    prod = (prod_page.get('records') or prod_page.get('content') or [])[0]
    payload = {
        'items': [{'productId': prod['id'], 'quantity': 1}],
        'shippingAddress': '北京市朝阳区',
        'paymentMethod': 'WECHAT'
    }
    order_res = requests.post(f'{BASE}/orders', headers={'Authorization': f'Bearer {luo_tok}'}, json=payload).json()
    order_id = order_res['data']['id']

    items = get(f'/orders/{order_id}', luo_tok)['data']['items']
    item_id = items[0]['id']
    print('new order itemId:', item_id)

    h = {'Authorization': f'Bearer {luo_tok}'}
    r = requests.put(f'{BASE}/orders/items/{item_id}/c2m-status', headers=h, json={'status': 'confirmed', 'remark': '用户提交定制'})
    print('confirm status:', r.status_code, r.text)
    if r.status_code != 200:
        raise Exception('确认定制失败')
    detail = get(f'/orders/{order_id}', luo_tok)['data']
    print('currentStatus after confirm:', detail['items'][0].get('currentStatus'), detail['items'][0].get('c2mStatus'))

    pack_list = get('/admin/logistics/pack/list', admin_tok)['data']
    print('pack list total:', pack_list['total'])
    print('pack records:', [r['orderItemId'] for r in pack_list['records']])
    assert any(str(r['orderItemId']) == str(item_id) for r in pack_list['records']), '采发包列表未找到该订单项'

    pack_record_candidates = [r for r in pack_list['records'] if str(r['orderItemId']) == str(item_id)]
    if not pack_record_candidates:
        raise Exception('采发包列表未找到该订单项(定位失败)')
    pack_record = pack_record_candidates[0]
    print('confirm-issue packId:', pack_record['id'])
    post(f'/admin/logistics/pack/{pack_record["id"]}/confirm-issue', admin_tok, {'remark': '确认下发'})

    ws_list = get('/admin/logistics/workshop/list', admin_tok)['data']
    print('workshop list itemIds:', [r['orderItemId'] for r in ws_list['records']])
    assert any(str(r['orderItemId']) == str(item_id) for r in ws_list['records']), '工坊签收列表未找到该订单项'
    ws_record = [r for r in ws_list['records'] if str(r['orderItemId']) == str(item_id)][0]
    post(f'/admin/logistics/workshop/{ws_record["id"]}/confirm-receive', admin_tok, {'remark': '签收'})

    pr_list = get('/admin/logistics/production/list', admin_tok)['data']
    print('production list itemIds:', [r['orderItemId'] for r in pr_list['records']])
    assert any(str(r['orderItemId']) == str(item_id) for r in pr_list['records']), '绣制列表未找到该订单项'
    pr_record = [r for r in pr_list['records'] if str(r['orderItemId']) == str(item_id)][0]
    post(f'/admin/logistics/production/{pr_record["id"]}/finish-production', admin_tok, {'remark': '绣制完成'})

    sp_list = get('/admin/logistics/shipment/list', admin_tok)['data']
    print('shipment list itemIds:', [r['orderItemId'] for r in sp_list['records']])
    assert any(str(r['orderItemId']) == str(item_id) for r in sp_list['records']), '成品发货列表未找到该订单项'

    print('物流流转验证成功：采发包→工坊签收→绣制中→成品发货')

if __name__ == '__main__':
    try:
        main()
    except Exception as e:
        print('验证失败:', e)
        sys.exit(1)
