import requests

# input duvida: string com a pergunta   
# output resposta: string com a resposta
def gemini(duvida):
    # Formatando a requisição
    requisicao = {
        "modelType": "text_only",
        "prompt": f"Responde em portugues... {duvida}"
    }

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
    entrada = input("Digite algo: ")
    # Verifica se o usuário deseja sair
    if entrada.lower() == "sair":
        break

    # Incrementa o contado
    contador += 1

    # Limita o histórico aos 3 últimos valores
    if len(historico) > 3:
        historico.pop(0)


    # Exibe a resposta com o número progressivo e o histórico

    if len(historico) > 0:
        resposta += f"\nAnteriormente: {historico}, Me responda agora: {entrada}"
    else:
        resposta = gemini(entrada)

    historico.append(("eu perguntei", entrada, "voce respendeu" , resposta))

    print(historico)
