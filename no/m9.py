import os
import shutil

# Perguntando o nome da pasta de origem
nome_pasta_origem = input("Digite o nome da pasta de origem: ")

# Caminho da pasta de origem (onde o conteúdo será movido de volta)
caminho_origem = f"/data/data/com.termux/files/home/99/{nome_pasta_origem}"

# Caminho completo da pasta de destino
caminho_destino = f"/data/data/com.termux/files/home/99/{nome_pasta_origem}"

# Verifica se a pasta de destino existe, caso não, cria
if not os.path.exists(caminho_destino):
    os.makedirs(caminho_destino)

# Lista todos os arquivos e pastas na pasta de origem
conteudo_origem = os.listdir(caminho_origem)

# Move todo o conteúdo da pasta de origem de volta para a pasta de destino
for item in conteudo_origem:
    caminho_item_origem = os.path.join(caminho_origem, item)
    caminho_item_destino = os.path.join(caminho_destino, item)
    shutil.move(caminho_item_origem, caminho_item_destino)

print(f"Todo o conteúdo da pasta {nome_pasta_origem} foi movido de volta para {caminho_destino}")

