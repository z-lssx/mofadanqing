import requests
import json

BASE_URL = "http://localhost:8080/api"

def login():
    # 尝试 users login (根据 UserController)
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
            print("Login successful")
            return data["data"]["token"]
        else:
            print(f"Login failed: {data}")
            return None
    except Exception as e:
        print(f"Login error: {e}")
        return None

def get_product(token):
    url = f"{BASE_URL}/products?page=1&size=10"
    headers = {"Authorization": f"Bearer {token}"}
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        data = response.json()
        if data.get("code") == 200:
            records = data["data"]["records"]
            if records:
                print(f"Found product: {records[0]['id']} - {records[0]['name']}")
                return records[0]
            else:
                print("No products found")
                return None
        else:
            print(f"Get products failed: {data}")
            return None
    except Exception as e:
        print(f"Get product error: {e}")
        return None

def create_order(token, product_id):
    url = f"{BASE_URL}/orders"
    headers = {"Authorization": f"Bearer {token}"}
    payload = {
        "items": [
            {
                "productId": product_id,
                "quantity": 1
            }
        ],
        "shippingAddress": "Test Address",
        "paymentMethod": "WECHAT"
    }
    
    print(f"Creating order with payload: {json.dumps(payload)}")
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        print(f"Response status: {response.status_code}")
        print(f"Response body: {response.text}")
    except Exception as e:
        print(f"Create order error: {e}")

if __name__ == "__main__":
    token = login()
    if token:
        product = get_product(token)
        if product:
            create_order(token, product["id"])
