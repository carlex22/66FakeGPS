import shutil
import json
import requests
from datetime import datetime
import os

def ticket(id):
    directory_path = f"/data/data/com.termux/files/home/99/{id}/omega/cache"

    if not os.path.exists(directory_path):
        print(f"Motorista {id} não encontrado.")
        return None
    else:
        files = [file for file in os.listdir(directory_path) if not file.endswith(".bak")]

        if len(files) > 0:
            file_path = os.path.join(directory_path, files[0])

            with open(file_path, "rb") as file:
                file_content = file.read()

                ticket_index_start = file_content.find(b'"ticket":"')

                if ticket_index_start != -1:
                    ticket_index_start += len(b'"ticket":"')
                    ticket_index_end = file_content.find(b'"', ticket_index_start)
                    if ticket_index_end != -1:
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

    url = "https://api.99taxis.mobi/halo/v1/multi/ability/d0f757be0913017417c20b9a1ad0d7a0"
    headers = {"Content-Type": "application/json"}

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

    response = requests.post(url, headers=headers, json=request_body)

    if response.status_code == 200:
        data = response.json().get('data', {}).get('abilities', {}).get('driver/dIndexInfo', {}).get('data', {}).get('driver_status_info', {})
        if data:
            #print (data)

            if data.get('conectar_status', '') == 1:
                return f"\033[92mOnline \033[0m"
            elif data.get('conectar_status', '') == 2:
                expire_time_timestamp = data.get('expire_time', '')
                expire_time_human = datetime.fromtimestamp(expire_time_timestamp).strftime('%d/%m')
                return f"Off=> {expire_time_human} "
            else:
                return None
        else:
            return None
    else:
        return None

def saldomotorista(ticket):
    if ticket is None:
        return None

    url = "https://api.99taxis.mobi/halo/v1/multi/ability/9996366648bbaf6f4859cda470784ac4"
    headers = {"Content-Type": "application/json"}

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

    response = requests.post(url, headers=headers, json=request_body)

    if response.status_code == 200:
        data = response.json().get('data', {}).get('abilities', {}).get('expo/dIncomeHome', {}).get('data', {}).get('driver_logic', {})
        if data:
            saldo = data.get('balance', {}).get('text', '')
            saldo_sem_moeda = saldo.replace('R$', '').strip()
            return saldo_sem_moeda
        else:
            return None
    else:
        return None

base_directory = "/data/data/com.termux/files/home/99/"
motoristas = []
relogin = []

if os.path.exists(base_directory):
    pastas = [folder for folder in os.listdir(base_directory) if os.path.isdir(os.path.join(base_directory, folder))]

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
                else:
                    data_fim_suspensao = datetime.now().strftime('%d/%m')  # Usa o horário atual

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

    motoristas_ordenados = sorted(motoristas, key=lambda x: (x["status"] != "\033[92mOnline \033[0m", -float(x.get("saldo", 0)), x.get("data_fim_suspensao", "")))

    exibir_resultado = input("\n\nExibir resultado da pesquisa? (S/N): ").upper()
    if exibir_resultado == "S":
        for motorista in motoristas_ordenados:
            print(f"Driver {motorista['id']} {motorista['status']}R$ {motorista['saldo']}")

    else:
        print("Não deseja exibir o resultado da pesquisa.")

    if relogin:
        corrigir_login = input(f"\nDeseja corrigir o login do motorista {relogin[0]}? (S/N): ").upper()
        if corrigir_login == "S":
            print("Corrigindo login...")
            shutil.copytree(f"/data/data/com.termux/files/home/99/{relogin[0]}/", "/data/data/com.app99.driver/files/", dirs_exist_ok=True)
            print("Dados transferidos, abra o app 99 e após sucesso no login retorne aqui para salvar os dados de login do motorista {relogin[0]}.\n Quando pronto retorne...\nContinuar?/ (s/n)")
        else:
            print("Não deseja corrigir o login.")
    else:
        print("Não há motoristas com login a corrigir.")

else:
    print("Diretório base não encontrado.")

