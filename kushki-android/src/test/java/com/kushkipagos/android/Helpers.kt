package com.kushkipagos.android

import org.json.JSONArray
import org.json.JSONObject

internal object Helpers {

    fun buildResponse(code: String, message: String, token: String = ""): String {
        return JSONObject()
                .put("code", code)
                .put("message", message)
                .put("token", token)
                .toString()
    }

    fun buildBankListResponse():String {
        val response = JSONArray()

        for (i in 0..3){
            var json = JSONObject()
            json.put("code",i)
            json.put("name","Banco $i")
            response.put(i,json)
        }

        return response.toString()
    }

    fun buildSecureValidationResponse(code: String, message: String, questionnaireCode: String = ""):String{

        var questions = JSONArray()
        val response = JSONObject()
        val question1 = JSONObject(
                """
                {
                    "text": "DE LOS SIGUIENTES TELEFONOS INDIQUE CON CUAL USTED HA TENIDO O TIENE ALGUN VINCULO",
                    "options": [
                        "516216026",
                        "516217026",
                        "516215016",
                        "NINGUNA DE LAS ANTERIORES"
                    ],
                    "id": "1"
                }
           """
        )
        val question2 = JSONObject(
                """
                {
                    "text": "EN JULIO DE 2019 SU CUENTA CORRIENTE CON 'BCO POPULAR' ",
                    "options": [
                        "ESTABA ABIERTA/VIGENTE",
                        "ESTABA CANCELADA/SALDADA/CERRADA",
                        "NUNCA HE TENIDO CUENTA CORRIENTE CON LA ENTIDAD"
                    ],
                    "id": "2"
                }
           """
        )
        val question3 = JSONObject(
                """
                {
                    "text": "A JULIO DE 2019 EL SALDO DE SU CREDITO HIPOTECARIO CON 'BANCO AV VILLAS (AHORRAMAS)' ESTABA ENTRE",
                    "options": [
                        "${'$'}144723001-${'$'}202612000",
                        "202612001-${'$'}260501000",
                        "${'$'}260501001-${'$'}318390000",
                        "${'$'}318390001-${'$'}376279000",
                        "${'$'}376279001-${'$'}434168000",
                        "NO TENGO CREDITO HIPOTECARIO CON LA ENTIDAD"
                    ],
                    "id": "3"
                }
           """
        )

        if(code == "OTP300") questions = JSONArray()
        else
        questions
                .put(0,question1)
                .put(1,question2)
                .put(2,question3)
        return response
                .put("code",code)
                .put("message",message)
                .put("questionnaireCode",questionnaireCode)
                .put("questions",questions)
                .toString()

    }




}
