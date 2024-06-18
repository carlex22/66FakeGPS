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
            "prompt": f"Nao cite as perguntas e respostas anteriores, responda a ultima pergunta analizando o que foi dito   . Responde em portugues brasileito e de  forma que um humano entenda :{duvida}"
    }

    print(requisicao)

    # Enviando a requisição e capturando a resposta     
    resposta = requests.post("http://localhost:3000/chat-with-gemini", json=requisicao)

    # Exibindo a resposta
    if resposta.status_code == 200:
        resposta_json = resposta.json()
        return resposta_json
    else:
        return None



# Inicializa o contador e o histórico
contador = 0
historico = []

while True:
  # Obtém a entrada do usuário
  entrada = input("Pergunta: ")

  # Verifica se o usuário deseja sair
  if entrada.lower() == "sair":
    break

  # Incrementa o contador
  contador += 1

  # Atualiza o histórico
  #historico.append(("eu perguntei", entrada, "voce respendeu", contador))

  # Limita o histórico aos 3 últimos valores
  if len(historico) > 3:
    historico.pop(0)

  # Exibe a resposta com o número progressivo e o histórico
            
  if len(historico) > 0:
      resposta += f"\nAnteriormente: {historico}, Me responda agora: {entrada}"
  else:
      resposta = f"Perguntei {entrada}"

  res = gemini(resposta)
  historico.append(("perguntei", entrada, "voce respendeu", res))

  print(res)

