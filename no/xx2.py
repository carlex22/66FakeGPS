import os
import re


def telefone(id):
    caminho_arquivo = f"/data/data/com.termux/files/home/99/{id}/mmkv/login_pref"
    
    try:
        with open(caminho_arquivo, "rb") as arquivo:
            conteudo_binario = arquivo.read()
            conteudo_texto = conteudo_binario.decode('utf-8', errors='replace')  # Decodificar como texto
            
            # Usar expressão regular para encontrar número de telefone
            telefone_encontrado = re.search(r'(?:\+?(55|1))?(\d{10,11})', conteudo_texto)
            
            if telefone_encontrado:
                telefone_formatado = telefone_encontrado.group(1) + telefone_encontrado.group(2)
                idn = telefone_formatado
            else:
                idn = id + 'x' * (10 - len(id))
    
    except FileNotFoundError:
        print(f"Arquivo para ID {id} não encontrado.")
    except Exception as e:
        print(f"Ocorreu um erro ao ler o arquivo para ID {id}: {e}")
    


    # Movendo os arquivos, se necessário
    caminho_origem = f"/data/data/com.termux/files/home/99/{id}"

    caminho_destino = f"/data/data/com.termux/files/home/99/{idn}"
    
    print(id)
    print(idn)


    try:
        os.rename(caminho_origem, caminho_destino)
        print(f"Conteúdo movido de {caminho_origem} para {caminho_destino}")
    except FileNotFoundError:
        print(f"Erro: pasta {caminho_origem} não encontrada.")
    except FileExistsError:
        print(f"Erro: pasta {caminho_destino} já existe.")
    except Exception as e:
        print(f"Ocorreu um erro ao mover o conteúdo: {e}")


id = input("Digite o ID para buscar o número de telefone: ")                                                                                                        
telefone(id)


