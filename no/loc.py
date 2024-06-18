from plyer import gps

# Função para obter dados de localização usando Plyer
def obter_dados_localizacao():
    # Inicializa o serviço de localização
    gps.configure(on_location=on_location)

    # Inicia a obtenção da localização
    gps.start()

# Função de retorno de chamada para processar os dados de localização
def on_location(**kwargs):
    latitude = kwargs['lat']
    longitude = kwargs['lon']
    altitude = kwargs.get('altitude', 'N/A')
    speed = kwargs.get('speed', 'N/A')
    accuracy = kwargs.get('accuracy', 'N/A')

    # Exibe os dados de localização
    print("Latitude:", latitude)
    print("Longitude:", longitude)
    print("Altitude:", altitude)
    print("Velocidade:", speed)
    print("Precisão:", accuracy)

# Chama a função para obter dados de localização
obter_dados_localizacao()
