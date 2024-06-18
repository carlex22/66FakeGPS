import json
import requests
from datetime import datetime
import os

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
        return None

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

    # Verificar o status da resposta
    if response.status_code == 200:
        # Exibir apenas as informações de "driver_status_info" da resposta do servidor
        data = response.json().get('data', {}).get('abilities', {}).get('driver/dIndexInfo', {}).get('data', {}).get('driver_status_info', {})
        if data:
            # Converter o timestamp de expiração para formato humano
            expire_time_timestamp = data.get('expire_time', '')
            expire_time_human = datetime.fromtimestamp(expire_time_timestamp).strftime('%d/%m')

            # Verificar se o status é 2 (ou qualquer outro status que indique sucesso e saldo disponível)
            if data.get('status', '') == 2:
                return f"\033[92m Online \033[0m"
            elif data.get('status', '') == 1:
                return f"\033[91m Off até {expire_time_human} \033[0m"
            else:
                return None
        else:
            return None
    else:
        return None

def saldomotorista(ticket):
    if ticket is None:
        return None

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
            return None
    else:
        return None

# Pesquisar o nome das pastas em /data/data/com.termux/files/home/99/
base_directory = "/data/data/com.termux/files/home/99/"

# Lista para armazenar as informações dos motoristas
motoristas = []

# Lista para armazenar os motoristas com erro de login
relogin = []

# Verificar se o diretório base existe
if os.path.exists(base_directory):
    # Listar os diretórios dentro do diretório base
    pastas = [folder for folder in os.listdir(base_directory) if os.path.isdir(os.path.join(base_directory, folder))]

    # Adicionar as informações dos motoristas à lista motoristas
    for id_motorista in pastas:
        print(f"Buscando motorista {id_motorista}... ", end="")
        ticket_atual = ticket(id_motorista)
        if ticket_atual:
            status = statusmotorista(ticket_atual)
            saldo = saldomotorista(ticket_atual)
            data_fim_suspensao = None
            if status is not None:
                if "off" in status:
                    data_fim_suspensao = status.split(" ")[-1]

            # Verificar se houve erro na obtenção de status ou saldo
            if status is not None and saldo is not None:
                motoristas.append({
                    "id": id_motorista,
                    "status": status,
                    "saldo": saldo,
                    "data_fim_suspensao": data_fim_suspensao
                })
                print("Ok...")
            else:
                relogin.append(id_motorista)
                print("Erro no Login...")
        else:
            relogin.append(id_motorista)
            print("Erro no Login...")

    # Classificar a lista de motoristas conforme especificado
    motoristas_ordenados = sorted(motoristas, key=lambda x: (x["status"] != "\033[92m Online \033[0m", -float(x.get("saldo", 0)), x.get("data_fim_suspensao", "")))

    # Exibir os motoristas ordenados se o usuário desejar
    exibir_resultado = input("\n\nExibir resultado da pesquisa? (S/N): ").upper()
    if exibir_resultado == "S":
        for motorista in motoristas_ordenados:
            print(f"Driver {motorista['id']},{motorista['status']}R$ {motorista['saldo']}")

    # Exibir mensagem para efetuar login do primeiro motorista da lista
    if motoristas_ordenados:
        print(f"\n\nO motorista da vez é o {motoristas_ordenados[0]['id']}, deseja efetuar o login? (S/N)")
    else:
        print("\n\nNão há motoristas para efetuar login.")
else:
    print("Diretório base não encontrado.")

