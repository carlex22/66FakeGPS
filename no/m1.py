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
    # Lista das pastas a serem arquivadas e seus respectivos nomes
    pastas_arquivar = [
        ("/data/data/com.app99.driver/files/omega", "omega"),
        ("/data/data/com.app99.driver/files/mmkv", "mmkv")
    ]

    # Move todo o conteúdo das pastas para a pasta de destino com o nome das pastas originais
    for pasta_origem, nome_pasta in pastas_arquivar:
        caminho_pasta_destino = os.path.join(caminho_destino, nome_pasta)
        if not os.path.exists(caminho_pasta_destino):
            os.makedirs(caminho_pasta_destino)

        conteudo_origem = os.listdir(pasta_origem)
        for item in conteudo_origem:
            caminho_item_origem = os.path.join(pasta_origem, item)
            caminho_item_destino = os.path.join(caminho_pasta_destino, item)
            if os.path.isdir(caminho_item_destino):  # Verifica se é um diretório
                shutil.rmtree(caminho_item_destino)  # Remove o diretório recursivamente
            elif os.path.exists(caminho_item_destino):  # Verifica se o arquivo existe
                os.remove(caminho_item_destino)  # Remove o arquivo
            shutil.move(caminho_item_origem, caminho_item_destino)

    # Após mover os arquivos, altera as permissões para 777
    for root, dirs, files in os.walk(caminho_destino):
        for arquivo in files:
            caminho_arquivo = os.path.join(root, arquivo)
            os.chmod(caminho_arquivo, 0o777)

    # Força o fechamento do aplicativo com.app99.driver
    subprocess.run(["am", "force-stop", "com.app99.driver"])

    print(f"As pastas foram arquivadas em {caminho_destino} e o aplicativo com.app99.driver foi fechado")

elif acao.upper() == 'R':
    # Caminho da pasta de origem (yy)
    caminho_origem = yy

    # Lista das pastas a serem recuperadas e seus respectivos nomes
    pastas_recuperar = [
        ("/data/data/com.app99.driver/files/omega", "omega"),
        ("/data/data/com.app99.driver/files/mmkv", "mmkv")
    ]

    # Copia todo o conteúdo da pasta de origem de volta para as pastas originais
    for pasta_destino, nome_pasta in pastas_recuperar:
        caminho_pasta_origem = os.path.join(caminho_origem, nome_pasta)
        if not os.path.exists(caminho_pasta_origem):
            continue

        conteudo_origem = os.listdir(caminho_pasta_origem)
        for item in conteudo_origem:
            caminho_item_origem = os.path.join(caminho_pasta_origem, item)
            caminho_item_destino = os.path.join(pasta_destino, item)
            if os.path.isdir(caminho_item_destino):  # Verifica se é um diretório
                shutil.rmtree(caminho_item_destino)  # Remove o diretório recursivamente
            elif os.path.exists(caminho_item_destino):  # Verifica se o arquivo existe
                os.remove(caminho_item_destino)  # Remove o arquivo
            if os.path.isdir(caminho_item_origem):  # Verifica se é um diretório
                shutil.copytree(caminho_item_origem, caminho_item_destino)  # Copia o diretório
            else:
                shutil.copy(caminho_item_origem, caminho_item_destino)  # Copia o arquivo

    # Após copiar os arquivos de volta, altera as permissões para 777
    for pasta_destino, _ in pastas_recuperar:
        for root, dirs, files in os.walk(pasta_destino):
            for arquivo in files:
                caminho_arquivo = os.path.join(root, arquivo)
                os.chmod(caminho_arquivo, 0o777)

    # Força o fechamento do aplicativo com.app99.driver
    subprocess.run(["am", "force-stop", "com.app99.driver"])

    print(f"As pastas foram recuperadas para os locais originais e o aplicativo com.app99.driver foi fechado")

else:
    print("Opção inválida. Escolha 'A' para arquivar ou 'R' para recuperar as pastas.")

