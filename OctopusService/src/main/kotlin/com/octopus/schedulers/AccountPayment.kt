package com.octopus.schedulers

import com.octopus.UNIT_OF_WORK_IN_CENTS
import com.octopus.core.enums.JobType
import com.octopus.core.enums.PaymentStatus
import com.octopus.core.models.HostSubJobsExecuted
import com.octopus.dao.HostDao
import com.octopus.dao.SubJobDao
import com.octopus.hostDaoKey
import com.octopus.subjobDaoKey
import org.knowm.sundial.Job
import org.knowm.sundial.SundialJobScheduler
import org.knowm.sundial.annotations.SimpleTrigger
import org.knowm.sundial.exceptions.JobInterruptException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

typealias AccountId = Int
typealias HostId = Int

class AccountHostsPayable {
    private val accountsPaymentMap = mutableMapOf<AccountId, MutableMap<HostId, MutableSet<HostSubJobsExecuted>>>()
    fun addHostExecution(hostSubJobsExecuted: HostSubJobsExecuted) {
        if (accountsPaymentMap.containsKey(hostSubJobsExecuted.accountId)) {
            val hostsMap = accountsPaymentMap[hostSubJobsExecuted.accountId]!!
            hostsMap.getOrDefault(hostSubJobsExecuted.hostId, mutableSetOf()).add(hostSubJobsExecuted)
        } else {
            accountsPaymentMap[hostSubJobsExecuted.accountId] = mutableMapOf(hostSubJobsExecuted.hostId to mutableSetOf(hostSubJobsExecuted))
        }
    }

    fun numberOfAccounts(): Int {
        return accountsPaymentMap.size
    }
    fun getAccountPaymentMap(): Map<Int, Map<Int, Set<HostSubJobsExecuted>>> {
        return accountsPaymentMap
    }
}

@SimpleTrigger(repeatInterval = 15, timeUnit = TimeUnit.SECONDS) // TODO: update to DAYS once we want to release to prod/staging
class AccountPayment : Job() {
    private val logger: Logger = LoggerFactory.getLogger(AccountPayment::class.java)
    private val accountHostsPayable = AccountHostsPayable()
    @Throws(JobInterruptException::class)
    override fun doRun() {
        val hostDao = SundialJobScheduler.getServletContext().getAttribute(hostDaoKey) as HostDao
        val subJobDao = SundialJobScheduler.getServletContext().getAttribute(subjobDaoKey) as SubJobDao
        val hostSubJobExecuted = hostDao.getHostSubJobExecutedWithPaymentStatus(PaymentStatus.NOT_PAID)
        if (hostSubJobExecuted.isEmpty()) {
            logger.info("No hosts require payments")
        } else {
            logger.info("${hostSubJobExecuted.size} payment/s to be made")
            processPayments(hostSubJobExecuted, subJobDao)
        }
    }

    private fun processPayments(hostsSubJobsExecuted: List<HostSubJobsExecuted>, subJobDao: SubJobDao) {
        for (hostSubJobExecuted in hostsSubJobsExecuted) {
            logger.info("processing payment for account: ${hostSubJobExecuted.accountId}")
            subJobDao.updateSubJobPaymentStatus(hostSubJobExecuted.subJobId, PaymentStatus.PROCESSING)
            accountHostsPayable.addHostExecution(hostSubJobExecuted)
        }
        makePayment(subJobDao)
    }

    private fun makePayment(subJobDao: SubJobDao) {
        logger.info("Paying out ${accountHostsPayable.numberOfAccounts()} account/s")
        for ((accountId, hostWithSubJobsExecuted) in accountHostsPayable.getAccountPaymentMap()) {
            val calculatedWeights = calculateWeightsForSubJobs(hostWithSubJobsExecuted)
            val priceToPay = calculatePriceToPay(calculatedWeights)
            if (sendPayment(accountId, priceToPay)) {
                for ((_, executedSubJobs) in hostWithSubJobsExecuted) {
                    for (executedSubJob in executedSubJobs) {
                        subJobDao.updateSubJobPaymentStatus(executedSubJob.subJobId, PaymentStatus.PAID)
                    }
                }
            }
        }
    }

    private fun sendPayment(accountId: Int, priceToPay: Int): Boolean {
        logger.info("Sending Payment request for account: $accountId of amount $priceToPay cents")
        // TODO: add payment logic here
        logger.info("payment sent to account: $accountId of amount $priceToPay cents")
        return true
    }

    private fun calculateWeightsForSubJobs(hostSubJobsExecutedMap: Map<Int, Set<HostSubJobsExecuted>>): Int {
        var totalWeight = 0
        for ((hostId, hostSubJobsExecuted) in hostSubJobsExecutedMap) {
            logger.info("calculating subjobs executed weights for host $hostId")
            val hostPaymentMap = createPaymentMapSubJobsExecuted(hostSubJobsExecuted)
            for (jobType in JobType.values()) {
                val jobWeight = hostPaymentMap.getOrDefault(jobType, 0) * JobType.weight(jobType)
                totalWeight += jobWeight
            }
        }
        return totalWeight
    }

    /*
        takes a list of HostSubJobExecuted and returns a map of the JobType to Count of specific JobType
     */
    private fun createPaymentMapSubJobsExecuted(hostSubJobsExecuted: Set<HostSubJobsExecuted>): Map<JobType, Int> {
        val paymentMap = mutableMapOf<JobType, Int>()
        for (hostSubJobExecuted in hostSubJobsExecuted) {
            paymentMap[hostSubJobExecuted.jobType] = paymentMap.getOrDefault(hostSubJobExecuted.jobType, 0) + 1
        }
        return paymentMap
    }

    /*
        returns the amount in cents to be paid given the weight of the subjobs processed
     */
    private fun calculatePriceToPay(calculatedWeight: Int): Int {
        return calculatedWeight * UNIT_OF_WORK_IN_CENTS
    }
}
