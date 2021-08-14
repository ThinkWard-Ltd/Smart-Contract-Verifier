package com.savannasolutions.SmartContractVerifierServer.negotiation.requests

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class SealAgreementRequest(@JsonProperty("AgreementID") val AgreementID: UUID,
                                val index: Int = -1)
