import json
import requests
from datetime import datetime

# Carregar dados do arquivo request.json
with open('request.json') as f:
    request_data = json.load(f)

# Extrair informações necessárias
url = request_data['url']
headers = request_data['headers']

# Remover o cabeçalho "Transfer-Encoding: chunked" se estiver presente
headers.pop('Transfer-Encoding', None)

# Carregar o conteúdo do arquivo request_body.json
with open('request_body.json') as f:
    request_body = json.load(f)

# Fazer o POST com os dados carregados e habilitar verificação de certificado
response = requests.post(url, headers=headers, json=request_body, verify=False)

# Verificar o status da resposta
if response.status_code == 200:
    # Exibir apenas as informações de "driver_status_info" da resposta do servidor
    data = response.json().get('data', {}).get('abilities', {}).get('driver/dIndexInfo', {}).get('data', {}).get('driver_status_info', {})
    if data:
        print("Informações de Status do Motorista:")
        print(f"Title: {data.get('title', '')}")
        print(f"Context: {data.get('context', '')}")
        print(f"Status: {data.get('status', '')}")
        print(f"Reason: {data.get('reason', '')}")
        
        # Converter o timestamp de expiração para formato humano
        expire_time_timestamp = data.get('expire_time', '')
        expire_time_human = datetime.fromtimestamp(expire_time_timestamp).strftime('%Y-%m-%d %H:%M:%S')
        print(f"Expire Time (Human): {expire_time_human}")
    else:
        print("Não foi possível encontrar as informações de 'driver_status_info' na resposta.")

else:
    print("Erro ao fazer o POST:", response.status_code)
    print("Resposta:")
    print(response.text)

