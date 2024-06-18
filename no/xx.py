import re

def telefone(id):
    caminho_arquivo = f"/data/data/com.termux/files/home/99/{id}/mmkv/login_pref"

    with open(caminho_arquivo, "rb") as arquivo:
        conteudo_binario = arquivo.read()
        conteudo_texto = conteudo_binario.decode('utf-8', errors='replace')  # Decodificar como texto
            
        telefone_encontrado = re.search(r'(?:\+?(55|1))?(\d{10,11})', conteudo_texto)
            
        if telefone_encontrado:
            telefone_formatado = telefone_encontrado.group(1) + telefone_encontrado.group(2)
        else:
            telefone_formatado = id + 'x' * (10 - len(id))

    return telefone_formatado

id = input("Digite o ID para buscar o número de telefone: ")
id = telefone(id)

print(id)

