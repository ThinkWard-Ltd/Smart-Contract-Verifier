package com.savannasolutions.SmartContractVerifierServer.negotiation.responses

import com.fasterxml.jackson.annotation.JsonProperty
import com.savannasolutions.SmartContractVerifierServer.common.commonDataObjects.ResponseStatus
import java.util.*

data class CreateConditionResponse(@JsonProperty("ConditionID") val conditionID: UUID? = null,)
