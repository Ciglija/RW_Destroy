import requests

url = "http://192.168.0.30:5000/load-database"
data = {}

try:
    response = requests.post(url, json=data)
    if response.status_code == 200:
        print("Uspešno učitane kutije.")
    else:
        print("Proverite da li je server pokrenut.")
except requests.exceptions.RequestException as e:
    print("Proverite da li je server pokrenut.")
