package hrms

/** ছোট CSV পড়া/লেখার সহায়ক। Excel এ বাংলা ঠিক দেখাতে ফাইলের শুরুতে BOM দেওয়া হয়। */
class CsvUtil {

    static final String BOM = '\uFEFF'

    /** একটি লাইন তৈরি (শেষের নতুন লাইন ছাড়া) */
    static String line(List values) {
        values.collect { escape(it) }.join(',')
    }

    static String escape(Object value) {
        if (value == null) return ''
        String s = value.toString()
        // CSV injection ঠেকানো: Excel এ সূত্র হিসেবে চলতে পারে এমন শুরু হলে আগে ' বসানো
        // ('+8801...' এর মতো ফোন নম্বর বাদ)
        if (s ==~ /^[=@].*/ || (s ==~ /^[+\-].*/ && !(s ==~ /^[+\-]?[0-9 ()\-]+$/))) {
            s = "'" + s
        }
        if (s.contains(',') || s.contains('"') || s.contains('\n') || s.contains('\r')) {
            s = '"' + s.replace('"', '""') + '"'
        }
        s
    }

    /** পুরো টেক্সট পার্স করে সারির তালিকা বানায়; ফাঁকা লাইন বাদ যায় */
    static List<List<String>> parse(String text) {
        List<List<String>> rows = []
        if (!text) return rows
        if (text.startsWith(BOM)) text = text.substring(1)

        List<String> row = []
        StringBuilder cell = new StringBuilder()
        boolean inQuotes = false
        int n = text.length()

        for (int i = 0; i < n; i++) {
            char c = text.charAt(i)
            if (inQuotes) {
                if (c == (char) '"') {
                    if (i + 1 < n && text.charAt(i + 1) == (char) '"') {
                        cell.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    cell.append(c)
                }
            } else if (c == (char) '"') {
                inQuotes = true
            } else if (c == (char) ',') {
                row << cell.toString()
                cell.setLength(0)
            } else if (c == (char) '\n' || c == (char) '\r') {
                if (c == (char) '\r' && i + 1 < n && text.charAt(i + 1) == (char) '\n') i++
                row << cell.toString()
                cell.setLength(0)
                if (!isBlankRow(row)) rows << row
                row = []
            } else {
                cell.append(c)
            }
        }
        if (cell.length() > 0 || !row.isEmpty()) {
            row << cell.toString()
            if (!isBlankRow(row)) rows << row
        }
        rows
    }

    private static boolean isBlankRow(List<String> row) {
        row.every { !it?.trim() }
    }
}
