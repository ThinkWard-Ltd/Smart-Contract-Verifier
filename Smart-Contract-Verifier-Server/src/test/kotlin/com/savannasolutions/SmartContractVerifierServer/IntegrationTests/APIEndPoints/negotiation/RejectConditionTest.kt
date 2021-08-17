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
class RejectConditionTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var agreementsRepository: AgreementsRepository

    @MockBean
    lateinit var userRepository: UserRepository

    @MockBean
    lateinit var conditionsRepository: ConditionsRepository

    private lateinit var userA: User
    private lateinit var userB: User
    private lateinit var acceptedConditionID: UUID
    private lateinit var rejectedConditionID: UUID
    private lateinit var pendingConditionID: UUID

    @BeforeEach
    fun beforeEach() {

        userA = User("0x743Fb032c0bE976e1178d8157f911a9e825d9E23")
        userB = User("0x37Ec9a8aBFa094b24054422564e68B08aF3114B4")

        val agreement = Agreements(
            UUID.fromString("3c5657d6-e302-48d3-b9df-dcfccec97503"),
            "test agreement",
            "test description",
            CreatedDate = Date(),
            MovedToBlockChain = false
        )

        val pendingCondition = Conditions(UUID.fromString("967ee13c-dd5d-4de5-adb5-7dd4907fb2cf"),
            "Test conditions pending",
            "Test condition description",
            ConditionStatus.PENDING,
            Date())
        pendingConditionID = pendingCondition.conditionID

        val acceptedCondition = Conditions(UUID.fromString("d20088ad-6e94-426f-84f8-bf01b9e9bf6e"),
            "Test condition accepted",
            "Test condition description",
            ConditionStatus.ACCEPTED,
            Date())
        acceptedConditionID = acceptedCondition.conditionID

        val rejectedCondition = Conditions(UUID.fromString("e2526862-6282-4fc9-9ea7-f6dbc6064c48"),
            "Test condition rejected",
            "Test condition description",
            ConditionStatus.REJECTED,
            Date())
        rejectedConditionID = rejectedCondition.conditionID

        val conditionList = ArrayList<Conditions>()
        conditionList.add(pendingCondition)
        conditionList.add(acceptedCondition)
        conditionList.add(rejectedCondition)

        whenever(userRepository.existsById(userA.publicWalletID)).thenReturn(true)
        whenever(userRepository.existsById(userB.publicWalletID)).thenReturn(true)
        whenever(userRepository.getById(userA.publicWalletID)).thenReturn(userA)
        whenever(userRepository.getById(userB.publicWalletID)).thenReturn(userB)
        whenever(agreementsRepository.save(any<Agreements>())).thenReturn(agreement)
        whenever(conditionsRepository.getAllByContract(agreement)).thenReturn(conditionList)
        for(condition in conditionList)
        {
            whenever(conditionsRepository.existsById(condition.conditionID)).thenReturn(true)
            whenever(conditionsRepository.getById(condition.conditionID)).thenReturn(condition)
        }

    }

    private fun requestSender(rjson: String): MockHttpServletResponse {
        return mockMvc.perform(
            MockMvcRequestBuilders.post("/negotiation/reject-condition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rjson)
        ).andReturn().response
    }

    @Test
    fun `RejectCondition successful`()
    {
        val rjson = "{\"ConditionID\" : \"$pendingConditionID\"}"

        val response = requestSender(rjson)

        assertContains(response.contentAsString, "\"Status\":\"SUCCESSFUL\"")
    }

    @Test
    fun `RejectCondition failed due to condition not existing`()
    {
        val rjson = "{\"ConditionID\" : \"980ccce0-59aa-4246-96ef-b55501f150ec\"}"

        val response = requestSender(rjson)

        assertContains(response.contentAsString, "\"Status\":\"FAILED\"")
    }

    @Test
    fun `RejectCondition failed due to condition being already rejected`()
    {
        val rjson = "{\"ConditionID\" : \"$rejectedConditionID\"}"

        val response = requestSender(rjson)

        assertContains(response.contentAsString, "\"Status\":\"FAILED\"")
    }

    @Test
    fun `RejectCondition failed due to condition being already accepted`()
    {
        val rjson = "{\"ConditionID\" : \"$acceptedConditionID\"}"

        val response = requestSender(rjson)

        assertContains(response.contentAsString, "\"Status\":\"FAILED\"")
    }

}