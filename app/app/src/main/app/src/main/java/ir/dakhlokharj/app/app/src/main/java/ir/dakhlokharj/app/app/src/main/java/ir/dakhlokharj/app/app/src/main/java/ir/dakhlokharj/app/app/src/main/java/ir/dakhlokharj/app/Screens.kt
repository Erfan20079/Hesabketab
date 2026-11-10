@file:OptIn(ExperimentalMaterial3Api::class)

package ir.dakhlokharj.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Indigo = Color(0xFF3D35E0)
val Rose = Color(0xFFEF6C82)
val Mint = Color(0xFFC8F0B0)
val Soft = Color(0xFFF6F6FA)
val Green = Color(0xFF2E9E5B)
val DonutColors = listOf(Color(0xFF3D35E0), Color(0xFF8B5CF6), Color(0xFFB9B4F5), Color(0xFFEF6C82), Color(0xFFF5B942), Color(0xFF9CA3AF))
val ExpenseCats = listOf("خوراک", "حمل‌ونقل", "قبوض", "خرید", "تفریح", "سلامت", "سایر")
val IncomeCats = listOf("حقوق", "پروژه", "هدیه", "سایر")

// ---------- اجزای مشترک ----------

@Composable
fun Title(t: String) {
    Text(t, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 6.dp))
}

@Composable
fun CardBox(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) { Column(Modifier.padding(14.dp), content = content) }
}

@Composable
fun BoxScope.Fab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Indigo,
        contentColor = Color.White,
        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
    ) { Icon(Icons.Default.Add, contentDescription = "افزودن") }
}

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit, number: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (number) KeyboardType.Number else KeyboardType.Text),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun LineChart(a: List<Long>, b: List<Long>) {
    val max = ((a + b).maxOrNull() ?: 1L).coerceAtLeast(1L)
    Canvas(Modifier.fillMaxWidth().height(150.dp)) {
        fun path(d: List<Long>): Path {
            val p = Path()
            d.forEachIndexed { i, v ->
                // قدیمی‌ترین ماه سمت راست (راست‌به‌چپ)
                val x = if (d.size == 1) size.width else size.width - size.width * i / (d.size - 1)
                val y = size.height * 0.95f - (size.height * 0.9f * v / max)
                if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
            }
            return p
        }
        drawPath(path(a), Indigo, style = Stroke(width = 4.dp.toPx()))
        drawPath(path(b), Rose, style = Stroke(width = 4.dp.toPx()))
    }
}

@Composable
fun Donut(values: List<Long>) {
    val total = values.sum().coerceAtLeast(1L)
    Canvas(Modifier.size(110.dp)) {
        val stroke = 20.dp.toPx()
        var start = -90f
        values.forEachIndexed { i, v ->
            val sweep = 360f * v / total
            drawArc(
                color = DonutColors[i % DonutColors.size],
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(stroke)
            )
            start += sweep
        }
    }
}

// ---------- صفحه خانه ----------

@Composable
fun HomeScreen(vm: MainVM) {
    val txs by vm.txs.collectAsState()
    val inst by vm.installments.collectAsState()
    val goals by vm.goals.collectAsState()
    val now = System.currentTimeMillis()
    val (cy, cm, _) = millisToJalali(now)
    val months = remember(cy, cm) {
        (5 downTo 0).map { off ->
            var m = cm - off
            var y = cy
            while (m < 1) { m += 12; y-- }
            y to m
        }
    }
    val inc = months.map { ym -> txs.filter { !it.isExpense && monthOf(it.date) == ym }.sumOf { it.amount } }
    val exp = months.map { ym -> txs.filter { it.isExpense && monthOf(it.date) == ym }.sumOf { it.amount } }
    val thisInc = inc.last()
    val thisExp = exp.last()
    val dueNow = inst.filter { !it.paid && (monthOf(it.due) == (cy to cm) || it.due < now) }

    LazyColumn(
        Modifier.fillMaxSize().background(Soft),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("سلام!", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text(todayText(), color = Color.Gray)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CardBox(Modifier.weight(1f), Indigo) {
                    Text("درآمد این ماه", color = Color.White, fontSize = 12.sp)
                    Text(thisInc.money(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                CardBox(Modifier.weight(1f), Rose) {
                    Text("هزینه این ماه", color = Color.White, fontSize = 12.sp)
                    Text(thisExp.money(), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            CardBox {
                Text("مانده این ماه", color = Color.Gray, fontSize = 12.sp)
                Text("${(thisInc - thisExp).money()} تومان", fontWeight = FontWeight.Bold, fontSize = 20.sp,
                    color = if (thisInc >= thisExp) Green else Rose)
            }
        }
        item {
            CardBox {
                Title("درآمدها و هزینه‌ها")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("● درآمد", color = Indigo, fontSize = 12.sp)
                    Text("● هزینه", color = Rose, fontSize = 12.sp)
                }
                LineChart(inc, exp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    months.forEach { Text(MonthNames[it.second - 1], fontSize = 10.sp, color = Color.Gray) }
                }
            }
        }
        item { Title("بدهی‌های این ماه") }
        if (dueNow.isEmpty()) item { Text("بدهی یا قسط معوقی ندارید 🎉", color = Color.Gray) }
        items(dueNow, key = { it.id }) { i ->
            CardBox {
                Text(i.title, fontWeight = FontWeight.Bold)
                Text("${i.amount.money()} تومان • سررسید ${fmtDate(i.due)}", color = Color.Gray, fontSize = 12.sp)
            }
        }
        if (goals.isNotEmpty()) {
            item { Title("اهداف") }
            items(goals.take(3), key = { it.id }) { g ->
                CardBox(color = Mint) {
                    Text(g.title, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (g.saved.toFloat() / g.target.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(), color = Indigo
                    )
                    Text("${g.saved.money()} از ${g.target.money()}", fontSize = 12.sp)
                }
            }
        }
    }
}

// ---------- تراکنش‌ها ----------

@Composable
fun TxScreen(vm: MainVM) {
    val txs by vm.txs.collectAsState()
    var expense by remember { mutableStateOf(true) }
    var show by remember { mutableStateOf(false) }
    val list = txs.filter { it.isExpense == expense }
    val (cy, cm, _) = millisToJalali(System.currentTimeMillis())
    val byCat = list.filter { monthOf(it.date) == (cy to cm) }
        .groupBy { it.category }
        .map { e -> e.key to e.value.sumOf { it.amount } }
        .sortedByDescending { it.second }

    Box(Modifier.fillMaxSize().background(Soft)) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (expense) Button(onClick = {}, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Indigo)) { Text("هزینه‌ها") }
                    else OutlinedButton(onClick = { expense = true }, Modifier.weight(1f)) { Text("هزینه‌ها") }
                    if (!expense) Button(onClick = {}, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Indigo)) { Text("درآمدها") }
                    else OutlinedButton(onClick = { expense = false }, Modifier.weight(1f)) { Text("درآمدها") }
                }
            }
            if (byCat.isNotEmpty()) {
                item {
                    CardBox {
                        Title(if (expense) "هزینه‌های این ماه به تفکیک دسته" else "درآمدهای این ماه به تفکیک دسته")
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Donut(byCat.map { it.second })
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                byCat.forEachIndexed { i, (c, v) ->
                                    Text("● $c: ${v.money()}", color = DonutColors[i % DonutColors.size], fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            if (list.isEmpty()) item { Text("هنوز موردی ثبت نشده. با دکمه + اضافه کنید.", color = Color.Gray) }
            items(list, key = { it.id }) { t ->
                CardBox {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t.category, fontWeight = FontWeight.Bold)
                            Text(
                                (if (t.note.isNotBlank()) t.note + " • " else "") + fmtDate(t.date),
                                color = Color.Gray, fontSize = 12.sp
                            )
                        }
                        Text(
                            (if (t.isExpense) "−" else "+") + t.amount.money(),
                            color = if (t.isExpense) Rose else Green, fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { vm.deleteTx(t) }) { Icon(Icons.Default.Delete, contentDescription = "حذف") }
                    }
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
        Fab { show = true }
    }
    if (show) AddTxDialog(expense, onDismiss = { show = false }) { a, c, n, d ->
        vm.addTx(a, expense, c, n, d)
        show = false
    }
}

@Composable
fun AddTxDialog(expense: Boolean, onDismiss: () -> Unit, onSave: (Long, String, String, Long) -> Unit) {
    val cats = if (expense) ExpenseCats else IncomeCats
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(fmtDate(System.currentTimeMillis())) }
    var cat by remember { mutableStateOf(cats.first()) }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (expense) "افزودن هزینه" else "افزودن درآمد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Field("مبلغ (تومان)", amount, { amount = it }, number = true)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cats.forEach { c -> FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) }) }
                }
                Field("توضیح (اختیاری)", note, { note = it })
                Field("تاریخ شمسی (سال/ماه/روز)", date, { date = it })
                if (error) Text("مبلغ یا تاریخ معتبر نیست", color = Rose, fontSize = 12.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val a = parseAmount(amount)
                val d = parseDate(date)
                if (a != null && a > 0 && d != null) onSave(a, cat, note.trim(), d) else error = true
            }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

// ---------- اقساط ----------

@Composable
fun InstallmentsScreen(vm: MainVM) {
    val list by vm.installments.collectAsState()
    var show by remember { mutableStateOf(false) }
    val now = System.currentTimeMillis()
    Box(Modifier.fillMaxSize().background(Soft)) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Title("اقساط و بدهی‌ها") }
            if (list.isEmpty()) item { Text("هنوز قسطی ثبت نکرده‌اید.", color = Color.Gray) }
            items(list, key = { it.id }) { i ->
                val late = !i.paid && i.due < now
                val fg = if (late) Color.White else Color.Black
                CardBox(color = if (late) Rose else Color.White) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = i.paid, onCheckedChange = { vm.toggleInstallment(i) })
                        Column(Modifier.weight(1f)) {
                            Text(i.title, fontWeight = FontWeight.Bold, color = fg)
                            Text(
                                "سررسید ${fmtDate(i.due)} • ${i.amount.money()} تومان" + if (i.paid) " • پرداخت شد" else "",
                                fontSize = 12.sp, color = fg.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(onClick = { vm.deleteInstallment(i) }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = fg)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
        Fab { show = true }
    }
    if (show) {
        var title by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var date by remember { mutableStateOf(fmtDate(System.currentTimeMillis())) }
        var error by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { show = false },
            title = { Text("افزودن قسط / بدهی") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Field("عنوان (مثلاً قسط ماشین)", title, { title = it })
                    Field("مبلغ (تومان)", amount, { amount = it }, number = true)
                    Field("سررسید شمسی (سال/ماه/روز)", date, { date = it })
                    if (error) Text("اطلاعات واردشده معتبر نیست", color = Rose, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val a = parseAmount(amount)
                    val d = parseDate(date)
                    if (title.isNotBlank() && a != null && a > 0 && d != null) {
                        vm.addInstallment(title.trim(), a, d)
                        show = false
                    } else error = true
                }) { Text("ذخیره") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("انصراف") } }
        )
    }
}

// ---------- اهداف و پس‌انداز ----------

@Composable
fun GoalsScreen(vm: MainVM) {
    val goals by vm.goals.collectAsState()
    var show by remember { mutableStateOf(false) }
    var addTo by remember { mutableStateOf<Goal?>(null) }
    Box(Modifier.fillMaxSize().background(Soft)) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                CardBox(color = Indigo) {
                    Text("پس‌انداز کل", color = Color.White, fontSize = 12.sp)
                    Text("${goals.sumOf { it.saved }.money()} تومان", color = Color.White,
                        fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
            }
            item { Title("اهداف و پس‌اندازهای تو") }
            if (goals.isEmpty()) item { Text("هنوز هدفی تعریف نکرده‌اید.", color = Color.Gray) }
            items(goals, key = { it.id }) { g ->
                val done = g.saved >= g.target
                CardBox(color = if (done) Mint else Color.White) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(g.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { vm.deleteGoal(g) }) { Icon(Icons.Default.Delete, contentDescription = "حذف") }
                    }
                    LinearProgressIndicator(
                        progress = { (g.saved.toFloat() / g.target.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(), color = Indigo
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${g.saved.money()} از ${g.target.money()}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { addTo = g }) { Text("افزودن مبلغ") }
                    }
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
        Fab { show = true }
    }
    if (show) {
        var title by remember { mutableStateOf("") }
        var target by remember { mutableStateOf("") }
        var error by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { show = false },
            title = { Text("هدف جدید") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Field("عنوان (مثلاً خرید لپ‌تاپ)", title, { title = it })
                    Field("مبلغ هدف (تومان)", target, { target = it }, number = true)
                    if (error) Text("اطلاعات واردشده معتبر نیست", color = Rose, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val t = parseAmount(target)
                    if (title.isNotBlank() && t != null && t > 0) {
                        vm.addGoal(title.trim(), t)
                        show = false
                    } else error = true
                }) { Text("ذخیره") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("انصراف") } }
        )
    }
    addTo?.let { g ->
        var amount by remember(g.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { addTo = null },
            title = { Text("افزودن به «${g.title}»") },
            text = { Field("مبلغ (تومان)", amount, { amount = it }, number = true) },
            confirmButton = {
                TextButton(onClick = {
                    val a = parseAmount(amount)
                    if (a != null && a > 0) {
                        vm.addToGoal(g, a)
                        addTo = null
                    }
                }) { Text("ذخیره") }
            },
            dismissButton = { TextButton(onClick = { addTo = null }) { Text("انصراف") } }
        )
    }
}
