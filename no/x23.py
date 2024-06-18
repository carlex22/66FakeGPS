import os                                         import re
import shutil
import json
import requests
from datetime import datetime


print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

print(" _|    _|                      _|\n _|    _|    _|_|_|    _|_|_|  _|  _|\n _|_|_|_|  _|    _|  _|        _|_|\n _|    _|  _|    _|  _|        _|  _|\n _|    _|    _|_|_|    _|_|_|  _|    _|");
                                         
                                         

print("  ___         ___   _   ___ _    _____  __\n | _ )_  _   / __| /_\ | _ \ |  | __\ \/ /\n | _ \ || | | (__ / _ \|   / |__| _| >  <\n |___/\_, |  \___/_/ \_\_|_\____|___/_/\_|\n      |__/");

print (" O senhor da guerra não gosta de crianças")

print ("    Powered by: Chat-gpt & 66 pipoca\n\n\n")

def salva():
    caminho_arquivo = "/data/data/com.app99.driver/files/mmkv/login_pref"
    try:
        with open(caminho_arquivo, "rb") as arquivo:
            conteudo_binario = arquivo.read()
            conteudo_texto = conteudo_binario.decode('utf-8', errors='replace') # Decodificar como texto
            telefone_encontrado = re.search(r'(?:\+?(55|1))?(\d{10,11})', conteudo_texto)
            if telefone_encontrado:
                idn = telefone_encontrado.group(1) + telefone_encontrado.group(2)
            else:
                return None;

    except FileNotFoundError:
        print(f"Arquivo login do app99 não encontrado.")
    except Exception as e:
        print(f"Ocorreu um erro ao ler o arquivo para login do app99 {e}")


    caminho_destino = f"/data/data/com.termux/files/home/99/{idn}"
    pastas_arquivar = [
        ("/data/data/com.app99.driver/files/omega", "omega"),             ("/data/data/com.app99.driver/files/mmkv", "mmkv")            ]                                   

    for pasta_origem, nome_pasta in pastas_arquivar:
        caminho_pasta_destino = os.path.join(caminho_destino, nome_pasta)
        if not os.path.exists(caminho_pasta_destino):
            os.makedirs(caminho_pasta_destino)

        conteudo_origem = os.listdir(pasta_origem)  
        for item in conteudo_origem:            
            caminho_item_origem = os.path.join(pasta_origem, item)
            caminho_item_destino = os.path.join(caminho_pasta_destino, item)                                                    
            if os.path.isdir(caminho_item_destino):
                shutil.rmtree(caminho_item_destino)
            elif os.path.exists(caminho_item_destino):
                os.remove(caminho_item_destino)                 

            shutil.move(caminho_item_origem, caminho_item_destino)

    for root, dirs, files in os.walk(caminho_destino):
        for arquivo in files:
            caminho_arquivo = os.path.join(root, arquivo)
            los.chmod(caminho_arquivo, 0o777)

    subprocess.run(["am", "force-stop", "com.app99.driver"])    
    print(f"Os arquivos de login do motorista {idn}, foram movidospara {caminho_destino}")

    return idn
#fim salva


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
#fim ticket

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
#fim status

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
#fim saldo

acao = input("Deseja salvar o motorista logado no app99?\nOu recupetar um login salvo? (S/R)")

if acao.upper() == 'S':
    id = salva()

if acao.upper() == 'R':
    bacao = input("Deseja visualizar dados salvos ou atualiza? (V/A)")

    if bacao.upper() == 'A':
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
            #else:
             #   relogin.append(id_motorista)
             #   print("Erro no Login...")

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
                print("Recuperando login...")
                shutil.copytree(f"/data/data/com.termux/files/home/99/{relogin[0]}/", "/data/data/com.app99.driver/files/", dirs_exist_ok=True)
                print("Dados Recuperados, force o fechamento e reabra o app99, após o login retorne para salvar novame nova o motorista {relogin[0]}.")
        
        if motoristas_ordenados:                    
            login = input(f"\n\nO motorista da vez é {motoristas_ordenados[0]['id']}, deseja efetuar carregar arquivos para login? (S/N)")
            if login == "S":
                caminho_origem = f"/data/data/com.termux/files/home/99/{motoristas_ordenados[0]['id']}"
                pastas_recuperar = [
                        ("/data/data/com.app99.driver/files/omega", "omega"),
                        ("/data/data/com.app99.driver/files/mmkv", "mmkv")
                ]

                for pasta_destino, nome_pasta in pastas_recuperar:
                    caminho_pasta_origem = os.path.join(caminho_origem, nome_pasta)
                    if not os.path.exists(caminho_pasta_origem):
                        continue

                    conteudo_origem = os.listdir(caminho_pasta_origem)
                    for item in conteudo_origem:
                        caminho_item_origem = os.path.join(caminho_pasta_origem, item)  
                        caminho_item_destino = os.path.join(pasta_destino, item)
                        if os.path.isdir(caminho_item_destino):
                            shutil.rmtree(caminho_item_destino)
                        elif os.path.exists(caminho_item_destino):
                            os.remove(caminho_item_destino)
                        if os.path.isdir(caminho_item_origem):
                            shutil.copytree(caminho_item_origem, caminho_item_destino)
                        else:
                            shutil.copy(caminho_item_origem, caminho_item_destino)
                
                for pasta_destino, _ in pastas_recuperar:
                    for root, dirs, files in os.walk(pasta_destino):
                        for arquivo in files:
                            caminho_arquivo = os.path.join(root, arquivo)
                            os.chmod(caminho_arquivo, 0o777)

                subprocess.run(["am", "force-stop", "com.app99.driver"])

                print("Login Recuperado, force o fechamento e reabra o app99.")


print("\n\nThauuu onbrigado!");



