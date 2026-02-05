import requests

BASE = 'http://localhost:8080/api'

def login(username, password='123456'):
    r = requests.post(f'{BASE}/users/login', json={'username': username, 'password': password})
    r.raise_for_status()
    data = r.json()['data']
    return data['token']

def main():
    tok = login('luo')
    h = {'Authorization': f'Bearer {tok}'}
    r = requests.get(f'{BASE}/orders', headers=h)
    print('orders status:', r.status_code)
    data = r.json()['data']
    for o in data.get('records', []):
        print('order:', o['id'], o['status'], o.get('c2mStatus'))
        # fetch items
        d = requests.get(f'{BASE}/orders/{o["id"]}', headers=h).json()['data']
        for it in d.get('items', []):
            print('  item:', it['id'], it.get('currentStatus'), it.get('c2mStatus'))

if __name__ == '__main__':
    main()
