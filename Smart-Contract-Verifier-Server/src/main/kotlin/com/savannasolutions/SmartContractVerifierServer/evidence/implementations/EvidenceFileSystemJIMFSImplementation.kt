package com.savannasolutions.SmartContractVerifierServer.evidence.implementations

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.savannasolutions.SmartContractVerifierServer.evidence.interfaces.EvidenceFileSystem
import org.bouncycastle.util.encoders.UTF8
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.lang.Exception
import java.nio.file.FileSystem
import java.nio.file.Files
import javax.annotation.PostConstruct

@Component
class EvidenceFileSystemJIMFSImplementation: EvidenceFileSystem{

    var filesystem: FileSystem = Jimfs.newFileSystem(Configuration.windows())

    override fun saveFile(fileToSave: MultipartFile, filename: String) {
        val path = filesystem.getPath(filename)
        try {
            Files.createFile(path)
            Files.write(path, fileToSave.bytes)
        }catch (e: Exception){
            println(e.message)
        }
    }

    override fun retrieveFile(fileToRetrieve: String): File? {
        val path = filesystem.getPath(fileToRetrieve)
        try{
            return
        }catch (e: Exception){
            println(e.message)
        }
        return
    }

    override fun deleteFile(fileToDelete: String) {
        val path = filesystem.getPath(fileToDelete)
        try{
            Files.deleteIfExists(path)
        }catch (e: Exception){
            println(e.message)
        }
    }
}