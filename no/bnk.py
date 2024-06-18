import requests


url = 'https://pay.99taxis.mobi/web_wallet/international/external/withdraw/component/v1/channel/updateCardInfo'


urlinseri = 'https://pay.99taxis.mobi/web_wallet/international/external/withdraw/component/v1/channel/createandbind'

headers = {
    'Content-Type': 'application/x-www-form-urlencoded',
    'Accept': 'application/json, text/plain, */*'
}

# Solicita ao usuário o valor para a variável token
token_value = input("Insira o valor para a variável token: ")

data = {
    'token': token_value,
    'params': '{"appID":"","bankCode":"DLOCAL","cardType":7,"actionType":1,"extraMap":{"stripe":{"external_account":{"account_number":"597017070","routing_number":"0001"},"citi_para":{"holder_middle_name":null,"holder_phone_area_code":"","agency_digit":"","account_digit":"","holder_address_colony":null,"holder_address_interior_number":null,"holder_phone":"","holder_identity_id":"03656247994","holder_name":"","holder_address":"","holder_last_name":"Santos","email":"","bank_code":"260","card_type":"1","bank_name":"NUBANK","bank_bic":"","holder_identity_type":"","holder_address_street_number":"","holder_address_street_name":"","holder_first_name":"Carlos Rodrigues dos","holder_post_code":"","holder_province_code":"","holder_city_name":"","holder_birthday":{"year":"","month":"","day":""}}},"other_data":{},"legal_entity":{"dob":{"year":"","month":"","day":""},"first_name":"","last_name":"","personal_id_number":"","verification":{"document":"","document_back":""},"address":{"city":"","state":"","line1":"","postal_code":""}},"driver_img":{"driver_license":"","driver_license_back":""},"citu_para":{"card_type":""}},"lang":"pt-BR","biz_type":"2","utc_offset":"-180","origin_id":"","maptype":"wgs84","lat":"-25.4820872","lng":"-49.2904578","location_cityid":"55000515"}'
}

response = requests.post(url, headers=headers, data=data)

print(response.text)

