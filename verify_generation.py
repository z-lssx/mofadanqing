import os
import sys
import dashscope
from dashscope import MultiModalConversation
from http import HTTPStatus

def verify():
    print("=== DashScope Verification Script ===")
    
    # 1. Check API Key
    api_key = os.getenv("DASHSCOPE_API_KEY")
    if not api_key:
        print("❌ Error: DASHSCOPE_API_KEY is not set in environment variables.")
        return False
    print(f"✅ API Key found: {api_key[:6]}******")
    
    dashscope.api_key = api_key
    
    # 2. Check Package
    # print(f"✅ DashScope SDK Version: {dashscope.__version__}")
    print(f"✅ DashScope SDK imported successfully")
    
    # 3. Test Call
    print("⏳ Attempting to generate a test image...")
    try:
        messages = [{
            "role": "user",
            "content": [{"text": "A simple red circle"}]
        }]
        
        rsp = MultiModalConversation.call(
            model="qwen-image-plus",
            messages=messages,
            result_format='message',
            stream=False,
            size='1024*1024'
        )
        
        if rsp.status_code == HTTPStatus.OK:
            if rsp.output and rsp.output.choices:
                print(f"✅ Success! Image URL found in response")
                return True
            else:
                print("❌ Error: API returned OK but no results.")
                return False
        else:
            print(f"❌ API Request Failed. Code: {rsp.code}, Message: {rsp.message}")
            return False
            
    except Exception as e:
        print(f"❌ Exception during API call: {str(e)}")
        return False

if __name__ == "__main__":
    if verify():
        sys.exit(0)
    else:
        sys.exit(1)
