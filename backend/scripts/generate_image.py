import os
import sys
import argparse
import dashscope
from dashscope import MultiModalConversation
from http import HTTPStatus
import json

def generate_image(prompt, model="z-image-turbo", ref_img=None):
    # Ensure API Key is set
    api_key = os.getenv("DASHSCOPE_API_KEY")
    if not api_key:
        print("Error: DASHSCOPE_API_KEY not found in environment variables.", file=sys.stderr)
        sys.exit(1)
        
    dashscope.api_key = api_key

    # Construct messages
    user_content = [{"text": prompt}]
    if ref_img:
        # If ref_img is provided, add it to content. 
        # Note: Check if specific model supports image input. qwen-image-plus typically handles text-to-image.
        # But MultiModalConversation structure allows mixing.
        user_content.append({"image": ref_img})
        
    messages = [
        {
            "role": "user",
            "content": user_content
        }
    ]

    try:
        # Call MultiModalConversation as per user instruction
        response = MultiModalConversation.call(
            model=model,
            messages=messages,
            result_format='message',
            stream=False,
            watermark=False,
            prompt_extend=False,
            size='1120*1440'
        )

        if response.status_code == HTTPStatus.OK:
            # Parse output
            # Expected structure: output.choices[0].message.content[0]['image']
            if response.output and response.output.choices:
                choice = response.output.choices[0]
                if choice.message and choice.message.content:
                    # Look for image in content list
                    image_url = None
                    for item in choice.message.content:
                        if 'image' in item:
                            image_url = item['image']
                            break
                    
                    if image_url:
                        print(f"###URL_START###{image_url}###URL_END###")
                    else:
                        print("Error: No image URL found in response.", file=sys.stderr)
                        # Print full output to stderr for debugging
                        print(json.dumps(response.output, ensure_ascii=False, indent=2), file=sys.stderr)
                        sys.exit(1)
                else:
                    print("Error: Unexpected response structure (no message/content).", file=sys.stderr)
                    sys.exit(1)
            else:
                print("Error: No choices in output.", file=sys.stderr)
                sys.exit(1)
        else:
            print(f"Error: API Request Failed. Code: {response.code}, Message: {response.message}", file=sys.stderr)
            sys.exit(1)
            
    except Exception as e:
        print(f"Error: {str(e)}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='DashScope Image Generation')
    parser.add_argument('--prompt', type=str, required=True, help='Text prompt')
    parser.add_argument('--model', type=str, default='qwen-image-plus', help='Model name')
    parser.add_argument('--ref_img', type=str, help='Reference image URL')

    args = parser.parse_args()
    
    generate_image(args.prompt, args.model, args.ref_img)
