import os
import shutil
import subprocess

# Pergunta se deseja arquivar ou recuperar um motorista
acao = input("Deseja arquivar (A) ou recuperar (R) um motorista? (A/R): ")

# Pergunta o número do motorista
numero_motorista = input("Digite o número do motorista: ")

# Caminho da pasta de destino
caminho_destino = f"/data/data/com.termux/files/home/99/{numero_motorista}"

# Salva o caminho completo da pasta de destino em $yy
yy = caminho_destino

# Verifica se a pasta de destino existe, caso não, cria
if not os.path.exists(caminho_destino):
    os.makedirs(caminho_destino)

if acao.upper() == 'A':
    # Caminho da pasta de origem (mmkv)
    caminho_origem = "/data/data/com.app99.driver/files/mmkv"

    # Move todo o conteúdo da pasta de origem para a pasta de destino
    conteudo_origem = os.listdir(caminho_origem)
    for item in conteudo_origem:
        caminho_item_origem = os.path.join(caminho_origem, item)
        caminho_item_destino = os.path.join(caminho_destino, item)
        if os.path.exists(caminho_item_destino):
            os.remove(caminho_item_destino)
        shutil.move(caminho_item_origem, caminho_item_destino)

    # Após mover os arquivos, altera as permissões para 777
    for root, dirs, files in os.walk(caminho_destino):
        for arquivo in files:
            caminho_arquivo = os.path.join(root, arquivo)
            os.chmod(caminho_arquivo, 0o777)

    # Força o fechamento do aplicativo com.app99.driver
    subprocess.run(["am", "force-stop", "com.app99.driver"])

    print(f"O motorista {numero_motorista} foi arquivado em {caminho_destino} e o aplicativo com.app99.driver foi fechado")

elif acao.upper() == 'R':
    # Caminho da pasta de origem (yy)
    caminho_origem = yy

    # Caminho da pasta de destino (mmkv)
    caminho_destino = "/data/data/com.app99.driver/files/mmkv"

    # Lista todos os arquivos e pastas na pasta de origem
    conteudo_origem = os.listdir(caminho_origem)

    # Copia todo o conteúdo da pasta de origem de volta para a pasta mmkv
    for item in conteudo_origem:
        caminho_item_origem = os.path.join(caminho_origem, item)
        caminho_item_destino = os.path.join(caminho_destino, item)
        if os.path.exists(caminho_item_destino):
            os.remove(caminho_item_destino)
        shutil.copy(caminho_item_origem, caminho_item_destino)

    # Após copiar os arquivos, altera as permissões para 777
    for root, dirs, files in os.walk(caminho_destino):
        for arquivo in files:
            caminho_arquivo = os.path.join(root, arquivo)
            os.chmod(caminho_arquivo, 0o777)

    # Força o fechamento do aplicativo com.app99.driver
    subprocess.run(["am", "force-stop", "com.app99.driver"])

    print(f"O motorista {numero_motorista} foi recuperado para {caminho_destino} e o aplicativo com.app99.driver foi fechado")

else:
    print("Opção inválida. Escolha 'A' para arquivar ou 'R' para recuperar um motorista.")

