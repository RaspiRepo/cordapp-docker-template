package com.template.webserver
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.corda.core.identity.CordaX500Name
import net.corda.core.contracts.ContractState
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.http.ResponseEntity

import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneId
import javax.servlet.http.HttpServletRequest
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.HttpStatus


val SERVICE_NAMES = listOf("Notary", "Network Map Service")


/**
 * Define your API endpoints here.
 */
@RestController
// @RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy
    private val myLegalName = rpc.proxy.nodeInfo().legalIdentities.first().name


    data class Response(val message: String, val status: String, val success: Boolean, val txId: String)

    data class Envelope (val name: String) {
        val sender: String = ""
        val receiver: String = ""
        val key: String = ""
    }

    data class Payload(val name: String) {
        var version: String? = ""
    }



    @GetMapping(value = ["/"], produces = ["text/plain"])
    private fun home(): String {
        return "welcome to CordaApp"
    }

    @GetMapping(value = ["/servertime"], produces = ["text/plain"])
    private fun serverTime() = LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC")).toString()

    @GetMapping(value = ["/addresses"], produces = arrayOf("text/plain"))
    private fun addresses() = proxy.nodeInfo().addresses.toString()

    @GetMapping(value = ["/identities"], produces = arrayOf("text/plain"))
    private fun identities() = proxy.nodeInfo().legalIdentities.toString()

    @GetMapping(value = ["/platformversion"], produces = arrayOf("text/plain"))
    private fun platformVersion() = proxy.nodeInfo().platformVersion.toString()

    @GetMapping(value = [ "peers" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })
    }

    @GetMapping(value = ["/notaries"], produces = arrayOf("text/plain"))
    private fun notaries() = proxy.notaryIdentities().toString()

    @GetMapping(value = ["/flows"], produces = arrayOf("text/plain"))
    private fun flows() = proxy.registeredFlows().toString()

    @GetMapping(value = ["/states"], produces = arrayOf("text/plain"))
    private fun states() = proxy.vaultQueryBy<ContractState>().states.toString()

    @GetMapping(value = [ "me" ], produces = [ APPLICATION_JSON_VALUE ])
    fun whoami() = mapOf("me" to myLegalName)


    @PostMapping(value = [ "create-clause" ], produces = [ APPLICATION_JSON_VALUE ], consumes = [ APPLICATION_JSON_VALUE ], headers = [ "Content-Type=application/json" ])
    fun createClause(@RequestBody payload: Payload): ResponseEntity<Response> {
        val gson = Gson();
        val payloadjson: String = gson.toJson(payload);
        logger.info("{}", payloadjson);

        val Response = Response("Successfully invoked the contract for transaction ID: {}}", "SUCCESS", true, "123");
        return ResponseEntity.status(HttpStatus.CREATED).body(Response);
    }

    @PostMapping(value = [ "create-iou" ], produces = [ APPLICATION_JSON_VALUE ], headers = ["Content-Type=application/json" ])
    fun createIOU(request: HttpServletRequest): ResponseEntity<Response> {
        logger.debug("{}", request);

        val Response = Response("Successfully invoked the contract for transaction ID: {}}", "SUCCESS", true, "123");
        return ResponseEntity.status(HttpStatus.CREATED).body(Response);
    }    
}