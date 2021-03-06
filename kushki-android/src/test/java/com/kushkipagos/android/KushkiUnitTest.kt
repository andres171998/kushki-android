package com.kushkipagos.android

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.kushkipagos.android.Helpers.buildBankListResponse
import com.kushkipagos.android.Helpers.buildBinInfoResponse
import com.kushkipagos.android.Helpers.buildResponse
import com.kushkipagos.android.Helpers.buildSecureValidationResponse
import org.apache.commons.lang3.RandomStringUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import java.net.HttpURLConnection

class KushkiUnitTest {

    @Rule
    @JvmField
    val wireMockRule = WireMockRule(8888)
    private val totalAmount = 10.0
    private val validCard = Card("John Doe", "5321952125169352", "123", "12", "21")
    private val invalidCard = Card("Invalid John Doe", "4242424242", "123", "12", "21")
    private val kushki = Kushki("10000002036955013614148494909956", "USD", TestEnvironment.LOCAL)
    private val kushkiTransfer = Kushki("7871421bff4c4c19a925a50dc6b70af3", "USD", TestEnvironment.LOCAL)
    private val kushkiTransferSubscription = Kushki("20000000107415376000", "COP", TestEnvironment.LOCAL)
    private val kushkiSingleIP = Kushki("10000002036955013614148494909956", "USD", TestEnvironment.LOCAL,true)
    private val kushkiCardAsync = Kushki("10000002667885476032150186346335", "CLP", TestEnvironment.LOCAL)
    private val kushkiCardAsyncErrorMerchant = Kushki("20000000", "CLP", TestEnvironment.LOCAL)
    private val kushkiCardAsyncErrorCurrency = Kushki("10000002667885476032150186346335", "CCC", TestEnvironment.LOCAL)
    private val kushkiCash = Kushki("6000000000154083361249085016881", "USD", TestEnvironment.LOCAL)
    private val kushkiCashErrorMerchant = Kushki("20000000", "USD", TestEnvironment.LOCAL)
    private val kushkiCashErrorCurrency = Kushki("10000002667885476032150186346335", "CCC", TestEnvironment.LOCAL)
    private val kushkiBankList = Kushki("20000000107415376000","COP",TestEnvironment.LOCAL)
    private val kushkiBinInfo = Kushki("10000002036955013614148494909956","USD",TestEnvironment.LOCAL)
    private val totalAmountCardAsync = 1000.00
    private val kushkiSubscriptionTransfer = TransferSubscriptions("892352","1","jose","gonzalez",
            "123123123","CC","01",12,"tes@kushkipagos.com","USD")
    private val kushkiCardSubscriptionAsync = Kushki("e955d8c491674b08869f0fe6f480c63e", "CLP", TestEnvironment.LOCAL_QA)
    private val kushkiCardSubscriptionAsyncErrorMerchant = Kushki("20000000103303102000", "CLP", TestEnvironment.LOCAL_QA)

    private val name = "José"
    private val lastName = "Fernn"
    private val identification = "1721834349"
    private val returnUrl = "https://return.url"
    private val description = "Description test"
    private val email = "email@test.com"
    private val amount = Amount(10.0,0.0,1.2)
    private val callbackUrl = "www.kushkipagos.com"
    private val callbackUrlAsync = "https://mycallbackurl.com"
    private val userType = "0"
    private val documentType = "NIT"
    private val documentNumber = "892352"
    private val currency = "CLP"
    private val paymentDescription = "Test JD"
    private val cityCode = "1"
    private val stateCode = "13"
    private val phone = "00987654321"
    private val expeditionDate = "15/12/1958"
    private val answers = JSONArray("""
       [
        {
            "id": "1",
            "answer": "1"
        },
        {
            "id": "2",
            "answer": "1"
        },
        {
            "id": "3",
            "answer": "1"
        },
        {
            "id": "4",
            "answer": "1"
        }
        ]
    """)
    private val answersInvalid = JSONArray("""
        [
            {
                "id": "asdasd",
                "answer": "asdasd"
            }
        ]
    """)
    private val cardNumber = "4242424242424242"
    @Test
    @Throws(KushkiException::class)
    fun shouldReturnTokenWhenCalledWithValidParams() {
        val token = RandomStringUtils.randomAlphanumeric(32)
        val expectedRequestBody = buildExpectedRequestBody(validCard, totalAmount)
        val responseBody = buildResponse("000", "", token)
        stubTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushki.requestToken(validCard, totalAmount)
        System.out.println(transaction.token)
        System.out.println(token)
        assertThat(transaction.token.length, equalTo(32))
    }



    @Test
    @Throws(KushkiException::class)
    fun shouldReturnErrorMessageWhenCalledWithInvalidParams() {
        val errorCode = RandomStringUtils.randomNumeric(3)
        val errorMessage = "Cuerpo de la petición inválido."
        val expectedRequestBody = buildExpectedRequestBody(invalidCard, totalAmount)
        val responseBody = buildResponse(errorCode, errorMessage)
        stubTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_PAYMENT_REQUIRED)
        val transaction = kushki.requestToken(invalidCard, totalAmount)
        assertThat(transaction.token, equalTo(""))
        assertThat(transaction.code, equalTo("K001"))
        assertThat(transaction.message, equalTo(errorMessage))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnOKMessageWhenCalledWithSingleIP() {
        val token = RandomStringUtils.randomAlphanumeric(32)
        val expectedRequestBody = buildExpectedRequestBody(validCard, totalAmount)
        val responseBody = buildResponse("000", "", token)
        stubTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiSingleIP.requestToken(validCard, totalAmount)
        System.out.println(transaction.token)
        System.out.println(token)
        assertThat(transaction.message, equalTo(""))
        assertThat(transaction.token.length, equalTo(32))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnSubscriptionTokenWhenCalledWithValidParams() {
        val token = RandomStringUtils.randomAlphanumeric(32)
        val expectedRequestBody = buildExpectedSubscriptionRequestBody(validCard)
        val responseBody = buildResponse("000", "", token)
        stubSubscriptionTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushki.requestSubscriptionToken(validCard)
        assertThat(transaction.token.length, equalTo(32))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnErrorMessageWhenCalledWithInvalidSubscriptionParams() {
        val errorCode = RandomStringUtils.randomNumeric(3)
        val errorMessage = "Cuerpo de la petición inválido."
        val expectedRequestBody = buildExpectedSubscriptionRequestBody(invalidCard)
        val responseBody = buildResponse(errorCode, errorMessage)
        stubSubscriptionTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_PAYMENT_REQUIRED)
        val transaction = kushki.requestSubscriptionToken(invalidCard)
        assertThat(transaction.token, equalTo(""))
        assertThat(transaction.code, equalTo("K001"))
        assertThat(transaction.message, equalTo(errorMessage))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnCardAsyncTokenWhenCalledWithValidParams() {
        val token = RandomStringUtils.randomAlphanumeric(32)
        val expectedRequestBody = buildExpectedRequestCardAsyncBody(totalAmountCardAsync,returnUrl, description, email)
        val responseBody = buildResponse("000", "", token)
        stubCardAsyncTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiCardAsync.requestCardAsyncToken(totalAmountCardAsync,returnUrl, description,email)
        System.out.println(transaction.token)
        System.out.println(token)
        assertThat(transaction.token.length, equalTo(32))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnCardAsyncTokenWhenCalledWithIncompleteParams() {
        val token = RandomStringUtils.randomAlphanumeric(32)
        val expectedRequestBody = buildExpectedRequestCardAsyncBodyIncomplete(totalAmountCardAsync,returnUrl)
        val responseBody = buildResponse("000", "", token)
        stubCardAsyncTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiCardAsync.requestCardAsyncToken(totalAmountCardAsync,returnUrl)
        System.out.println(transaction.token)
        System.out.println(token)
        assertThat(transaction.token.length, equalTo(32))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnCardAsyncTokenWhenCalledWithIncompleteParamsOnlyEmail() {
        val token = RandomStringUtils.randomAlphanumeric(32)
        val expectedRequestBody = buildExpectedRequestCardAsyncBodyOnlyEmail(totalAmountCardAsync,returnUrl,email)
        val responseBody = buildResponse("000", "", token)
        stubCardAsyncTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiCardAsync.requestCardAsyncToken(totalAmountCardAsync,returnUrl)
        System.out.println(transaction.token)
        System.out.println(token)
        assertThat(transaction.token.length, equalTo(32))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnCashTokenWhenCalledWithValidParams() {
        val token = RandomStringUtils.randomAlphanumeric(32)
        val expectedRequestBody = buildExpectedRequestCashBody(name, lastName, identification, documentType,
                email,totalAmount, currency, description)
        val responseBody = buildResponse("000", "", token)
        stubCashTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiCash.requestCashToken(name, lastName, identification, documentType,
                email,totalAmount, "USD", description)
        System.out.println(transaction.token)
        System.out.println(token)
        assertThat(transaction.token.length, equalTo(32))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnCashTokenWhenCalledWithIncompleteParams() {
        val token = RandomStringUtils.randomAlphanumeric(32)
        val expectedRequestBody = buildExpectedRequestCashBodyIncomplete(name, lastName, identification, documentType,
                totalAmount, currency)
        val responseBody = buildResponse("000", "", token)
        stubCashTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiCash.requestCashToken(name, lastName, identification, documentType, totalAmount, "USD")
        System.out.println(transaction.token)
        System.out.println(token)
        assertThat(transaction.token.length, equalTo(32))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnCashTokenWhenCalledWithIncompleteParamsOnlyEmail() {
        val token = RandomStringUtils.randomAlphanumeric(32)
        val expectedRequestBody = buildExpectedRequestCashBodyIncompleteOnlyEmail(
                name, lastName, identification, documentType, email, totalAmount, currency)
        val responseBody = buildResponse("000", "", token)
        stubCashTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiCash.requestCashToken(name, lastName, identification, documentType, email, totalAmount, "USD")
        System.out.println(transaction.token)
        System.out.println(token)
        assertThat(transaction.token.length, equalTo(32))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnTransferSubscriptionTokenWhenCalledWithCompleteParamsOnlyEmail() {
        val token = RandomStringUtils.randomAlphanumeric(32)
        val expectedRequestBody = buildRequestTransferSubscriptionMessage(kushkiSubscriptionTransfer)
        val responseBody = buildResponse("000", "", token)
        stubTransferSubscriptionTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiTransferSubscription.requestTransferSubscriptionToken(kushkiSubscriptionTransfer)
        System.out.println(transaction.token)
        System.out.println(token)
        assertThat(transaction.token.length, equalTo(32))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnErrorMessageWhenCalledWithInvalidMerchant() {
        val errorCode = RandomStringUtils.randomNumeric(3)
        val errorMessage = "ID de comercio o credencial no válido"
        val expectedRequestBody = buildExpectedRequestCardAsyncBody(totalAmountCardAsync, returnUrl, description, email)
        val responseBody = buildResponse(errorCode, errorMessage)
        stubCardAsyncTokenApiErrorMerchant(expectedRequestBody, responseBody, HttpURLConnection.HTTP_PAYMENT_REQUIRED)
        val transaction = kushkiCardAsyncErrorMerchant.requestCardAsyncToken(totalAmountCardAsync, returnUrl, description, email)
        assertThat(transaction.token, equalTo(""))
        assertThat(transaction.code, equalTo("CAS004"))
        assertThat(transaction.message, equalTo(errorMessage))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnErrorMessageWhenCalledWithInvalidCurrency() {
        val errorCode = RandomStringUtils.randomNumeric(3)
        val errorMessage = "Cuerpo de la petición inválido."
        val expectedRequestBody = buildExpectedRequestCardAsyncBody(totalAmountCardAsync, returnUrl, description, email)
        val responseBody = buildResponse(errorCode, errorMessage)
        stubCardAsyncTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_PAYMENT_REQUIRED)
        val transaction = kushkiCardAsyncErrorCurrency.requestCardAsyncToken(totalAmountCardAsync, returnUrl, description, email)
        assertThat(transaction.token, equalTo(""))
        assertThat(transaction.code, equalTo("CAS001"))
        assertThat(transaction.message, equalTo(errorMessage))
    }


    @Test
    @Throws(KushkiException::class)
    fun shouldReturnCashErrorMessageWhenCalledWithInvalidMerchant() {
        val errorCode = RandomStringUtils.randomNumeric(3)
        val errorMessage = "ID de comercio o credencial no válido"
        val expectedRequestBody = buildExpectedRequestCashBody(name, lastName, identification, documentType,
                email,totalAmount, currency, description)
        val responseBody = buildResponse(errorCode, errorMessage)
        stubCashAsyncTokenApiErrorMerchant(expectedRequestBody, responseBody, HttpURLConnection.HTTP_PAYMENT_REQUIRED)
        val transaction = kushkiCashErrorMerchant.requestCashToken(name, lastName, identification, documentType,
                email,totalAmount, currency, description)
        assertThat(transaction.token, equalTo(""))
        assertThat(transaction.code, equalTo("C004"))
        assertThat(transaction.message, equalTo(errorMessage))
    }


    @Test
    @Throws(KushkiException::class)
    fun shouldReturnCashErrorMessageWhenCalledWithInvalidCurrency() {
        val errorCode = RandomStringUtils.randomNumeric(3)
        val errorMessage = "Cuerpo de la petición inválido."
        val expectedRequestBody = buildExpectedRequestCashBody(name, lastName, identification, documentType,
                email,totalAmount, "ABC", description)
        val responseBody = buildResponse(errorCode, errorMessage)
        stubCashTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_PAYMENT_REQUIRED)
        val transaction = kushkiCash.requestCashToken(name, lastName, identification, documentType,
                email,totalAmount, "ABC", description)
        assertThat(transaction.token, equalTo(""))
        assertThat(transaction.code, equalTo("C001"))
        assertThat(transaction.message, equalTo(errorMessage))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnTransferTokenWhenCalledWithValidParams() {
        val token = RandomStringUtils.randomAlphanumeric(32)
        val expectedRequestBody = buildExpectedRequestTransferBody()
        val responseBody = buildResponse("000", "", token)
        stubTransferTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiTransfer.requestTransferToken(amount, callbackUrl, userType, documentType,
                documentNumber,email,currency)
        System.out.println(transaction.token)
        System.out.println(token)
        assertThat(transaction.token.length, equalTo(32))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnTransferTokenWhenCalledWithValidCompletedParams() {
        val token = RandomStringUtils.randomAlphanumeric(32)
        val expectedRequestBody = buildExpectedRequestTransferBody(paymentDescription)
        val responseBody = buildResponse("000", "", token)
        stubTransferTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiTransfer.requestTransferToken(amount, callbackUrl, userType, documentType,
                documentNumber,email,currency,paymentDescription)
        System.out.println(transaction.token)
        System.out.println(token)
        assertThat(transaction.token.length, equalTo(32))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnTransferTokenWhenCalledWithInvalidParams() {
        val errorCode = RandomStringUtils.randomNumeric(3)
        val errorMessage = "Cuerpo de la petición inválido."
        val expectedRequestBody = buildExpectedRequestTransferBody(paymentDescription,"test")
        val responseBody = buildResponse(errorCode, errorMessage)
        stubTransferTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_PAYMENT_REQUIRED)
        val transaction = kushki.requestTransferToken(amount, callbackUrl, "test", documentType,
                documentNumber,email,currency)
        assertThat(transaction.token, equalTo(""))
        assertThat(transaction.code, equalTo("T001"))
        assertThat(transaction.message, equalTo(errorMessage))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnBankListWhenCalledWithValidResponse() {
        val responseBody = buildBankListResponse()
        stubBankListApi(responseBody, HttpURLConnection.HTTP_OK)
        val banklist = kushkiBankList.getBankList()
        System.out.println(banklist.banks)
        System.out.println(banklist.banks[3])
        assertThat(banklist.banks, notNullValue())
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnBinInfoWhenCalledWithValidResponse() {
        val responseBody = buildBinInfoResponse()
        stubBinInfoApi(responseBody, HttpURLConnection.HTTP_OK)
        val binInfo = kushkiBinInfo.getBinInfo("465775")
        System.out.println(binInfo.bank)
        System.out.println(binInfo.brand)
        System.out.println(binInfo.cardType)
        assertThat(binInfo.bank, notNullValue())
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnAskQuestionnaireWhenCalledWithCompleteParams() {
        val expectedRequestBody = buildRequestTransferSubscriptionMessage(kushkiSubscriptionTransfer)
        val responseBody = buildSecureValidationResponse("000","", "02")
        stubSecureValidationApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiTransferSubscription.requestTransferSubscriptionToken(kushkiSubscriptionTransfer)
        val secureInfo = AskQuestionnaire(transaction.secureId,transaction.secureService,cityCode,stateCode,phone,expeditionDate)
        val secureValidation = kushkiTransferSubscription.requestSecureValidation(secureInfo)
        assertThat(secureValidation.questions.length(), equalTo(3))
        System.out.println(secureValidation.questions.
                getJSONObject(0).
                getJSONArray("options").
                getJSONObject(0).
                get("text"))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnOTPExpiradoMessageWhenCalledWithInvalidSucureServiceId() {
        val errorCode = "OTP300"
        val errorMessage = "OTP expirado"
        val expectedRequestBody = buildRequestTransferSubscriptionMessage(kushkiSubscriptionTransfer)
        val responseBody = buildSecureValidationResponse(errorCode,errorMessage, "")
        stubSecureValidationApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val askQuestionnaire = AskQuestionnaire("InvalidId","confronta",cityCode,stateCode,phone,expeditionDate)
        val secureValidation = kushkiTransferSubscription.requestSecureValidation(askQuestionnaire)
        assertThat(secureValidation.questions.length(), equalTo(0))
        assertThat(secureValidation.code, equalTo("OTP300"))
        assertThat(secureValidation.message, equalTo("OTP expirado"))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnErrorMessageWhenCalledWithInvalidConfrontaBiometrics() {
        val errorCode = "TR006"
        val errorMessage = "Cuerpo de petición inválido"
        val expectedRequestBody = buildRequestTransferSubscriptionMessage(kushkiSubscriptionTransfer)
        val responseBody = buildSecureValidationResponse(errorCode,errorMessage, "")
        stubSecureValidationApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiTransferSubscription.requestTransferSubscriptionToken(kushkiSubscriptionTransfer)
        val askQuestionnaire = AskQuestionnaire(transaction.secureId,transaction.secureService,"","","","")
        val secureValidation = kushkiTransferSubscription.requestSecureValidation(askQuestionnaire)
        assertThat(secureValidation.questions.length(), equalTo(0))
        assertThat(secureValidation.code, equalTo("TR006"))
        assertThat(secureValidation.message, equalTo("Cuerpo de petición inválido"))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnOkMessageWhenCalledWithValidAnswers() {
        val expectedRequestBody = buildRequestTransferSubscriptionMessage(kushkiSubscriptionTransfer)
        val responseBody = buildSecureValidationResponse("000","", "02")
        stubSecureValidationApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiTransferSubscription.requestTransferSubscriptionToken(kushkiSubscriptionTransfer)
        val askQuestionnaire = AskQuestionnaire(transaction.secureId,transaction.secureService,cityCode,stateCode,phone,expeditionDate)
        var secureValidation = kushkiTransferSubscription.requestSecureValidation(askQuestionnaire)
        println(transaction.secureId)
        println(transaction.secureService)
        val validateAnswers = ValidateAnswers(transaction.secureId,transaction.secureService,"7224",answers)
        secureValidation = kushkiTransferSubscription.requestSecureValidation(validateAnswers)
        assertThat(secureValidation.message, equalTo("ok"))
        assertThat(secureValidation.code, equalTo("BIO000"))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnInvalidUserMessageWhenCalledWithInvalidAnswers() {
        val expectedRequestBody = buildRequestTransferSubscriptionMessage(kushkiSubscriptionTransfer)
        val responseBody = buildSecureValidationResponse("000","", "02")
        stubSecureValidationApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiTransferSubscription.requestTransferSubscriptionToken(kushkiSubscriptionTransfer)
        val askQuestionnaire = AskQuestionnaire(transaction.secureId,transaction.secureService,cityCode,stateCode,phone,expeditionDate)
        var secureValidation = kushkiTransferSubscription.requestSecureValidation(askQuestionnaire)
        val validateAnswers = ValidateAnswers(transaction.secureId,transaction.secureService,"3123",answersInvalid)
        secureValidation = kushkiTransferSubscription.requestSecureValidation(validateAnswers)
        assertThat(secureValidation.code, equalTo("BIO100"))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnCardSubscriptionAsyncTokenWhenCalledWithValidParams() {
        val token = RandomStringUtils.randomAlphanumeric(32)
        val expectedRequestBody = buildExpectedRequestCardSubscriptionAsyncBody(email, currency, callbackUrlAsync, cardNumber)
        val responseBody = buildResponse("000", "", token)
        stubCardSubscriptionAsyncTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiCardSubscriptionAsync.requestCardSubscriptionAsyncToken(email, currency, callbackUrlAsync, cardNumber)
        System.out.println(transaction.token)
        System.out.println(token)
        assertThat(transaction.token.length, equalTo(32))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnCardSubscriptionAsyncTokenCalledWithIncompleteParams() {
        val token = RandomStringUtils.randomAlphanumeric(32)
        val expectedRequestBody = buildExpectedRequestCardSubscriptionAsyncIncompleteBody(email, currency, callbackUrlAsync)
        val responseBody = buildResponse("000", "", token)
        stubCardSubscriptionAsyncTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_OK)
        val transaction = kushkiCardSubscriptionAsync.requestCardSubscriptionAsyncToken(email, currency, callbackUrlAsync)
        System.out.println(transaction.token)
        System.out.println(token)
        assertThat(transaction.token.length, equalTo(32))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnCardSubscriptionAsyncErrorMessageWhenCalledWithInvalidMerchant() {
        val errorCode = RandomStringUtils.randomNumeric(3)
        val errorMessage = "ID de comercio o credencial no válido"
        val expectedRequestBody = buildExpectedRequestCardSubscriptionAsyncBody(email, currency, callbackUrlAsync, cardNumber)
        val responseBody = buildResponse(errorCode, errorMessage)
        stubSubscriptionCardAsyncTokenApiErrorMerchant(expectedRequestBody, responseBody, HttpURLConnection.HTTP_PAYMENT_REQUIRED)
        val transaction = kushkiCardSubscriptionAsyncErrorMerchant.requestCardSubscriptionAsyncToken(email, currency, callbackUrlAsync, cardNumber)
        assertThat(transaction.token, equalTo(""))
        assertThat(transaction.code, equalTo("K004"))
        assertThat(transaction.message, equalTo(errorMessage))
    }

    @Test
    @Throws(KushkiException::class)
    fun shouldReturnCardSubscriptionAsyncMessageWhenCalledWithInvalidCurrency() {
        val errorCode = RandomStringUtils.randomNumeric(3)
        val errorMessage = "Cuerpo de la petición inválido."
        val expectedRequestBody = buildExpectedRequestCardSubscriptionAsyncBody(email, "USD", callbackUrlAsync, cardNumber)
        val responseBody = buildResponse(errorCode, errorMessage)
        stubCardSubscriptionAsyncTokenApi(expectedRequestBody, responseBody, HttpURLConnection.HTTP_PAYMENT_REQUIRED)
        val transaction = kushkiCardSubscriptionAsync.requestCardSubscriptionAsyncToken(email, "USD", callbackUrlAsync, cardNumber)
        assertThat(transaction.token, equalTo(""))
        assertThat(transaction.code, equalTo("K001"))
        assertThat(transaction.message, equalTo(errorMessage))
    }


    private fun stubTokenApi(expectedRequestBody: String, responseBody: String, status: Int) {
        System.out.println("response---body")
        System.out.println(responseBody)
        wireMockRule.stubFor(post(urlEqualTo("v1/tokens"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Public-Merchant-Id", "10000001656015280078454110039965")
                        .withBody(responseBody)))
    }

    private fun stubSubscriptionTokenApi(expectedRequestBody: String, responseBody: String, status: Int) {
        wireMockRule.stubFor(post(urlEqualTo("v1/subscription-tokens"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Public-Merchant-Id", "10000001656015280078454110039965")
                        .withBody(responseBody)))
    }

    private fun stubTransferTokenApi(expectedRequestBody: String, responseBody: String, status: Int) {
        wireMockRule.stubFor(post(urlEqualTo("transfer/v1/tokens"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Public-Merchant-Id", "200000001030988")
                        .withBody(responseBody)))
    }

    private fun stubCardAsyncTokenApi(expectedRequestBody: String, responseBody: String, status: Int) {
        System.out.println("response---body")
        System.out.println(responseBody)
        wireMockRule.stubFor(post(urlEqualTo("card-async/v1/tokens"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Public-Merchant-Id", "20000000103098876000")
                        .withBody(responseBody)))
    }

    private fun stubCashTokenApi(expectedRequestBody: String, responseBody: String, status: Int) {
        System.out.println("response---body")
        System.out.println(responseBody)
        wireMockRule.stubFor(post(urlEqualTo("cash/v1/tokens"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Public-Merchant-Id", "6000000000154083361249085016881")
                        .withBody(responseBody)))
    }

    private fun stubTransferSubscriptionTokenApi(expectedRequestBody: String, responseBody: String, status: Int) {
        System.out.println("response---body")
        System.out.println(responseBody)
        wireMockRule.stubFor(post(urlEqualTo("v1/transfer-subscription-tokens"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Public-Merchant-Id", "20000000103098876000")
                        .withBody(responseBody)))
    }

    private fun stubCardAsyncTokenApiErrorMerchant(expectedRequestBody: String, responseBody: String, status: Int) {
        System.out.println("response---body")
        System.out.println(responseBody)
        wireMockRule.stubFor(post(urlEqualTo("card-async/v1/tokens"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Public-Merchant-Id", "200000001030988")
                        .withBody(responseBody)))
    }

    private fun stubCashAsyncTokenApiErrorMerchant(expectedRequestBody: String, responseBody: String, status: Int) {
        System.out.println("response---body")
        System.out.println(responseBody)
        wireMockRule.stubFor(post(urlEqualTo("card-async/v1/tokens"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Public-Merchant-Id", "200000001030988")
                        .withBody(responseBody)))
    }

    private fun stubBankListApi(responseBody: String, status: Int) {
        System.out.println("response---body")
        System.out.println(responseBody)
        wireMockRule.stubFor(get(urlEqualTo("transfer-subscriptions/v1/bankList"))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Public-Merchant-Id", "20000000100323955000")
                        .withBody(responseBody)))
    }

    private fun stubBinInfoApi(responseBody: String, status: Int) {
        System.out.println("response---body")
        System.out.println(responseBody)
        wireMockRule.stubFor(get(urlEqualTo("card/v1/bin"))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Public-Merchant-Id", "20000000100323955000")
                        .withBody(responseBody)))
    }

    private fun stubSecureValidationApi(expectedRequestBody: String, responseBody: String, status: Int) {
        System.out.println("response---body")
        System.out.println(responseBody)
        wireMockRule.stubFor(post(urlEqualTo("rules/v1/secureValidation"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Public-Merchant-Id", "20000000107415376000")
                        .withBody(responseBody)))
    }

    private fun buildExpectedRequestBody(card: Card, totalAmount: Double): String {
            val expectedRequestMessage = buildRequestMessage(card, totalAmount)
        return expectedRequestMessage
    }

    private fun buildExpectedSubscriptionRequestBody(card: Card): String {
        val expectedRequestMessage = buildSubscriptionRequestMessage("10000001436354684173102102", card)

        return expectedRequestMessage
    }

    private fun buildSubscriptionRequestMessage(publicMerchantId: String, card: Card): String {
        try {
            val requestTokenParams = JSONObject()
            requestTokenParams.put("merchant_identifier", publicMerchantId)
            requestTokenParams.put("card", card.toJsonObject())
            return requestTokenParams.toString()
        } catch (e: JSONException) {
            throw IllegalArgumentException(e)
        }

    }

    private fun buildRequestMessage(card: Card, totalAmount: Double): String {
        try {
            val requestTokenParams = JSONObject()

            requestTokenParams.put("card", card.toJsonObject())
            requestTokenParams.put("totalAmount", totalAmount)
            return requestTokenParams.toString()
        } catch (e: JSONException) {
            throw IllegalArgumentException(e)
        }
    }

    private fun buildExpectedRequestTransferBody():String{
        val expectedRequestMessage = buildRequestTransferMessage(amount, callbackUrl, userType, documentType,
                documentNumber,email,currency
        )
        return expectedRequestMessage
    }

    private fun buildExpectedRequestTransferBody(paymentDescription:String,documentTypeAux: String="CC"):String{
        val expectedRequestMessage = buildRequestTransferMessage(amount, callbackUrl, userType, documentTypeAux,
                documentNumber,email,currency,paymentDescription
        )
        return expectedRequestMessage
    }
    private fun buildRequestTransferMessage(amount: Amount, callbackUrl: String, userType: String, documentType:String,documentNumber:String,
                                            email:String,currency:String): String {
        try {
            val requestTokenParams = JSONObject()
            requestTokenParams.put("amount", amount.toJsonObject())
            requestTokenParams.put("callbackUrl", callbackUrl)
            requestTokenParams.put("userType", userType)
            requestTokenParams.put("documentType", documentType)
            requestTokenParams.put("documentNumber", documentNumber)
            requestTokenParams.put("email", email)
            requestTokenParams.put("currency", currency)
            return requestTokenParams.toString()
        } catch (e: JSONException) {
            throw IllegalArgumentException(e)
        }

    }
    private fun buildRequestTransferMessage(amount: Amount, callbackUrl: String, userType: String, documentType:String,documentNumber:String,
                                            email:String,currency:String,paymentDescription:String): String {
        try {
            val requestTokenParams = JSONObject()
            requestTokenParams.put("amount", amount.toJsonObject())
            requestTokenParams.put("callbackUrl", callbackUrl)
            requestTokenParams.put("userType", userType)
            requestTokenParams.put("documentType", documentType)
            requestTokenParams.put("documentNumber", documentNumber)
            requestTokenParams.put("email", email)
            requestTokenParams.put("currency", currency)
            requestTokenParams.put("paymentDescription", paymentDescription)
            return requestTokenParams.toString()
        } catch (e: JSONException) {
            throw IllegalArgumentException(e)
        }

    }

    private fun buildExpectedRequestCardAsyncBody(totalAmount: Double, returnUrl: String, description: String, email: String ): String {
        val expectedRequestMessage = buildRequestCardAsyncMessage(totalAmount, returnUrl, description, email)
        return expectedRequestMessage
    }

    private fun buildRequestCardAsyncMessage(totalAmount: Double, returnUrl: String, description: String, email: String): String {
        try {
            val requestTokenParams = JSONObject()

            requestTokenParams.put("totalAmount", totalAmount)
            requestTokenParams.put("returnUrl", returnUrl)
            requestTokenParams.put("description", description)
            requestTokenParams.put("email", email)

            return requestTokenParams.toString()
        } catch (e: JSONException) {
            throw IllegalArgumentException(e)
        }
    }

    private fun buildExpectedRequestCashBody(name: String, lastName: String, identification: String, documentType: String,
                                             email: String,totalAmount: Double, currency: String, description: String): String {
        val expectedRequestMessage = buildRequestCashMessage(name, lastName, identification, documentType,
                email,totalAmount, currency, description)
        return expectedRequestMessage
    }

    private fun buildRequestCashMessage(name: String, lastName: String, identification: String, documentType: String,
                                        email: String,totalAmount: Double, currency: String, description: String): String {
        try {
            val requestTokenParams = JSONObject()

            requestTokenParams.put("name", name)
            requestTokenParams.put("lastName", lastName)
            requestTokenParams.put("identification", identification)
            requestTokenParams.put("documentType", documentType)
            requestTokenParams.put("email", email)
            requestTokenParams.put("totalAmount", totalAmount)
            requestTokenParams.put("currency", currency)
            requestTokenParams.put("description", description)


            return requestTokenParams.toString()
        } catch (e: JSONException) {
            throw IllegalArgumentException(e)
        }
    }

    private fun buildRequestTransferSubscriptionMessage(transferSubscriptions: TransferSubscriptions): String {
        try {
            val requestTokenParams = transferSubscriptions.toJsonObject()

            return requestTokenParams.toString()
        } catch (e: JSONException) {
            throw IllegalArgumentException(e)
        }
    }


    private fun buildExpectedRequestCardAsyncBodyIncomplete(totalAmount: Double, returnUrl: String ): String {
        val expectedRequestMessage = buildRequestCardAsyncMessageWithIncompleteParameters(totalAmount, returnUrl)
        return expectedRequestMessage
    }

    private fun buildRequestCardAsyncMessageWithIncompleteParameters(totalAmount: Double, returnUrl: String): String {
        try {
            val requestTokenParams = JSONObject()

            requestTokenParams.put("totalAmount", totalAmount)
            requestTokenParams.put("returnUrl", returnUrl)

            return requestTokenParams.toString()
        } catch (e: JSONException) {
            throw IllegalArgumentException(e)
        }
    }

    private fun buildExpectedRequestCardAsyncBodyOnlyEmail(totalAmount: Double, returnUrl: String, email: String ): String {
        val expectedRequestMessage = buildRequestCardAsyncMessageWithIncompleteOnlyEmail(totalAmount, returnUrl, email)
        return expectedRequestMessage
    }

    private fun buildRequestCardAsyncMessageWithIncompleteOnlyEmail(totalAmount: Double, returnUrl: String, email: String): String {
        try {
            val requestTokenParams = JSONObject()

            requestTokenParams.put("totalAmount", totalAmount)
            requestTokenParams.put("returnUrl", returnUrl)
            requestTokenParams.put("email", email)

            return requestTokenParams.toString()
        } catch (e: JSONException) {
            throw IllegalArgumentException(e)
        }
    }

    private fun buildRequestCashMessageWithIncompleteParameters(
            name: String, lastName: String, identification: String, documentType: String,
            totalAmount: Double, currency: String): String {
        try {
            val requestTokenParams = JSONObject()

            requestTokenParams.put("name", name)
            requestTokenParams.put("lastName", lastName)
            requestTokenParams.put("identification", identification)
            requestTokenParams.put("documentType", documentType)
            requestTokenParams.put("totalAmount", totalAmount)
            requestTokenParams.put("currency", currency)

            return requestTokenParams.toString()
        } catch (e: JSONException) {
            throw IllegalArgumentException(e)
        }
    }

    private fun buildExpectedRequestCashBodyIncomplete(
            name: String, lastName: String, identification: String,
            documentType: String, totalAmount: Double, currency: String ): String {
        val expectedRequestMessage = buildRequestCashMessageWithIncompleteParameters(
                name, lastName, identification, documentType, totalAmount,currency)
        return expectedRequestMessage
    }


    private fun buildRequestCashMessageWithIncompleteParametersOnlyEmail(
            name: String, lastName: String, identification: String, documentType: String,
            email: String, totalAmount: Double, currency: String): String {
        try {
            val requestTokenParams = JSONObject()

            requestTokenParams.put("name", name)
            requestTokenParams.put("lastName", lastName)
            requestTokenParams.put("identification", identification)
            requestTokenParams.put("documentType", documentType)
            requestTokenParams.put("email", email)
            requestTokenParams.put("totalAmount", totalAmount)
            requestTokenParams.put("currency", currency)

            return requestTokenParams.toString()
        } catch (e: JSONException) {
            throw IllegalArgumentException(e)
        }
    }

    private fun buildExpectedRequestCashBodyIncompleteOnlyEmail(
            name: String, lastName: String, identification: String,
            documentType: String, email:String ,totalAmount: Double, currency: String ): String {
        val expectedRequestMessage = buildRequestCashMessageWithIncompleteParametersOnlyEmail(
                name, lastName, identification, documentType, email, totalAmount,currency)
        return expectedRequestMessage
    }

    private fun buildExpectedRequestCardSubscriptionAsyncBody(email: String, currency: String, callbackUrl: String, cardNumber: String): String {
        val expectedRequestMessage = buildRequestCardSubscriptionAsyncMessage(email, currency, callbackUrl, cardNumber)
        return expectedRequestMessage
    }

    private fun buildRequestCardSubscriptionAsyncMessage(email: String, currency: String, callbackUrl: String, cardNumber: String): String {
        try {
            val requestTokenParams = JSONObject()

            requestTokenParams.put("email", email)
            requestTokenParams.put("currency", currency)
            requestTokenParams.put("callbackUrl", callbackUrl)
            requestTokenParams.put("cardNumber", cardNumber)

            return requestTokenParams.toString()
        } catch (e: JSONException) {
            throw IllegalArgumentException(e)
        }
    }

    private fun stubCardSubscriptionAsyncTokenApi(expectedRequestBody: String, responseBody: String, status: Int) {
        System.out.println("response---body")
        System.out.println(responseBody)
        wireMockRule.stubFor(post(urlEqualTo("subscriptions/v1/card-async/tokens"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Public-Merchant-Id", "e955d8c491674b08869f0fe6f480c63e")
                        .withBody(responseBody)))
    }

    private fun buildExpectedRequestCardSubscriptionAsyncIncompleteBody(email: String, currency: String, callbackUrl: String): String {
        val expectedRequestMessage = buildRequestCardSubscriptionAsyncIncompleteMessage(email, currency, callbackUrl)
        return expectedRequestMessage
    }

    private fun buildRequestCardSubscriptionAsyncIncompleteMessage(email: String, currency: String, callbackUrl: String): String {
        try {
            val requestTokenParams = JSONObject()

            requestTokenParams.put("email", email)
            requestTokenParams.put("currency", currency)
            requestTokenParams.put("callbackUrl", callbackUrl)

            return requestTokenParams.toString()
        } catch (e: JSONException) {
            throw IllegalArgumentException(e)
        }
    }

    private fun stubSubscriptionCardAsyncTokenApiErrorMerchant(expectedRequestBody: String, responseBody: String, status: Int) {
        System.out.println("response---body")
        System.out.println(responseBody)
        wireMockRule.stubFor(post(urlEqualTo("subscriptions/v1/card-async/tokens"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Public-Merchant-Id", "20000000103303102000")
                        .withBody(responseBody)))
    }
}
