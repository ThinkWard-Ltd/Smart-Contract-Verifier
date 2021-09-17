package com.savannasolutions.SmartContractVerifierServer.UnitTests.negotiation

import com.savannasolutions.SmartContractVerifierServer.common.commonDataObjects.ApiResponse
import com.savannasolutions.SmartContractVerifierServer.common.commonDataObjects.ResponseStatus
import com.savannasolutions.SmartContractVerifierServer.contracts.repositories.JudgesRepository
import com.savannasolutions.SmartContractVerifierServer.negotiation.models.Agreements
import com.savannasolutions.SmartContractVerifierServer.negotiation.models.ConditionStatus
import com.savannasolutions.SmartContractVerifierServer.negotiation.models.Conditions
import com.savannasolutions.SmartContractVerifierServer.negotiation.repositories.AgreementsRepository
import com.savannasolutions.SmartContractVerifierServer.negotiation.repositories.ConditionsRepository
import com.savannasolutions.SmartContractVerifierServer.negotiation.services.NegotiationService
import com.savannasolutions.SmartContractVerifierServer.user.models.User
import com.savannasolutions.SmartContractVerifierServer.user.repositories.UserRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

internal class AcceptConditionUnitTests {
    private val conditionsRepository : ConditionsRepository = mock()
    private val agreementsRepository : AgreementsRepository = mock()
    private val userRepository : UserRepository = mock()
    private val judgesRepository : JudgesRepository = mock()
    private val negotiationService = NegotiationService(agreementsRepository, conditionsRepository, userRepository, judgesRepository)

    private fun parameterizedAcceptCondition(conditionExists: Boolean,
                                             agreementExists: Boolean,
                                             conditionID: UUID,
                                             agreementID: UUID,
                                             status: ConditionStatus,
                                             moveToBlockchain: Boolean = false ): ApiResponse<Objects>
    {
        //Given
        var mockAgreementA = Agreements(
            agreementID,
            CreatedDate = Date(),
            MovedToBlockChain = moveToBlockchain,)

        val userA = User("UserA","uA")
        val userB = User("UserB", "uB")

        mockAgreementA = mockAgreementA.apply { users.add(userA) }
        mockAgreementA = mockAgreementA.apply { users.add(userB) }

        var mockConditionA = Conditions(conditionID,
            "title",
            "",
            status,
            Date()
        ).apply { contract = mockAgreementA }

        mockConditionA = mockConditionA.apply { proposingUser = userA }

        //When
        whenever(conditionsRepository.getById(conditionID)).thenReturn(mockConditionA)
        whenever(conditionsRepository.existsById(conditionID)).thenReturn(conditionExists)
        whenever(userRepository.existsById(userA.publicWalletID)).thenReturn(true)
        whenever(userRepository.getById(userA.publicWalletID)).thenReturn(userA)
        whenever(agreementsRepository.existsById(agreementID)).thenReturn(agreementExists)
        whenever(agreementsRepository.getById(agreementID)).thenReturn(mockAgreementA)

        //Then
        return negotiationService.acceptCondition(userB.publicWalletID, mockAgreementA.ContractID, conditionID)

    }

    @Test
    fun `acceptCondition successful accept`() {
        //Given

        //When
        val response = parameterizedAcceptCondition(conditionExists = true,
                                                    true,
                                                    UUID.fromString("57d34390-2ec2-4596-8627-c72fc0646712"),
                                                    UUID.fromString("71ffe65f-f823-4afd-85f2-cd7010d105ca"),
                                                    ConditionStatus.PENDING)

        //Then
        Assertions.assertEquals(response.status, ResponseStatus.SUCCESSFUL)
    }

    @Test
    fun `acceptCondition condition does not exist`()
    {
        //Given

        //When
        val response = parameterizedAcceptCondition(conditionExists = false,
                                             true,
                                                    UUID.fromString("57d34390-2ec2-4596-8627-c72fc0646712"),
                                                    UUID.fromString("71ffe65f-f823-4afd-85f2-cd7010d105ca"),
                                                    ConditionStatus.PENDING)


        //Then
        Assertions.assertEquals(response.status, ResponseStatus.FAILED)
    }

    @Test
    fun `acceptCondition condition is already rejected`(){
        //Given

        //when
        val response = parameterizedAcceptCondition(conditionExists = true,
            true,
            UUID.fromString("57d34390-2ec2-4596-8627-c72fc0646712"),
            UUID.fromString("71ffe65f-f823-4afd-85f2-cd7010d105ca"),
            ConditionStatus.REJECTED)


        //then
        Assertions.assertEquals(response.status, ResponseStatus.FAILED)

    }

    @Test
    fun `acceptCondition condition is already accepted`()
    {
        //Given

        //when
        val response = parameterizedAcceptCondition(conditionExists = true,
            true,
            UUID.fromString("57d34390-2ec2-4596-8627-c72fc0646712"),
            UUID.fromString("71ffe65f-f823-4afd-85f2-cd7010d105ca"),
            ConditionStatus.ACCEPTED)


        //then
        Assertions.assertEquals(response.status, ResponseStatus.FAILED)
    }

    @Test
    fun `acceptCondition agreement does not exist`()
    {
        //Given

        //When
        val response = parameterizedAcceptCondition(conditionExists = true,
            false,
            UUID.fromString("57d34390-2ec2-4596-8627-c72fc0646712"),
            UUID.fromString("71ffe65f-f823-4afd-85f2-cd7010d105ca"),
            ConditionStatus.PENDING)

        //Then
        Assertions.assertEquals(response.status, ResponseStatus.FAILED)
    }

    @Test
    fun `acceptCondition agreement has moved to blockchain`()
    {
        //Given

        //When
        val response = parameterizedAcceptCondition(conditionExists = true,
            true,
            UUID.fromString("57d34390-2ec2-4596-8627-c72fc0646712"),
            UUID.fromString("71ffe65f-f823-4afd-85f2-cd7010d105ca"),
            ConditionStatus.PENDING,
            true)

        //Then
        Assertions.assertEquals(response.status, ResponseStatus.FAILED)
    }
}