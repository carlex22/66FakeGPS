import json
import os
import requests
from datetime import datetime

# Solicitar o número do motorista e armazená-lo na variável $id
id = input("Digite o número do motorista: ")

# Alterar o caminho da pasta para incluir o ID do motorista
directory_path = f"/data/data/com.termux/files/home/99/{id}/omega/cache"

# Verificar se o diretório existe
if not os.path.exists(directory_path):
    print("Motorista não encontrado.")
else:
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
            ticket = None
            for line in file_content:
                try:
                    # Tentar carregar a linha como um objeto JSON
                    json_obj = json.loads(line)
                    ticket = json_obj.get('ex', {}).get('ticket', ticket)
                except json.JSONDecodeError as e:
                    print(f"Erro ao carregar linha como JSON: {e}")

            # Verificar se foi encontrado um valor para 'ticket'
            if ticket:
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
                    "ticket": "{ticket}"
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

                        # Verificar se o status é 2 (ou qualquer outro status que indique sucesso e saldo disponível)
                        if data.get('status', '') == 2:
                            # Extrair o saldo do campo "title"
                            title_text = data.get('title', '')
                            saldo_start_index = title_text.find("Seu saldo é de")
                            if saldo_start_index != -1:
                                saldo_start_index += len("Seu saldo é de")
                                saldo_text = title_text[saldo_start_index:].strip()
                                # Extrair apenas o valor monetário do texto
                                saldo = saldo_text.split()[0]
                                print(f"Saldo: {saldo}")

                                # Informar a resposta para o post com a mensagem "Motorista Online, Saldo" seguido do valor do saldo
                                resposta_post = f"Motorista Online, Saldo {saldo}"
                                print(resposta_post)
                            else:
                                print("Texto 'Seu saldo é de' não encontrado em 'title'.")
                        else:
                            print("O status não indica saldo disponível.")
                            # Exibir a resposta do arquivo original
                            with open('resposta_original.txt', 'r') as original_file:
                                resposta_original = original_file.read()
                                print(resposta_original)
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

