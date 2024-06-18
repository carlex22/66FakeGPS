import json
import requests

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
    print("POST bem-sucedido!")

    # Exibir a resposta do servidor formatada em JSON
    print("Resposta do servidor formatada:")
    formatted_response = json.dumps(response.json(), indent=4)
    print(formatted_response)

else:
    print("Erro ao fazer o POST:", response.status_code)
    print("Resposta:")
    print(response.text)

