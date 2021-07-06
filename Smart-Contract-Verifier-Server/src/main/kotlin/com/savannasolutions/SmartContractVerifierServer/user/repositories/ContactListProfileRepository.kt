package com.savannasolutions.SmartContractVerifierServer.user.repositories

import com.savannasolutions.SmartContractVerifierServer.user.models.ContactList
import com.savannasolutions.SmartContractVerifierServer.user.models.ContactListProfile
import com.savannasolutions.SmartContractVerifierServer.user.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ContactListProfileRepository: JpaRepository<ContactListProfile, UUID> {
    fun getAllByContactListAndUser(contactList: ContactList, user: User): List<ContactListProfile>?

    fun existByAliasAnAndContactListAndUser(contactList: ContactList, user: User, alias: String): Boolean
}