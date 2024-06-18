import openai

# Carregue sua chave de API a partir do arquivo de configuração
openai.api_key = 'sk-proj-IWxeCDbu1LMSNWWoVFJfT3BlbkFJaN2MBLvj1FZHEiH72ndC'



response = openai.Completion.create(
  engine="gpt-3.5-turbo-0613",
  prompt="Qual é a previsão do tempo para hoje?",
  max_tokens=50
)

print(response.choices[0].text.strip())

