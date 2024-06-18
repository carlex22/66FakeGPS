import os
import json
import requests
from datetime import datetime

id = input("Digite o número do motorista: ")
directory_path = f"/data/data/com.termux/files/home/99/{id}/omega/cache"

if not os.path.exists(directory_path):
    print("Motorista não encontrado.")
else:
    files = [file for file in os.listdir(directory_path) if not file.endswith(".bak")]

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
                    print(f"Valor do campo 'ticket': {ticket}")
                else:
                    print("Fim das aspas não encontrado para o campo 'ticket'.")
            else:
                print("Campo 'ticket' não encontrado no arquivo.")
    else:
        print("Não há arquivos disponíveis para processar.")

