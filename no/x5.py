import json
import os
import requests
from datetime import datetime

# Caminho para o diretório contendo o arquivo
directory_path = "/data/data/com.termux/files/home/99/24/omegacache"

# Listar os arquivos no diretório excluindo aqueles com extensão ".bak"
files = [file for file in os.listdir(directory_path) if not file.endswith(".bak")]

# Se houver pelo menos um arquivo após a exclusão
if len(files) > 0:
    # Obter o caminho completo do arquivo na lista
    file_path = os.path.join(directory_path, files[0])

    # Exibir apenas o último valor de 'ticket'
    with open(file_path, "r") as file:
        file_content = file.readlines()

        # Inicializar a variável para armazenar o último valor de 'ticket'
        ultimo_ticket = None
        for line in file_content:
            try:
                # Tentar carregar a linha como um objeto JSON
                json_obj = json.loads(line)
                ultimo_ticket = json_obj.get('ex', {}).get('ticket', ultimo_ticket)
            except json.JSONDecodeError as e:
                print(f"Erro ao carregar linha como JSON: {e}")

        # Verificar se foi encontrado um valor para 'ticket'
        if ultimo_ticket:
            # Definir o texto do corpo da solicitação com o valor do último 'ticket' encontrado
            request_body_text = f'''
            {{
              "abilities": {{
                "driver/dIndexInfo": {{
                  "new_version": 1
                }}
              }},
              "common": {{
                "app_version": "7.8.18",
                "terminal_id": 5,
                "ticket": "{ultimo_ticket}"
              }}
            }}'''

            # Carregar dados do arquivo request.json
            with open('request.json') as f:
                request_data = json.load(f)

            # Extrair informações necessárias
            url = request_data['url']
            headers = request_data['headers']

            # Remover o cabeçalho "Transfer-Encoding: chunked" se estiver presente
            headers.pop('Transfer-Encoding', None)

            # Converter o texto do corpo da solicitação em formato JSON
            request_body = json.loads(request_body_text)

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
        else:
            print("Não foi possível encontrar o último ticket.")
else:
    print("Não há arquivos disponíveis para processar.")

