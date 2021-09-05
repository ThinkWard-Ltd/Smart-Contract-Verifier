package com.savannasolutions.SmartContractVerifierServer.negotiation.responses

import com.fasterxml.jackson.annotation.JsonProperty
import com.savannasolutions.SmartContractVerifierServer.common.commonDataObjects.ResponseStatus
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation
import java.util.*
import kotlin.collections.ArrayList

data class CreateAgreementResponse(@JsonProperty("AgreementID") val agreementID: UUID? = null,)
{
    companion object{
        fun response(): ArrayList<FieldDescriptor>{
            val fieldDescriptor = ArrayList<FieldDescriptor>()
            fieldDescriptor.add(PayloadDocumentation.fieldWithPath("ResponseObject.AgreementID").description("This is the unique ID assigned to the contract").type("UUID"))
            return fieldDescriptor
        }
    }
}
