import requests

url = "http://192.168.0.30:5000/load-database"
data = {}

try:
    response = requests.post(url, json=data)
    print(f"Status Code: {response.status_code}")
    print(f"Response: {response.text}")
except requests.exceptions.RequestException as e:
    print(f"Error sending POST request: {e}")
