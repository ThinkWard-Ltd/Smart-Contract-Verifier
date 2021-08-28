package com.savannasolutions.SmartContractVerifierServer.IntegrationTests.APIEndPoints.negotiation

import com.savannasolutions.SmartContractVerifierServer.negotiation.models.Agreements
import com.savannasolutions.SmartContractVerifierServer.negotiation.models.ConditionStatus
import com.savannasolutions.SmartContractVerifierServer.negotiation.models.Conditions
import com.savannasolutions.SmartContractVerifierServer.negotiation.repositories.AgreementsRepository
import com.savannasolutions.SmartContractVerifierServer.negotiation.repositories.ConditionsRepository
import com.savannasolutions.SmartContractVerifierServer.user.models.User
import com.savannasolutions.SmartContractVerifierServer.user.repositories.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.util.*
import kotlin.collections.ArrayList
import kotlin.test.assertContains

@SpringBootTest
@AutoConfigureMockMvc
class SetDurationConditionTest {
    @Autowired
    lateinit var mockMvc : MockMvc

    @MockBean
    lateinit var agreementsRepository : AgreementsRepository

    @MockBean
    lateinit var userRepository: UserRepository

    @MockBean
    lateinit var conditionsRepository: ConditionsRepository

    private lateinit var userA : User
    private lateinit var userB : User
    private lateinit var userC : User
    private lateinit var agreement : Agreements
    private lateinit var conditionUUID : UUID

    @BeforeEach
    fun beforeEach()
    {

        userA = User("0x743Fb032c0bE976e1178d8157f911a9e825d9E23")
        userB = User("0x37Ec9a8aBFa094b24054422564e68B08aF3114B4")
        userC = User("0x69Ec9a8aBFa094b24054422564e68B08aF311400")


        agreement = Agreements(
            UUID.fromString("3c5657d6-e302-48d3-b9df-dcfccec97503"),
            "test agreement",
            "test description",
            CreatedDate = Date(),
            MovedToBlockChain = false)

        val condition = Conditions(
            UUID.fromString("967ee13c-dd5d-4de5-adb5-7dd4907fb2cf"),
            "Test conditions pending",
            "Test condition description",
            ConditionStatus.PENDING,
            Date()
        )
        conditionUUID = condition.conditionID

        val userList = ArrayList<User>()
        userList.add(userA)
        userList.add(userB)

        agreement.users.add(userA)
        agreement.users.add(userB)


        whenever(userRepository.existsById(userA.publicWalletID)).thenReturn(true)
        whenever(userRepository.existsById(userB.publicWalletID)).thenReturn(true)
        whenever(userRepository.getById(userA.publicWalletID)).thenReturn(userA)
        whenever(userRepository.getById(userB.publicWalletID)).thenReturn(userB)
        whenever(userRepository.getUsersByAgreementsContaining(agreement)).thenReturn(userList)
        whenever(agreementsRepository.existsById(agreement.ContractID)).thenReturn(true)
        whenever(agreementsRepository.getById(agreement.ContractID)).thenReturn(agreement)
        whenever(conditionsRepository.save(any<Conditions>())).thenReturn(condition)
    }

    private fun requestSender(rjson: String, userID: String, agreementID: UUID) : MockHttpServletResponse
    {
        return mockMvc.perform(
            MockMvcRequestBuilders.post("/user/${userID}/agreement/${agreementID}/condition/duration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rjson)).andReturn().response
    }

    @Test
    fun `SetDurationCondition successful`()
    {
        val rjson = "{\"Duration\" : 500.0}"

        val response = requestSender(rjson, userA.publicWalletID, agreement.ContractID)

        assertContains(response.contentAsString, "\"Status\":\"SUCCESSFUL\"")
        assertContains(response.contentAsString, conditionUUID.toString())
    }

    @Test
    fun `SetDurationCondition failed due to amount being below 0`()
    {
        val rjson = "{\"Duration\" : -500.0}"

        val response = requestSender(rjson,userA.publicWalletID, agreement.ContractID)

        assertContains(response.contentAsString, "\"Status\":\"FAILED\"")
    }

    @Test
    fun `SetDurationCondition failed agreement does not exist`()
    {
        val rjson = "{\"Duration\" : 500.0}"

        val response = requestSender(rjson, userA.publicWalletID, UUID.fromString("eb558bea-389e-4e7b-afed-4987dbf37f85"))

        assertContains(response.contentAsString, "\"Status\":\"FAILED\"")
    }

    @Test
    fun `SetDurationCondition proposing user not part of agreement`()
    {
        val rjson = "{\"Duration\" : 500.0}"

        val response = requestSender(rjson, userC.publicWalletID, agreement.ContractID)

        assertContains(response.contentAsString, "\"Status\":\"FAILED\"")
    }


}