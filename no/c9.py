import os
import shutil

# Perguntando o nome da pasta de origem
nome_pasta_origem = input("Digite o nome da pasta de origem: ")

# Caminho da pasta de origem (onde o conteúdo será copiado de volta)
caminho_origem = f"/data/data/com.termux/files/home/99/{nome_pasta_origem}"

# Caminho completo da pasta de destino (pasta mmkv)
caminho_destino = "/data/data/com.app99.driver/files/mmkv"

# Verifica se a pasta de destino (mmkv) existe, caso não, cria
if not os.path.exists(caminho_destino):
    os.makedirs(caminho_destino)

# Lista todos os arquivos e pastas na pasta de origem
conteudo_origem = os.listdir(caminho_origem)

# Copia todo o conteúdo da pasta de origem de volta para a pasta mmkv
for item in conteudo_origem:
    caminho_item_origem = os.path.join(caminho_origem, item)
    caminho_item_destino = os.path.join(caminho_destino, item)
    shutil.copy(caminho_item_origem, caminho_item_destino)

print(f"Todo o conteúdo da pasta {nome_pasta_origem} foi copiado de volta para mmkv")

