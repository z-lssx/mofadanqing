import requests

BASE = 'http://localhost:8080/api'

def login(username, password='123456'):
    r = requests.post(f'{BASE}/users/login', json={'username': username, 'password': password})
    r.raise_for_status()
    data = r.json()['data']
    return data['token']

def main():
    tok = login('admin')
    h = {'Authorization': f'Bearer {tok}'}
    params = {'page': 1, 'size': 50}
    r = requests.get(f'{BASE}/admin/logistics/shipment/list', headers=h, params=params)
    print('status:', r.status_code)
    print('body:', r.text)

if __name__ == '__main__':
    main()
