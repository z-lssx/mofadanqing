import requests
import json

BASE_URL = "http://localhost:8080/api"

def login():
    url = f"{BASE_URL}/users/login"
    payload = {
        "username": "luo",
        "password": "123456"
    }
    try:
        response = requests.post(url, json=payload)
        response.raise_for_status()
        data = response.json()
        if data.get("code") == 200:
            return data["data"]["token"]
        return None
    except Exception as e:
        print(f"Login error: {e}")
        return None

def create_order(token):
    url = f"{BASE_URL}/orders"
    headers = {"Authorization": f"Bearer {token}"}
    # 假设商品 ID 9 存在 (之前修复过)
    payload = {
        "items": [{"productId": 9, "quantity": 1}],
        "shippingAddress": "Trace Test Address",
        "paymentMethod": "WECHAT"
    }
    try:
        response = requests.post(url, json=payload, headers=headers)
        data = response.json()
        if data.get("code") == 200:
            order_id = data["data"]["id"]
            print(f"Order created: {order_id}")
            return order_id
        else:
            print(f"Create order failed: {data}")
            return None
    except Exception as e:
        print(f"Create order error: {e}")
        return None

def test_trace(token, order_id):
    url = f"{BASE_URL}/orders/{order_id}"
    headers = {"Authorization": f"Bearer {token}"}
    try:
        response = requests.get(url, headers=headers)
        data = response.json()
        if data.get("code") == 200:
            print("Trace data retrieved successfully")
            order = data["data"]["order"]
            items = data["data"]["items"]
            
            print(f"Order Status: {order['status']}")
            if items:
                print(f"Timeline items count: {len(items[0].get('statusTimeline', []))}")
                for timeline in items[0].get('statusTimeline', []):
                    print(f"  - {timeline['time']} : {timeline['status']}")
            return True
        else:
            print(f"Get trace failed: {data}")
            return False
    except Exception as e:
        print(f"Trace error: {e}")
        return False

if __name__ == "__main__":
    token = login()
    if token:
        order_id = create_order(token)
        if order_id:
            test_trace(token, order_id)
