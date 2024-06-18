import requests

url = 'https://pay.99taxis.mobi/web_wallet/international/external/withdraw/component/v1/channel/createandbind'
headers = {
    'Content-Type': 'application/x-www-form-urlencoded',
    'Accept': 'application/json, text/plain, */*'
}

# Pergunta ao usuário o valor para a variável data
data_value = input("Insira o valor para a variável data: ")

response = requests.post(url, headers=headers, data=data_value)

print(response.text)

