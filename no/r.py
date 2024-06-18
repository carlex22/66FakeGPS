import json

url = "https://api.99taxis.mobi/halo/v1/multi/ability/d0f757be0913017417c20b9a1ad0d7a0"


headers= {
        "secdd-authentication": "e3b741eeafe98ef67cee67e1eade44505480aa8efda2494f94cbb76e6d6df94f25a7aee4d4b6eea0090fee2021b3003d01284ff68e01000001000000",

        "didi-header-rid": "2911cf7d66226217000006324698339c",

        "TripCountry": "BR",

        "secdd-challenge": "1,com.app99.driver|1.0.27||||0||",

        "User-Agent": "Android/11 didihttp OneNet/2.1.1.18.1 com.app99.driver/7.8.18",

        "CityId": "55000199",

        "Content-Type": "application/json",


        "Host": "api.99taxis.mobi",

        "Connection": "Keep-Alive",

        "Accept-Encoding": "gzip",

        "wsgsig": "dd04-GG1iIlWwrwtDSmRAI4ZmtcXAlw1WJiYjNjF5U/NLfNBcG7KTYaqemI7oO6saUPKimuuNX1tHkxQ+QRNypB8bW4YHWNNbUSkLrhv713yYsGRJYQO9OiFUFsJ6cgJAfR/Iyvt0RSzEnTW7XSRmkToHXSE4Oh34C5S9pTv7EOq3sTWaX5w8pAH"
  }


data = {
    "abilities": {
        "driver/dGeoReverse": {"lng": -46.67567502177737, "lat": -23.58897906655345},
        "expo/driverDispatch": {"component_id": "driver_dispatch_tool"},
        "driver/index-settings-v2": {"is_self_built_map": 0},
        "driver/newIncomeCard": {"component_id": "driver_newIncomeCard", "hit_new_income_page": False, "online_status": 0},
        "driver/homeNewSheet": {"component_id": "driver_homeNewSheet", "is_car_offline": 1, "nt": 1, "session_id": 1713529366832, "locale": "pt-BR", "request_source": "home_resume", "car_type": 1300, "business_ids": "316", "is_order_finish": 0, "first_biz": 1, "width": 1080, "bottom_sheet_state": 0, "nl": 1, "country_id": "BR", "height": 2179},
        "driver/dGetDestinationStatus": {},
        "minos/mCheckTermsOfService": {},
        "driver/dIndexInfo": {"new_version": 1},
        "pmc/getLegalNoticeV3": {"appID": 70010}
    },
    "common": {
        "utc_offset": -180,
        "ticket": "WSqKC1dcjmW1A5pBwS-imbNMc5jUb7FtgJTaDd77EvgszD2OwjAQgNG7fO2Oohnb49hT7hH2BguEn8ZIIKood0coVK97K0MJ8qSTIgwjTBiJmFVNhZGJD2XHCX7_EP4JEA5Ede1mnqsV660LJ2IWFmLleX89jss324QzYbNlT63WJlwIftyT9VZKKS05wnWfb4Ru7wAAAP__",
        "app_version": "7.8.18",
        "lng": -46.67567750387063,
        "location_country": "BR",
        "biz_type": 2,
        "origin_id": 5,
        "appversioncode": 1107081805,
        "a3_token": "cE82pfJyNTjnBnXuTeQWGm2AKWbRLmYA2Mq/XfxUH2iq6M1nhGy6arh8/t17hUL/g+/kJY8FbSzjF2T1JPTAbtO8FBSesXpkGhC3lCDNsTa07mNAbUPNM3aK9NnTzq5z",
        "appversion_code": 1107081805,
        "map_type": "wgs84",
        "location_cityid": 55000199,
        "device_info": {"deviceid": "efb848d3dd2f308c117204af7c860a3a", "model": "M2007J20CG", "os": "11", "osType": "android"},
        "datatype": "2",
        "product_id": 316,
        "wsgenv": "eV60A2vj/bLfHi2mAAAAA2AEAABvBezZM/ebFTA122NsC2hahtiQWZEQ/yX+xk2uvHVfx5/+wGaFoAf5jntYWQV7Y+sgsHM9qFr+LkHF/1Mt7TKhptkK6fQJw++sz6KjWE3LAT0E9QKyXqDD09FbXOEEQ2AGRtaPkEWWJG9hlWf7CRAm6qxfLWAnCRJY48lUkZlnPx16sRJ00rYyyP7fzTitWr3B2jEZAFDdOgH7MP/F6+ax88o5LNAtTRA3RyOOL1GkYhJRsAQLLkwDg+OVyw3IOtZ4QlGAh242LmiVP27vYqzYQ0vyZTUMVnE6XE1Zux5I4CUPxWRikWOD6nvdViES5CNCQU8RXyl0jBBA450z2VFNHca0Isb011SBw"
    }
}


d = '{"abilities":{"driver/dGeoReverse":{"lng":-46.67567502177737,"lat":-23.58897906655345},"expo/driverDispatch":{"component_id":"driver_dispatch_tool"},"driver/index-settings-v2":{"is_self_built_map":0},"driver/newIncomeCard":{"component_id":"driver_newIncomeCard","hit_new_income_page":false,"online_status":0},"driver/homeNewSheet":{"component_id":"driver_homeNewSheet","is_car_offline":1,"nt":1,"session_id":1713529366832,"locale":"pt-BR","request_source":"home_resume","car_type":1300,"business_ids":"316","is_order_finish":0,"first_biz":1,"width":1080,"bottom_sheet_state":0,"nl":1,"country_id":"BR","height":2179},"driver/dGetDestinationStatus":{},"minos/mCheckTermsOfService":{},"driver/dIndexInfo":{"new_version":1},"pmc/getLegalNoticeV3":{"appID":70010}},"common":{"utc_offset":-180,"ticket":"WSqKC1dcjmW1A5pBwS-imbNMc5jUb7FtgJTaDd77EvgszD2OwjAQgNG7fO2Oohnb49hT7hH2BguEn8ZIIKood0coVK97K0MJ8qSTIgwjTBiJmFVNhZGJD2XHCX7_EP4JEA5Ede1mnqsV660LJ2IWFmLleX89jss324QzYbNlT63WJlwIftyT9VZKKS05wnWfb4Ru7wAAAP__","app_version":"7.8.18","lng":-46.67567750387063,"location_country":"BR","biz_type":2,"origin_id":5,"appversioncode":1107081805,"a3_token":"cE82pfJyNTjnBnXuTeQWGm2AKWbRLmYA2Mq/XfxUH2iq6M1nhGy6arh8/t17hUL/g+/kJY8FbSzjF2T1JPTAbtO8FBSesXpkGhC3lCDNsTa07mNAbUPNM3aK9NnTzq5z","appversion_code":1107081805,"map_type":"wgs84","location_cityid":55000199,"device_info":{"deviceid":"efb848d3dd2f308c117204af7c860a3a","model":"M2007J20CG","os":"11","osType":"android"},"datatype":"2","product_id":316,"wsgenv":"eV60A2vj/bLfHi2mAAAAA2AEAABvBezZM/ebFTA122NsC2hahtiQWZEQ/yX+xk2uvHVfx5/+wGaFoAf5jntYWQV7Y+sgsHM9qFr+LkHF/1Mt7TKhptkK6fQJw++sz6KjWE3LAT0E9QKyXqDD09FbXOEEQ2AGRtaPkEWWJG9hlWf7CRAm6qxfLWAnCRJY48lUkZlnPx16sRJ00rYyyP7fzTitWr3B2jEZAFDdOgH7MP/F6+ax88o5LNAtTRA3RyOOL1GkYhJRsAQLLkwDg+OVyw3IOtZ4QlGAh242LmiVP27vYqzYQ0vyZTUMVnE6XE1Zux5I4CUPxWRikWOD6nvdViES5CNCQU8RXyl0jBBA450z2VFNHca0Isb011SBwTXVUhhCYvnZ77elzm9LDQmN8tx5x0x2ukZcKDt3FZubmjfXMn5pUMuM3syTAEWyU13Ws9k6u77LXP4obGF5NrBHja+XmZT1H6muzwL9VjltivUSJH536pA1gcqCtmRfXkL9YcOcL7dONEqoBvZ0uwrASlJP2iXR3O4YU1HBLgLY1CcOPUJtmCu6DaNNCgXy3H942BbjSPGZ+7/mCrQPQje6d7+1Sk647Ci/miaZ/Kw6Pifo9UYjTscKQF7izg4aIRsR6jL46+ppopCfcM/Z9bCC6l5UMVOB7uJOmZw8crTpyfLGFXGMlfMRPaqbxai59Ujrkde48KMPkq357LL8xxMZlg4fIt3KZOuFqia+MMn0WiN6HzeR3+du6tlNBRvHxfsWYexYKjFCMr2JpjfzqJsvHR5CPpS21uz3pp+vcT3zBhbubKyl3gUj/Y+NDjMEx90LoBQI+JCX/vlESWAH9wFcItVxW2jmPV7JzwR6qGRCOBq00L+v4hZFQd5jcv6SGDAFem3Il6hkwPKSNTfWNpS81MYW/qinhSfimgobrHjf4b3zLxwSltxGc3Cnd5aAxjNb/KsH4yUjAH8rFIaWdbyZ2ZBfXJiHnEbjkZG3IYhRSy47WOyOQKPWzWe0XfoYLEvFCFEFI3OrkJ6hik8aUTx5i5qm5kciltoRvchpQ5IpHmMJOUktY/WvvhqxVfnDvrAqGCvzAgLBWBVRNzl7IT+cgVCBeSaHrd+G2QMSJwYAfcdTCyJZAKbcIcrHaAfrrcD5cdKeHovvmzVKzYJVIQemH9jJsAopL7yPNfKIx6zL63sGdSOrQKquzABWOtPvInNh57UXI2ykmZXzQqdg900FZ/kntt3Esr5GqnweEHoRsRGLfYSAH","data_type":"2","lang":"pt-BR","networkType":"GPRS","lat":-23.588979248519674,"terminal_id":5}}'





response = requests.post(url, headers=headers, data=d, verify=False)
print(response.text)  # Imprimir a resposta

