import requests

#apaga conteudp tela clear
import os
os.system('cls' if os.name == 'nt' else 'clear')


# input duvida: string com a pergunta   
# output resposta: string com a resposta
def gemini(duvida):
    # Formatando a requisição
    requisicao = {

        "modelType": "text_only",
        "prompt": f"Responde em portugues brasileito e de  forma que um humano entenda. O que esse arquivo faz e sua relacaco com os demais? \n\n conteudo:{duvida}  \n Responda em portugues brasileiro e de forma que um humano entenda",
    }

    # Enviando a requisição e capturando a resposta
    resposta = requests.post("http://localhost:3000/chat-with-gemini", json=requisicao)

    # Exibindo a resposta
    if resposta.status_code == 200:
        resposta_json = resposta.json()
        return resposta_json
    else:
        return None


#salva nome somente dos arquivos do diretorio
import os
arquivos = [arquivo for arquivo in os.listdir() if os.path.isfile(arquivo)]


#imprime arquivos do diretorio  con numwro a esquerda
for i, arquivo in enumerate(arquivos):
    print(f"{i} - {arquivo}")       

#perguntar ao usuario qual arquivo deseja abrir
arquivo = int(input("Digite o número do arquivo que deseja abrir: "))   


#imprime nome do arquivo escolhido
print(f"Você escolheu o arquivo: {arquivos[arquivo]}\nAgarde um momento...")  


#verifica se exite arqiuvos no diretorio
if len(arquivos) == 0:
    print("Nenhum arquivo encontrado")
    exit()



#gera gemini com contudo em texto do arquivo escolhido  
with open(arquivos[arquivo], "r") as f:
    conteudo = f.read()
    resposta = gemini(conteudo)
    #print 1 letra r por 0.01segundo
    import time

    print("\n\n\n")


    import subprocess

    # iInicie o processo em segundo plano
 #   subprocess.Popen(os.system("termux-tts-speak '" + resposta['result'] + "'"), stdout=subprocess.PIPE, stderr=subprocess.PIPE)


    import re

    texto = resposta['result']
    #remover caractetes especiais de texto
    re = re.sub(r'[^\w\s]', '', texto)


    # Opção mais simples
    subprocess.Popen(["termux-tts-speak", re])


# Continue a

#executa comando termux-tts-speak para falar resposta
    #os.system("termux-tts-speak '" + resposta['result'] + "'") 



    for i in resposta['result']:
        print(i, end='', flush=True)
        time.sleep(0.05)
        
    print("\n\n\n")
        





