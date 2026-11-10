package ir.dakhlokharj.app

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

val MonthNames = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
)

// روزهای هفته بر اساس DayOfWeek.value (۱ = دوشنبه ... ۷ = یکشنبه)
val WeekDays = listOf("دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه")

fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
    val gDm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    val gy2 = if (gm > 2) gy + 1 else gy
    var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
            ((gy2 + 399) / 400) + gd + gDm[gm - 1]
    var jy = -1595 + 33 * (days / 12053)
    days %= 12053
    jy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) {
        jy += (days - 1) / 365
        days = (days - 1) % 365
    }
    val jm: Int
    val jd: Int
    if (days < 186) {
        jm = 1 + days / 31
        jd = 1 + days % 31
    } else {
        jm = 7 + (days - 186) / 30
        jd = 1 + (days - 186) % 30
    }
    return Triple(jy, jm, jd)
}

fun jalaliToGregorian(jyIn: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
    val jy = jyIn + 1595
    var days = -355668 + (365 * jy) + (jy / 33) * 8 + (((jy % 33) + 3) / 4) + jd +
            (if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30) + 186)
    var gy = 400 * (days / 146097)
    days %= 146097
    if (days > 36524) {
        days -= 1
        gy += 100 * (days / 36524)
        days %= 36524
        if (days >= 365) days++
    }
    gy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) {
        gy += (days - 1) / 365
        days = (days - 1) % 365
    }
    var gd = days + 1
    val leap = (gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0
    val salA = intArrayOf(0, 31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var gm = 0
    while (gm < 13) {
        val v = salA[gm]
        if (gd <= v) break
        gd -= v
        gm++
    }
    return Triple(gy, gm, gd)
}

fun millisToJalali(ms: Long): Triple<Int, Int, Int> {
    val d = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
    return gregorianToJalali(d.year, d.monthValue, d.dayOfMonth)
}

fun jalaliToMillis(jy: Int, jm: Int, jd: Int): Long {
    val g = jalaliToGregorian(jy, jm, jd)
    return LocalDate.of(g.first, g.second, g.third)
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun monthOf(ms: Long): Pair<Int, Int> = millisToJalali(ms).let { it.first to it.second }

fun toFa(s: String): String = s.map {
    when (it) {
        in '0'..'9' -> '۰' + (it - '0')
        ',' -> '٬'
        else -> it
    }
}.joinToString("")

fun toEn(s: String): String = s.map {
    when (it) {
        in '۰'..'۹' -> '0' + (it - '۰')
        in '٠'..'٩' -> '0' + (it - '٠')
        else -> it
    }
}.joinToString("")

fun Long.money(): String = toFa(String.format(Locale.US, "%,d", this))

fun fmtDate(ms: Long): String {
    val (y, m, d) = millisToJalali(ms)
    return toFa(String.format(Locale.US, "%04d/%02d/%02d", y, m, d))
}

fun parseDate(s: String): Long? {
    val p = toEn(s.trim()).split("/", "-")
    if (p.size != 3) return null
    val y = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    val d = p[2].toIntOrNull() ?: return null
    if (m !in 1..12 || d !in 1..31 || y !in 1300..1600) return null
    return try { jalaliToMillis(y, m, d) } catch (e: Exception) { null }
}

fun parseAmount(s: String): Long? = toEn(s).filter { it.isDigit() }.toLongOrNull()

fun todayText(): String {
    val now = System.currentTimeMillis()
    val (_, m, d) = millisToJalali(now)
    val wd = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).dayOfWeek.value
    return "${WeekDays[wd - 1]}، ${toFa(d.toString())} ${MonthNames[m - 1]}"
}
