import os
import json
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
        file_path = os.path.join(directory_path, files[0])

        # Exibir apenas o último valor de 'ticket' no arquivo binário
        with open(file_path, "rb") as file:
            file_content = file.read()

            # Encontrar a posição do texto '"ticket":"' no conteúdo do arquivo binário
            ticket_index_start = file_content.find(b'"ticket":"')

            if ticket_index_start != -1:
                # Avançar a posição para o início do valor do campo "ticket"
                ticket_index_start += len(b'"ticket":"')

                # Encontrar a posição do próximo caractere de aspas após o valor do campo "ticket"
                ticket_index_end = file_content.find(b'"', ticket_index_start)
                if ticket_index_end != -1:
                    # Extrair o valor do campo "ticket"
                    ticket = file_content[ticket_index_start:ticket_index_end].decode('utf-8')
                    

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
                    response = requests.post(url, headers=headers, json=request_body)

                    # Verificar o status da resposta
                    if response.status_code == 200:
                        # Exibir apenas as informações de "driver_status_info" da resposta do servidor
                        data = response.json().get('data', {}).get('abilities', {}).get('driver/dIndexInfo', {}).get('data', {}).get('driver_status_info', {})
                        if data:
                            print("Informações de Status do Motorista:{id}:")
                            
                            # Converter o timestamp de expiração para formato humano
                            expire_time_timestamp = data.get('expire_time', '')
                            expire_time_human = datetime.fromtimestamp(expire_time_timestamp).strftime('%Y-%m-%d %H:%M:%S')
                            

                            # Verificar o status
                            if data.get('status', '') == 1:
                                # Informar a resposta para o post com a mensagem "Motorista suspenso até," seguido do tempo de expiração humano
                                resposta_post = f"Motorista suspenso até, {expire_time_human}"
                                print(resposta_post)
                            elif data.get('status', '') == 2:
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
                    print("Fim das aspas não encontrado para o campo 'ticket'.")
            else:
                print("Campo 'ticket' não encontrado no arquivo.")
    else:
        print("Não há arquivos disponíveis para processar.")

