import requests
from tabulate import tabulate

url = "http://192.168.0.30:5000/get-missing-boxes"

try:
    response = requests.get(url)
    if response.status_code == 200:
        data = response.json()
        boxes = data.get("missing_boxes", [])

        if boxes:
            headers = ["Klijent", "Kutija", "Tura"]
            rows = [
                [box.get("client", ""), box.get("box", ""), box.get("batch", "")]
                for box in boxes
            ]

            table = tabulate(rows, headers=headers, tablefmt="grid")
            print(table)
        else:
            print("Sve kutije su skenirane.")
    else:
        print("Proverite da li je server pokrenut.")

except requests.exceptions.RequestException as e:
    print("Proverite da li je server pokrenut.")
