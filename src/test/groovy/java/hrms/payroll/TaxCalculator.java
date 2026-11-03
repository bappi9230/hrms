package hrms.payroll;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Day 6: Bangladesh individual income tax (salary) - simplified monthly TDS.
 *
 * Rates follow the FY2026-27 budget proposal (tax-free 3,75,000; women 4,25,000; slabs 10/15/20/25/30%).
 * !! Verify against the final Finance Act / NBR notice before real use. All numbers are constants below. !!
 *
 * Simplifications (on purpose, "keep rules simple first"):
 *  - annual salary = regular monthly gross x 12 (bonus, OT, one-off items are not taxed here)
 *  - salary exemption = min(1/3 of annual salary, 4,50,000)
 *  - no investment rebate, no special categories except women's threshold
 *  - minimum tax 5,000 applies when any tax is due (city corporation rate)
 */
public final class TaxCalculator {

    private TaxCalculator() { }

    public static final BigDecimal THRESHOLD_GENERAL = new BigDecimal("375000");
    public static final BigDecimal THRESHOLD_FEMALE  = new BigDecimal("425000");
    public static final BigDecimal EXEMPTION_CAP     = new BigDecimal("450000");
    public static final BigDecimal MINIMUM_TAX       = new BigDecimal("5000");

    /** {slab width, percent}; income beyond the last slab is taxed at TOP_RATE */
    private static final long[][] SLABS = {
            {300000L, 10}, {400000L, 15}, {500000L, 20}, {2000000L, 25}
    };
    private static final int TOP_RATE = 30;

    /** annual tax (whole taka) on an annual salary */
    public static BigDecimal annualTax(BigDecimal annualSalary, boolean female) {
        if (annualSalary == null || annualSalary.signum() <= 0) return BigDecimal.ZERO;
        BigDecimal exemption = annualSalary.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP).min(EXEMPTION_CAP);
        BigDecimal taxable = annualSalary.subtract(exemption);
        BigDecimal threshold = female ? THRESHOLD_FEMALE : THRESHOLD_GENERAL;
        BigDecimal remaining = taxable.subtract(threshold);
        if (remaining.signum() <= 0) return BigDecimal.ZERO;

        BigDecimal tax = BigDecimal.ZERO;
        for (long[] slab : SLABS) {
            BigDecimal part = remaining.min(BigDecimal.valueOf(slab[0]));
            tax = tax.add(part.multiply(BigDecimal.valueOf(slab[1])).divide(BigDecimal.valueOf(100)));
            remaining = remaining.subtract(part);
            if (remaining.signum() <= 0) break;
        }
        if (remaining.signum() > 0) {
            tax = tax.add(remaining.multiply(BigDecimal.valueOf(TOP_RATE)).divide(BigDecimal.valueOf(100)));
        }
        if (tax.compareTo(MINIMUM_TAX) < 0) tax = MINIMUM_TAX;
        return tax.setScale(0, RoundingMode.HALF_UP);
    }

    /** monthly TDS (whole taka) for a regular monthly gross */
    public static BigDecimal monthlyTds(BigDecimal monthlyRegularGross, boolean female) {
        if (monthlyRegularGross == null) return BigDecimal.ZERO;
        BigDecimal annual = monthlyRegularGross.multiply(BigDecimal.valueOf(12));
        return annualTax(annual, female).divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP);
    }
}
