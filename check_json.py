import requests
import json

BASE_URL = "http://localhost:8080/api"
USERNAME = "user"
PASSWORD = "123456"

def login():
    try:
        response = requests.post(f"{BASE_URL}/users/login", json={
            "username": USERNAME,
            "password": PASSWORD
        })
        response.raise_for_status()
        return response.json()["data"]["token"]
    except Exception as e:
        print(f"Login failed: {e}")
        return None

def check_messages():
    token = login()
    if not token: return

    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(f"{BASE_URL}/messages", headers=headers, params={"page": 1, "size": 1})
    print(json.dumps(response.json(), indent=2, ensure_ascii=False))

if __name__ == "__main__":
    check_messages()
