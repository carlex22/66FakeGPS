import json
import requests
from datetime import datetime
import os
import time

def ticket(id):
    # Alterar o caminho da pasta para incluir o ID do motorista
    directory_path = f"/data/data/com.termux/files/home/99/{id}/omega/cache"

    # Verificar se o diretório existe
    if not os.path.exists(directory_path):
        print(f"Motorista {id} não encontrado.")
        return None
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
                        return ticket
                    else:
                        print("Erro ao extrair o ticket")
                else:
                    print("Erro Campo 'ticket'")
        else:
            print("Não há dados do motorista")
    return None

def statusmotorista(ticket):
    if ticket is None:
        return "\033[91mERRO no Login\033[0m"

    # Definir a URL e os cabeçalhos para a função statusmotorista
    url = "https://api.99taxis.mobi/halo/v1/multi/ability/d0f757be0913017417c20b9a1ad0d7a0"
    headers = {
        "Content-Type": "application/json"
    }

    # Definir o corpo da solicitação com o valor do último 'ticket' encontrado
    request_body = {
        "abilities": {
            "driver/dIndexInfo": {
                "new_version": 1
            }
        },
        "common": {
            "app_version": "7.8.18",
            "terminal_id": 5,
            "ticket": ticket
        }
    }

    # Fazer o POST com os dados carregados
    response = requests.post(url, headers=headers, json=request_body)

    # Exibir a resposta
    if response.status_code == 200:
        print("\033[92mPost executado com sucesso.\033[0m")
    else:
        print("\033[91mERRO ao executar o Post.\033[0m")

    # Verificar o status da resposta
    if response.status_code == 200:
        # Exibir apenas as informações de "driver_status_info" da resposta do servidor
        data = response.json().get('data', {}).get('abilities', {}).get('driver/dIndexInfo', {}).get('data', {}).get('driver_status_info', {})
        if data:
            # Converter o timestamp de expiração para formato humano
            expire_time_timestamp = data.get('expire_time', '')
            expire_time_human = datetime.fromtimestamp(expire_time_timestamp).strftime('%d/%m %H:%M')

            # Verificar se o status é 2 (ou qualquer outro status que indique sucesso e saldo disponível)
            if data.get('status', '') == 2:
                return f"Motorista Online"
            elif data.get('status', '') == 1:
                return f"Motorista Suspenso até {expire_time_human}"
            else:
                return "Status desconhecido do motorista"
        else:
            return "Não foi possível encontrar as informações do motorista"
    else:
        return "\033[91mERRO no Login\033[0m"

def saldomotorista(ticket):
    # Definir a URL e os cabeçalhos para a função saldomotorista
    url = "https://api.99taxis.mobi/halo/v1/multi/ability/9996366648bbaf6f4859cda470784ac4"
    headers = {
        "Content-Type": "application/json"
    }

    # Definir o corpo da solicitação com o valor do último 'ticket' encontrado
    request_body = {
        "abilities": {
            "expo/dIncomeHome": {
                "need_refresh": True
            }
        },
        "common": {
            "app_version": "7.8.18",
            "terminal_id": 5,
            "ticket": ticket
        }
    }

    # Fazer o POST com os dados carregados
    response = requests.post(url, headers=headers, json=request_body)

    # Exibir a resposta
    if response.status_code == 200:
        print("\033[92mPost executado com sucesso.\033[0m")
    else:
        print("\033[91mERRO ao executar o Post.\033[0m")

    # Verificar o status da resposta
    if response.status_code == 200:
        # Exibir apenas as informações de "driver_logic" da resposta do servidor
        data = response.json().get('data', {}).get('abilities', {}).get('expo/dIncomeHome', {}).get('data', {}).get('driver_logic', {})
        if data:
            # Extrair o saldo do campo "balance"
            saldo = data.get('balance', {}).get('text', '')
            # Remover o prefixo de moeda ("R$")
            saldo_sem_moeda = saldo.replace('R$', '').strip()
            return saldo_sem_moeda
        else:
            return -1  # Indica que não foi possível encontrar as informações de saldo do motorista
    else:
        return -1  # Indica que ocorreu um erro ao obter o saldo do motorista

# Pesquisar o nome das pastas em /data/data/com.termux/files/home/99/
base_directory = "/data/data/com.termux/files/home/99/"

# Lista para armazenar as informações dos motoristas
motoristas = []

# Lista para armazenar os IDs dos motoristas com erro no login
relogin = []

# Verificar se o diretório base existe
if os.path.exists(base_directory):
    # Listar os diretórios dentro do diretório base
    pastas = [folder for folder in os.listdir(base_directory) if os.path.isdir(os.path.join(base_directory, folder))]

    # Adicionar as informações dos motoristas à lista motoristas
    for id_motorista in pastas:
        ticket_atual = ticket(id_motorista)
        if ticket_atual:
            online_suspenso = statusmotorista(ticket_atual)
            saldo = saldomotorista(ticket_atual)
            if saldo != -1 and "ERRO" not in online_suspenso:  # Verifica se não houve erro ao obter o saldo e o status
                data_fim_suspensao = None
                if "Suspenso" in online_suspenso:
                    data_fim_suspensao = online_suspenso.split(" ")[-1]

                motoristas.append({
                    "id": id_motorista,
                    "status": online_suspenso,
                    "saldo": saldo,
                    "data_fim_suspensao": data_fim_suspensao
                })
            else:
                relogin.append(id_motorista)

    # Classificar a lista de motoristas conforme especificado
    motoristas_ordenados = sorted(motoristas, key=lambda x: (x["status"] != "Motorista Online", -float(x.get("saldo", 0)), x.get("data_fim_suspensao", "")))
    
    # Exibir os motoristas ordenados
    for motorista in motoristas_ordenados:
        status_color = "\033[92m" if "Online" in motorista["status"] else "\033[91m"
        print(f"-------------------------------------------------")
        print(f"Motorista {motorista['id']} {motorista['status']} {status_color}{motorista['saldo']}\033[0m")
        if "Suspenso" in motorista["status"]:
            print(f"Disponível em {motorista['data_fim_suspensao']}")
    print("\n\n\nO motorista da vez é o {id_motorista}, deseja efetuar o login? (S/N)")
else:
    print("Diretório base não encontrado.")

