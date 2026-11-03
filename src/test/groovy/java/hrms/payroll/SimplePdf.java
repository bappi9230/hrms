package hrms.payroll;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Day 6: tiny dependency-free PDF writer (A4, one or more pages, Helvetica / Helvetica-Bold, lines and filled rectangles).
 * Text is limited to WinAnsi (Cp1252): other characters (e.g. Bengali) are written as '?'.
 */
public final class SimplePdf {

    public static final float PAGE_W = 595f;
    public static final float PAGE_H = 842f;

    private static final Charset CP1252 = Charset.forName("windows-1252");

    // Helvetica advance widths for ASCII 32..126 (per 1000 em)
    private static final int[] W = {
            278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
            1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
            667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
            333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
            556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584
    };

    private final List<ByteArrayOutputStream> pages = new ArrayList<>();
    private ByteArrayOutputStream cur;

    public SimplePdf() { newPage(); }

    public void newPage() {
        cur = new ByteArrayOutputStream();
        pages.add(cur);
    }

    /** y is measured from the TOP of the page (more natural for layouts) */
    public void text(float x, float y, float size, boolean bold, String s) {
        raw("BT /" + (bold ? "F2" : "F1") + " " + n(size) + " Tf " + n(x) + " " + n(PAGE_H - y) + " Td (");
        rawBytes(escape(s));
        raw(") Tj ET\n");
    }

    public void textRight(float xRight, float y, float size, boolean bold, String s) {
        text(xRight - width(s, size), y, size, bold, s);
    }

    public void line(float x1, float y1, float x2, float y2, float widthPt) {
        raw(n(widthPt) + " w 0 G " + n(x1) + " " + n(PAGE_H - y1) + " m " + n(x2) + " " + n(PAGE_H - y2) + " l S\n");
    }

    public void rect(float x, float y, float w, float h, float gray, boolean fill) {
        if (fill) {
            raw(n(gray) + " g " + n(x) + " " + n(PAGE_H - y - h) + " " + n(w) + " " + n(h) + " re f 0 g\n");
        } else {
            raw("0.8 w 0 G " + n(x) + " " + n(PAGE_H - y - h) + " " + n(w) + " " + n(h) + " re S\n");
        }
    }

    public static float width(String s, float size) {
        float total = 0;
        for (char c : clean(s).toCharArray()) {
            total += (c >= 32 && c <= 126) ? W[c - 32] : 556;
        }
        return total * size / 1000f;
    }

    public byte[] toBytes() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<Integer> offsets = new ArrayList<>();
            write(out, "%PDF-1.4\n%âãÏÓ\n");

            int pageCount = pages.size();
            // objects: 1 catalog, 2 pages, 3 F1, 4 F2, then per page: page obj, content obj
            offsets.add(out.size()); write(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
            StringBuilder kids = new StringBuilder();
            for (int i = 0; i < pageCount; i++) kids.append(5 + i * 2).append(" 0 R ");
            offsets.add(out.size()); write(out, "2 0 obj\n<< /Type /Pages /Kids [" + kids + "] /Count " + pageCount + " >>\nendobj\n");
            offsets.add(out.size()); write(out, "3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n");
            offsets.add(out.size()); write(out, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\nendobj\n");

            for (int i = 0; i < pageCount; i++) {
                int pageObj = 5 + i * 2;
                int contentObj = pageObj + 1;
                offsets.add(out.size());
                write(out, pageObj + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                        + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + contentObj + " 0 R >>\nendobj\n");
                byte[] content = pages.get(i).toByteArray();
                offsets.add(out.size());
                write(out, contentObj + " 0 obj\n<< /Length " + content.length + " >>\nstream\n");
                out.write(content, 0, content.length);
                write(out, "\nendstream\nendobj\n");
            }

            int xref = out.size();
            int count = offsets.size() + 1;
            write(out, "xref\n0 " + count + "\n0000000000 65535 f \n");
            for (int off : offsets) write(out, String.format("%010d 00000 n \n", off));
            write(out, "trailer\n<< /Size " + count + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("PDF build failed", e);
        }
    }

    // ------------------------------------------------------------------
    private static String clean(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\n' || c == '\r' || c == '\t') sb.append(' ');
            else if (c == '৳') sb.append("Tk");              // Bengali taka sign
            else if (c >= 32 && c <= 126) sb.append(c);
            else if (c >= 160 && c <= 255) sb.append(c);
            else sb.append('?');
        }
        return sb.toString();
    }

    private static byte[] escape(String s) {
        String c = clean(s).replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
        return c.getBytes(CP1252);
    }

    private static String n(float v) {
        return String.format(java.util.Locale.ROOT, "%.2f", v);
    }

    private void raw(String s) { rawBytes(s.getBytes(CP1252)); }

    private void rawBytes(byte[] b) { cur.write(b, 0, b.length); }

    private static void write(ByteArrayOutputStream out, String s) throws java.io.IOException {
        out.write(s.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
    }
}
