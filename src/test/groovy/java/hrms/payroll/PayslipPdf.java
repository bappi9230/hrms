package hrms.payroll;

import java.util.List;

/** Day 6: lays out one payslip on an A4 page using {@link SimplePdf}. Pure function of its inputs (easy to test). */
public final class PayslipPdf {

    private PayslipPdf() { }

    /**
     * @param company     heading
     * @param period      e.g. "October 2026"
     * @param statusNote  e.g. "DRAFT" (shown in red-ish text) or empty
     * @param left        employee info rows {label, value} (left column)
     * @param right       employee info rows (right column)
     * @param earnings    rows {label, amount-text}
     * @param deductions  rows {label, amount-text}
     * @param grossText   total earnings text
     * @param dedText     total deductions text
     * @param netText     net pay text
     * @param netWords    optional "In words" line (may be null)
     * @param notes       informational rows (employer PF, gratuity provision, payment method ...)
     */
    public static byte[] render(String company, String period, String statusNote,
                                List<String[]> left, List<String[]> right,
                                List<String[]> earnings, List<String[]> deductions,
                                String grossText, String dedText, String netText, String netWords,
                                List<String[]> notes) {
        SimplePdf p = new SimplePdf();
        final float L = 40f, R = SimplePdf.PAGE_W - 40f;
        float y = 50f;

        p.text(L, y, 18, true, company);
        p.textRight(R, y, 13, true, "PAYSLIP");
        y += 16;
        p.text(L, y, 10, false, "Pay period: " + period);
        if (statusNote != null && !statusNote.isEmpty()) p.textRight(R, y, 10, true, statusNote);
        y += 8;
        p.line(L, y, R, y, 1.5f);
        y += 18;

        // employee info (two columns)
        float yl = y, yr = y;
        for (String[] r : left) {
            p.text(L, yl, 10, true, r[0] + ":");
            p.text(L + 85, yl, 10, false, r[1]);
            yl += 15;
        }
        float mid = 315f;
        for (String[] r : right) {
            p.text(mid, yr, 10, true, r[0] + ":");
            p.text(mid + 95, yr, 10, false, r[1]);
            yr += 15;
        }
        y = Math.max(yl, yr) + 8;

        // earnings / deductions tables
        final float colW = (R - L - 20f) / 2f;
        final float xe = L, xd = L + colW + 20f;
        p.rect(xe, y - 11, colW, 18, 0.9f, true);
        p.rect(xd, y - 11, colW, 18, 0.9f, true);
        p.text(xe + 5, y + 2, 10, true, "EARNINGS");
        p.textRight(xe + colW - 5, y + 2, 10, true, "Amount (Tk)");
        p.text(xd + 5, y + 2, 10, true, "DEDUCTIONS");
        p.textRight(xd + colW - 5, y + 2, 10, true, "Amount (Tk)");
        y += 22;

        float ye = y, yd = y;
        for (String[] r : earnings) {
            p.text(xe + 5, ye, 9.5f, false, r[0]);
            p.textRight(xe + colW - 5, ye, 9.5f, false, r[1]);
            ye += 15;
        }
        for (String[] r : deductions) {
            p.text(xd + 5, yd, 9.5f, false, r[0]);
            p.textRight(xd + colW - 5, yd, 9.5f, false, r[1]);
            yd += 15;
        }
        y = Math.max(ye, yd) + 2;
        p.line(xe, y, xe + colW, y, 0.8f);
        p.line(xd, y, xd + colW, y, 0.8f);
        y += 14;
        p.text(xe + 5, y, 10, true, "Total earnings");
        p.textRight(xe + colW - 5, y, 10, true, grossText);
        p.text(xd + 5, y, 10, true, "Total deductions");
        p.textRight(xd + colW - 5, y, 10, true, dedText);
        y += 22;

        // net pay box
        p.rect(L, y - 14, R - L, 30, 0, false);
        p.text(L + 10, y + 5, 12, true, "NET PAY");
        p.textRight(R - 10, y + 5, 14, true, netText);
        y += 38;
        if (netWords != null && !netWords.isEmpty()) {
            p.text(L, y, 9, false, "In words: " + netWords);
            y += 18;
        }

        if (notes != null && !notes.isEmpty()) {
            p.text(L, y, 10, true, "Other information");
            y += 5;
            p.line(L, y, R, y, 0.5f);
            y += 14;
            for (String[] r : notes) {
                p.text(L + 5, y, 9, false, r[0]);
                p.textRight(R - 5, y, 9, false, r[1]);
                y += 13;
            }
        }

        p.text(L, 800, 8, false, "This is a computer-generated payslip and does not require a signature.");
        return p.toBytes();
    }
}
