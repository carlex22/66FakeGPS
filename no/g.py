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
#perguntar ao usuario duvida
duvida = input("Digite sua dúvida: ")   

#gera resposta geminu e imprime formatado   
resposta = gemini(duvida)   

#imprime resposta valor result json formatado    
print(resposta['result'])

