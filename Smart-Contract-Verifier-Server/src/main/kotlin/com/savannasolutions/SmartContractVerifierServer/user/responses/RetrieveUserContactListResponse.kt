package com.savannasolutions.SmartContractVerifierServer.user.responses

import com.fasterxml.jackson.annotation.JsonProperty
import com.savannasolutions.SmartContractVerifierServer.common.commonDataObjects.ContactListIDContactListNameResponse
import com.savannasolutions.SmartContractVerifierServer.common.commonDataObjects.ResponseStatus

data class RetrieveUserContactListResponse(@JsonProperty("ContactListInfo") val ContactListInfo: List<ContactListIDContactListNameResponse>?= null,
                                           @JsonProperty("Status") val status: ResponseStatus,)
