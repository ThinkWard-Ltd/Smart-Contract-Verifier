package com.savannasolutions.SmartContractVerifierServer.negotiation.services

import com.savannasolutions.SmartContractVerifierServer.common.*
import com.savannasolutions.SmartContractVerifierServer.contracts.repositories.JudgesRepository
import com.savannasolutions.SmartContractVerifierServer.negotiation.models.Agreements
import com.savannasolutions.SmartContractVerifierServer.negotiation.models.ConditionStatus
import com.savannasolutions.SmartContractVerifierServer.negotiation.models.Conditions
import com.savannasolutions.SmartContractVerifierServer.negotiation.repositories.AgreementsRepository
import com.savannasolutions.SmartContractVerifierServer.negotiation.repositories.ConditionsRepository
import com.savannasolutions.SmartContractVerifierServer.negotiation.requests.*
import com.savannasolutions.SmartContractVerifierServer.negotiation.responses.*
import com.savannasolutions.SmartContractVerifierServer.user.repositories.UserRepository
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.ArrayList


@Service
class NegotiationService constructor(val agreementsRepository: AgreementsRepository,
                                     val conditionsRepository: ConditionsRepository,
                                     val userRepository: UserRepository,
                                     val judgesRepository: JudgesRepository){

    fun acceptCondition(userID: String, agreementID: UUID, conditionID: UUID): AcceptConditionResponse{
        if(!conditionsRepository.existsById(conditionID))
            return AcceptConditionResponse(ResponseStatus.FAILED)

        if(!agreementsRepository.existsById(agreementID))
            return AcceptConditionResponse(ResponseStatus.FAILED)

        val condition = conditionsRepository.getById(conditionID)
            if(condition.conditionStatus == ConditionStatus.PENDING)
            {
                condition.conditionStatus = ConditionStatus.ACCEPTED
                conditionsRepository.save(condition)
                return AcceptConditionResponse(ResponseStatus.SUCCESSFUL)
            }
        return AcceptConditionResponse(ResponseStatus.FAILED)
    }

    fun createAgreement(userID: String, createAgreementRequest: CreateAgreementRequest): CreateAgreementResponse{
        if(createAgreementRequest.PartyB.isEmpty())
            return CreateAgreementResponse(status = ResponseStatus.FAILED)

        if(createAgreementRequest.Title.isEmpty())
            return CreateAgreementResponse(status = ResponseStatus.FAILED)

        if(createAgreementRequest.Description.isEmpty())
            return CreateAgreementResponse(status = ResponseStatus.FAILED)

        if(createAgreementRequest.PartyB == userID)
            return CreateAgreementResponse(status = ResponseStatus.FAILED)

        if(!userRepository.existsById(userID))
            return CreateAgreementResponse(status = ResponseStatus.FAILED)

        if(!userRepository.existsById(createAgreementRequest.PartyB))
            return CreateAgreementResponse(status = ResponseStatus.FAILED)

        val userA = userRepository.getById(userID)
        val userB = userRepository.getById(createAgreementRequest.PartyB)

        var nAgreement = Agreements(UUID.randomUUID(),
                                    AgreementTitle = createAgreementRequest.Title,
                                    AgreementDescription = createAgreementRequest.Description,
                                    CreatedDate = Date(),
                                    MovedToBlockChain = false,
                                    AgreementImageURL = createAgreementRequest.ImageURL
                                    )
        nAgreement = nAgreement.apply { users.add(userA)}
        nAgreement = nAgreement.apply { users.add(userB)}

        val tempA = agreementsRepository.getAllByUsersContaining(userA) ?:
        mutableSetOf()

        val tempB = agreementsRepository.getAllByUsersContaining(userB) ?:
        mutableSetOf()

        nAgreement = agreementsRepository.save(nAgreement)

        userA.agreements = tempA
        userB.agreements = tempB

        userA.agreements.add(nAgreement)
        userB.agreements.add(nAgreement)
        userRepository.save(userA)
        userRepository.save(userB)

        return CreateAgreementResponse(nAgreement.ContractID, ResponseStatus.SUCCESSFUL)
    }

    fun createCondition(userID: String, agreementID: UUID, createConditionRequest: CreateConditionRequest): CreateConditionResponse{
        if(!agreementsRepository.existsById(agreementID))
            return CreateConditionResponse(status = ResponseStatus.FAILED)

        if(createConditionRequest.ConditionDescription.isEmpty())
            return CreateConditionResponse(status = ResponseStatus.FAILED)

        if(createConditionRequest.Title.isEmpty())
            return CreateConditionResponse(status = ResponseStatus.FAILED)

        if(!userRepository.existsById(userID))
            return CreateConditionResponse(status = ResponseStatus.FAILED)

        val agreement = agreementsRepository.getById(agreementID)

        val users = userRepository.getUsersByAgreementsContaining(agreement)

        if(users.elementAt(0).publicWalletID != userID && users.elementAt(1).publicWalletID != userID)
            return CreateConditionResponse(status = ResponseStatus.FAILED)

        val user = userRepository.getById(userID)

        var nCondition = Conditions(UUID.randomUUID(),
                                    createConditionRequest.Title,
                                    createConditionRequest.ConditionDescription,
                                    ConditionStatus.PENDING,
                                    Date(),).apply { contract = agreement }
        nCondition = nCondition.apply { proposingUser = user }

        nCondition = conditionsRepository.save(nCondition)

        return CreateConditionResponse(nCondition.conditionID, ResponseStatus.SUCCESSFUL)
    }

    fun getAgreementDetails(userID: String, AgreementID: UUID): GetAgreementDetailsResponse{
        if(!agreementsRepository.existsById(AgreementID))
            return GetAgreementDetailsResponse(status = ResponseStatus.FAILED)
        if(!userRepository.existsById(userID))
            return GetAgreementDetailsResponse(status = ResponseStatus.FAILED)

        val agreement = agreementsRepository.getById(AgreementID)
        val conditionList = conditionsRepository.getAllByContract(agreement)
        val conditions = ArrayList<ConditionResponse>()
        if(conditionList != null)
        {
            for(cond in conditionList)
            {
                val tempCond = ConditionResponse(cond.conditionID,
                    cond.conditionDescription,
                    UserResponse(cond.proposingUser.publicWalletID),
                    cond.proposalDate,
                    agreement.ContractID,
                    cond.conditionStatus,
                    cond.conditionTitle)
                conditions.add(tempCond)
            }
        }

        val userList = userRepository.getUsersByAgreementsContaining(agreement)
        val partyA = UserResponse(userList[0].publicWalletID)
        val partyB = UserResponse(userList[1].publicWalletID)

        if(userID != partyA.PublicWalletID && userID != partyB.PublicWalletID)
            return GetAgreementDetailsResponse(status = ResponseStatus.FAILED)

        val paymentCondition : Conditions? = if(agreement.PaymentConditionUUID != null)
            conditionsRepository.getById(agreement.PaymentConditionUUID!!)
        else null

        val durationCondition : Conditions? = if(agreement.DurationConditionUUID != null)
            conditionsRepository.getById(agreement.DurationConditionUUID!!)
        else null

        val paymentConditionResponse : PaymentConditionResponse?
        if(paymentCondition != null)
        {
            var amountStr = paymentCondition.conditionDescription
            amountStr = amountStr.replace("Payment of ", "")
            val amount = amountStr.toDouble()
            paymentConditionResponse = PaymentConditionResponse(paymentCondition.conditionID,
                                                                amount,
                                                                agreement.PayingParty!!,
                                                                paymentCondition.conditionStatus)
        } else
            paymentConditionResponse = null

        val durationConditionResponse : DurationConditionResponse?
        if(durationCondition != null)
        {
            var amountStr = durationCondition.conditionDescription
            amountStr = amountStr.replace("Duration of ", "")
            val amount  = amountStr.toDouble()
            durationConditionResponse = DurationConditionResponse(durationCondition.conditionID,
                                                                  amount,
                                                                  durationCondition.conditionStatus)
        } else
            durationConditionResponse = null

        val agreementResponse = AgreementResponse(agreement.ContractID,
                                                    agreement.AgreementTitle,
                                                    agreement.AgreementDescription,
                                                    durationConditionResponse,
                                                    paymentConditionResponse,
                                                    partyA,
                                                    partyB,
                                                    agreement.CreatedDate,
                                                    agreement.SealedDate,
                                                    agreement.MovedToBlockChain,
                                                    conditions,
                                                    agreement.AgreementImageURL,
                                                    agreement.blockchainID)

        return GetAgreementDetailsResponse(agreementResponse,ResponseStatus.SUCCESSFUL)
    }

    fun rejectCondition(userID: String, agreementID: UUID, conditionID: UUID): RejectConditionResponse {
        if(!conditionsRepository.existsById(conditionID))
            return RejectConditionResponse(ResponseStatus.FAILED)

        if(!agreementsRepository.existsById(agreementID))
            return RejectConditionResponse(ResponseStatus.FAILED)

        val condition = conditionsRepository.getById(conditionID)

        if(condition.contract.ContractID != agreementID)
            return RejectConditionResponse(ResponseStatus.FAILED)

        val agreement = agreementsRepository.getById(agreementID)
        val userList = userRepository.getUsersByAgreementsContaining(agreement)

        var found = false
        for(user in userList)
            if(user.publicWalletID == userID)
                found = true

        if(!found)
            return RejectConditionResponse(ResponseStatus.FAILED)

        if(condition.conditionStatus == ConditionStatus.PENDING) {
            condition.conditionStatus = ConditionStatus.REJECTED
            conditionsRepository.save(condition)
            return RejectConditionResponse(ResponseStatus.SUCCESSFUL)
        }
        return RejectConditionResponse(ResponseStatus.FAILED)
    }

    fun getAllConditions(userID:String, agreementID: UUID):GetAllConditionsResponse{
        if(!agreementsRepository.existsById(agreementID))
            return GetAllConditionsResponse(status = ResponseStatus.FAILED)

        val agreement = agreementsRepository.getById(agreementID)
        val userList = userRepository.getUsersByAgreementsContaining(agreement)

        var found = false
        for(user in userList)
            if(user.publicWalletID == userID)
                found = true;

        if(!found)
            return GetAllConditionsResponse(status = ResponseStatus.FAILED)

        val conditions = conditionsRepository.getAllByContract(agreementsRepository.getById(agreementID))
        conditions?:return GetAllConditionsResponse(emptyList(),ResponseStatus.SUCCESSFUL)

        val conditionList = ArrayList<ConditionResponse>()
        for(cond in conditions)
        {
            conditionList.add(
                ConditionResponse(cond.conditionID,
                    cond.conditionDescription,
                    UserResponse(cond.proposingUser.publicWalletID),
                    cond.proposalDate,
                    cond.contract.ContractID,
                    cond.conditionStatus,
                    cond.conditionTitle
                )
            )
        }
        return GetAllConditionsResponse(conditionList, ResponseStatus.SUCCESSFUL)
    }

    fun sealAgreement(sealAgreementRequest: SealAgreementRequest): SealAgreementResponse{
        if(!agreementsRepository.existsById(sealAgreementRequest.AgreementID))
        {
            return SealAgreementResponse(ResponseStatus.FAILED)
        }
        val agreement = agreementsRepository.getById(sealAgreementRequest.AgreementID)
        val condList = conditionsRepository.getAllByContract(agreement)
        if (condList != null) {
            for (cond in condList) {
                if(cond.conditionStatus == ConditionStatus.PENDING)
                    return SealAgreementResponse(ResponseStatus.FAILED)
            }
        } else
            return SealAgreementResponse(ResponseStatus.FAILED)

        agreement.DurationConditionUUID?: return SealAgreementResponse(ResponseStatus.FAILED)
        if(conditionsRepository.getById(agreement.DurationConditionUUID!!).conditionStatus == ConditionStatus.PENDING ||
                conditionsRepository.getById(agreement.DurationConditionUUID!!).conditionStatus == ConditionStatus.REJECTED)
                    return SealAgreementResponse(ResponseStatus.FAILED)

        agreement.PaymentConditionUUID?: return SealAgreementResponse(ResponseStatus.FAILED)
        if(conditionsRepository.getById(agreement.PaymentConditionUUID!!).conditionStatus == ConditionStatus.PENDING ||
                conditionsRepository.getById(agreement.PaymentConditionUUID!!).conditionStatus == ConditionStatus.REJECTED)
                    return SealAgreementResponse(ResponseStatus.FAILED)

        agreement.SealedDate = Date()
        agreement.blockchainID = sealAgreementRequest.index
        agreement.MovedToBlockChain = true
        agreementsRepository.save(agreement)
        return SealAgreementResponse(ResponseStatus.SUCCESSFUL)
    }

    fun getConditionDetails(getConditionDetailsRequest: GetConditionDetailsRequest): GetConditionDetailsResponse
    {
        if(!conditionsRepository.existsById(getConditionDetailsRequest.conditionID))
            return GetConditionDetailsResponse(status = ResponseStatus.FAILED)

        val condition = conditionsRepository.getById(getConditionDetailsRequest.conditionID)
        return GetConditionDetailsResponse(ConditionResponse(condition.conditionID,
                                            condition.conditionDescription,
                                            UserResponse(condition.proposingUser.publicWalletID),
                                            condition.proposalDate,
                                            condition.contract.ContractID,
                                            condition.conditionStatus,
                                            condition.conditionTitle),
                                            ResponseStatus.SUCCESSFUL)
    }

    fun setPaymentCondition(setPaymentConditionRequest: SetPaymentConditionRequest): SetPaymentConditionResponse
    {
        if(setPaymentConditionRequest.PreposedUser.isEmpty())
            return SetPaymentConditionResponse(null, status = ResponseStatus.FAILED)

        if(setPaymentConditionRequest.PayingUser.isEmpty())
            return SetPaymentConditionResponse(null, status = ResponseStatus.FAILED)

        if(!agreementsRepository.existsById(setPaymentConditionRequest.AgreementID))
            return SetPaymentConditionResponse(null, ResponseStatus.FAILED)

        if(setPaymentConditionRequest.Payment < 0)
            return SetPaymentConditionResponse(null, ResponseStatus.FAILED)

        val agreement = agreementsRepository.getById(setPaymentConditionRequest.AgreementID)
        val userList = userRepository.getUsersByAgreementsContaining(agreement)

        if(setPaymentConditionRequest.PreposedUser != userList.elementAt(0).publicWalletID
            && setPaymentConditionRequest.PreposedUser != userList.elementAt(1).publicWalletID)
            return SetPaymentConditionResponse(null, ResponseStatus.FAILED)

        if(setPaymentConditionRequest.PayingUser != userList.elementAt(0).publicWalletID
            && setPaymentConditionRequest.PayingUser != userList.elementAt(1).publicWalletID)
            return SetPaymentConditionResponse(null, ResponseStatus.FAILED)

        val user = userRepository.getById(setPaymentConditionRequest.PreposedUser)

        var condition = Conditions(UUID.randomUUID(),
                     "Payment condition",
                "Payment of " + setPaymentConditionRequest.Payment.toString(),
                                ConditionStatus.PENDING,
                                Date(),).apply { contract = agreement }
        condition = condition.apply { proposingUser = user }

        condition = conditionsRepository.save(condition)
        if(agreement.PaymentConditionUUID != null)
        {
            val prevPaymentCondition = conditionsRepository.getById(agreement.PaymentConditionUUID!!)
            prevPaymentCondition.conditionStatus = ConditionStatus.REJECTED
            conditionsRepository.save(prevPaymentCondition)
        }

        agreement.PaymentConditionUUID = condition.conditionID
        agreement.PayingParty = setPaymentConditionRequest.PayingUser

        agreementsRepository.save(agreement)

        return SetPaymentConditionResponse(condition.conditionID, ResponseStatus.SUCCESSFUL)
    }

    fun setDurationCondition(setDurationConditionRequest: SetDurationConditionRequest): SetDurationConditionResponse
    {
        if(!agreementsRepository.existsById(setDurationConditionRequest.AgreementID))
            return SetDurationConditionResponse(status = ResponseStatus.FAILED)

        if(setDurationConditionRequest.Duration.isNegative || setDurationConditionRequest.Duration.isZero)
            return SetDurationConditionResponse(status = ResponseStatus.FAILED)

        if(!userRepository.existsById(setDurationConditionRequest.PreposedUser))
            return SetDurationConditionResponse(status = ResponseStatus.FAILED)

        val agreement = agreementsRepository.getById(setDurationConditionRequest.AgreementID)
        val userList = userRepository.getUsersByAgreementsContaining(agreement)

        if(userList.elementAt(0).publicWalletID != setDurationConditionRequest.PreposedUser &&
            userList.elementAt(1).publicWalletID != setDurationConditionRequest.PreposedUser)
            return SetDurationConditionResponse(status = ResponseStatus.FAILED)

        val user = userRepository.getById(setDurationConditionRequest.PreposedUser)

        var condition = Conditions(UUID.randomUUID(),
                "Duration condition",
                "Duration of " + setDurationConditionRequest.Duration.toSeconds().toString(),
                ConditionStatus.PENDING,
                Date(),).apply { contract = agreement }

        condition = condition.apply { proposingUser = user}

        condition = conditionsRepository.save(condition)

        if(agreement.DurationConditionUUID != null)
        {
            val prevDurationCondition = conditionsRepository.getById(agreement.DurationConditionUUID!!)
            prevDurationCondition.conditionStatus = ConditionStatus.REJECTED
            conditionsRepository.save(prevDurationCondition)
        }

        agreement.DurationConditionUUID = condition.conditionID

        agreementsRepository.save(agreement)

        return SetDurationConditionResponse(condition.conditionID, ResponseStatus.SUCCESSFUL)
    }

    fun getJudgingAgreements(getJudgingAgreementsRequest: GetJudgingAgreementsRequest): GetJudgingAgreementsResponse
    {
        if(!userRepository.existsById(getJudgingAgreementsRequest.walletID))
            return GetJudgingAgreementsResponse(status = ResponseStatus.FAILED)

        val user = userRepository.getById(getJudgingAgreementsRequest.walletID)
        val judgeList = judgesRepository.getAllByJudge(user) ?:
            return GetJudgingAgreementsResponse(emptyList(), ResponseStatus.SUCCESSFUL)

        val agreementsResponseList = ArrayList<AgreementResponse>()

        for(judge in judgeList)
        {
            agreementsResponseList.add(generateAgreementResponse(judge.agreement))
        }

        return GetJudgingAgreementsResponse(agreementsResponseList, ResponseStatus.SUCCESSFUL)
    }

    private fun generateAgreementResponse(agreement: Agreements): AgreementResponse {
        val conditionList = conditionsRepository.getAllByContract(agreement)
        val conditions = ArrayList<ConditionResponse>()
        if (conditionList != null) {
            for (cond in conditionList) {
                val tempCond = ConditionResponse(
                    cond.conditionID,
                    cond.conditionDescription,
                    UserResponse(cond.proposingUser.publicWalletID),
                    cond.proposalDate,
                    agreement.ContractID,
                    cond.conditionStatus,
                    cond.conditionTitle
                )
                conditions.add(tempCond)
            }
        }

        val userList = userRepository.getUsersByAgreementsContaining(agreement)
        val partyA = UserResponse(userList[0].publicWalletID)
        val partyB = UserResponse(userList[1].publicWalletID)
        val paymentCondition : Conditions? = if(agreement.PaymentConditionUUID != null)
            conditionsRepository.getById(agreement.PaymentConditionUUID!!)
        else null

        val durationCondition : Conditions? = if(agreement.DurationConditionUUID != null)
            conditionsRepository.getById(agreement.DurationConditionUUID!!)
        else null

        val paymentConditionResponse : PaymentConditionResponse?
        if(paymentCondition != null)
        {
            var amountStr = paymentCondition.conditionDescription
            amountStr = amountStr.replace("Payment of ", "")
            val amount = amountStr.toDouble()
            paymentConditionResponse = PaymentConditionResponse(paymentCondition.conditionID,
                amount,
                agreement.PayingParty!!,
                paymentCondition.conditionStatus)
        } else
            paymentConditionResponse = null

        val durationConditionResponse : DurationConditionResponse?
        if(durationCondition != null)
        {
            var amountStr = durationCondition.conditionDescription
            amountStr = amountStr.replace("Duration of ", "")
            val amount  = amountStr.toDouble()
            durationConditionResponse = DurationConditionResponse(durationCondition.conditionID,
                amount,
                durationCondition.conditionStatus)
        } else
            durationConditionResponse = null

        return AgreementResponse(
            agreement.ContractID,
            agreement.AgreementTitle,
            agreement.AgreementDescription,
            durationConditionResponse,
            paymentConditionResponse,
            partyA,
            partyB,
            agreement.CreatedDate,
            agreement.SealedDate,
            agreement.MovedToBlockChain,
            conditions,
            agreement.AgreementImageURL,
            agreement.blockchainID
        )
    }

}