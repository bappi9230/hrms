package hrms

import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Value

/**
 * Day 6: চূড়ান্ত পে-রোল থেকে পেমেন্ট শিট - ব্যাংক অ্যাডভাইস, bKash/Nagad ওয়ালেট শিট, নগদের তালিকা।
 * শুধু নেট বেতন > 0 লাইন ধরা হয়। পেমেন্ট তথ্য (PaymentInfo) অসম্পূর্ণ বা নেই এমন কর্মী "missing" তালিকায় যায়।
 */
@Transactional
class BankSheetService {

    @Value('${hrms.company.name:HRMS Demo Ltd.}')
    String companyName

    @Value('${hrms.company.bankName:}')
    String companyBank

    @Value('${hrms.company.accountNo:}')
    String companyAccount

    Map build(PayrollRun run) {
        Map<Long, PaymentInfo> infoByEmp = [:]
        PaymentInfo.list().each { PaymentInfo p -> infoByEmp[p.employee.id] = p }

        List<Map> bank = [], wallet = [], cash = [], missing = []
        PayrollLine.findAllByPayrollRun(run, [sort: 'employeeName']).each { PayrollLine l ->
            if (l.netPay <= 0) return
            PaymentInfo p = infoByEmp[l.employee.id]
            Map row = [line: l, name: l.employeeName, amount: l.netPay, info: p]
            if (p == null || !p.complete) { missing << row; return }
            if (p.method == 'BANK') bank << row
            else if (p.method == 'BKASH' || p.method == 'NAGAD') wallet << row
            else cash << row
        }
        [bank: bank, wallet: wallet, cash: cash, missing: missing,
         bankTotal: sum(bank), walletTotal: sum(wallet), cashTotal: sum(cash), missingTotal: sum(missing)]
    }

    private static BigDecimal sum(List<Map> rows) {
        rows.sum(0.0) { Map r -> r.amount as BigDecimal } as BigDecimal
    }

    static final List<String> BANK_HEADER = ['SL', 'Account name', 'Account no', 'Bank', 'Branch', 'Routing no', 'Amount (BDT)', 'Narration']
    static final List<String> WALLET_HEADER = ['SL', 'Wallet', 'Wallet no', 'Employee', 'Amount (BDT)', 'Reference']

    List<List> bankRows(PayrollRun run, Map sheet) {
        int n = 0
        (sheet.bank as List<Map>).collect { Map r ->
            PaymentInfo p = r.info as PaymentInfo
            [++n, p.accountName ?: r.name, p.accountNo, p.bankName, p.bankBranch, p.routingNo, r.amount, "Salary ${run}".toString()] as List
        }
    }

    List<List> walletRows(PayrollRun run, Map sheet) {
        int n = 0
        (sheet.wallet as List<Map>).collect { Map r ->
            PaymentInfo p = r.info as PaymentInfo
            [++n, p.method == 'BKASH' ? 'bKash' : 'Nagad', p.walletNo, r.name, r.amount, "Salary ${run}".toString()] as List
        }
    }
}
