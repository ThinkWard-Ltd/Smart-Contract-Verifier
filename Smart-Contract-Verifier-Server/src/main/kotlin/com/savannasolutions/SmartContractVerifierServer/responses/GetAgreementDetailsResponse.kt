package com.savannasolutions.SmartContractVerifierServer.responses

import com.savannasolutions.SmartContractVerifierServer.models.Conditions
import java.time.Duration
import java.util.*

data class GetAgreementDetailsResponse(val agreementID: UUID,
                                        val duration: Long?,
                                        val partyA: String?,
                                        val partyB: String?,
                                        val createdDate: Date?,
                                        val sealedDate: Date?,
                                        val movedToBlockchain: Boolean?,
                                        val conditions: List<UUID>?,
                                        val status: Enum<ResponseStatus>,)

