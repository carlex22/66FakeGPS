import json
import os

# Caminho para o diretório contendo o arquivo
directory_path = "/data/data/com.termux/files/home/99/24/omegacache"

# Listar os arquivos no diretório excluindo aqueles com extensão ".bak"
files = [file for file in os.listdir(directory_path) if not file.endswith(".bak")]

# Se houver pelo menos um arquivo após a exclusão
if len(files) > 0:
    # Obter o caminho completo do arquivo na lista
    file_path = os.path.join(directory_path, files[0])
    # Exibir o nome do arquivo encontrado
    print("Nome do arquivo encontrado:", files[0])

    # Exibir apenas o último valor de 'ticket'
    with open(file_path, "r") as file:
        file_content = file.readlines()

        # Inicializar a variável para armazenar o último valor de 'ticket'
        ultimo_ticket = None
        for line in file_content:
            try:
                # Tentar carregar a linha como um objeto JSON
                json_obj = json.loads(line)
                ultimo_ticket = json_obj.get('ex', {}).get('ticket', ultimo_ticket)
            except json.JSONDecodeError as e:
                print(f"Erro ao carregar linha como JSON: {e}")

        # Armazenar e exibir o valor do último 'ticket'
        if ultimo_ticket:
            with open('ultimo_ticket.txt', 'w') as output_file:
                output_file.write(ultimo_ticket)
                print("Valor do último ticket:", ultimo_ticket)
        else:
            print("Não foi possível encontrar o último ticket.")
else:
    print("Não há arquivos disponíveis para processar.")

