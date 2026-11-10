package ir.dakhlokharj.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainVM(app: Application) : AndroidViewModel(app) {
    private val dao = AppDb.get(app).dao()

    val txs = dao.txs().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val installments = dao.installments().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val goals = dao.goals().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addTx(amount: Long, expense: Boolean, cat: String, note: String, date: Long) {
        viewModelScope.launch {
            dao.insertTx(Tx(amount = amount, isExpense = expense, category = cat, note = note, date = date))
        }
    }
    fun deleteTx(t: Tx) { viewModelScope.launch { dao.deleteTx(t) } }

    fun addInstallment(title: String, amount: Long, due: Long) {
        viewModelScope.launch { dao.insertInstallment(Installment(title = title, amount = amount, due = due)) }
    }
    fun toggleInstallment(i: Installment) { viewModelScope.launch { dao.updateInstallment(i.copy(paid = !i.paid)) } }
    fun deleteInstallment(i: Installment) { viewModelScope.launch { dao.deleteInstallment(i) } }

    fun addGoal(title: String, target: Long) {
        viewModelScope.launch { dao.insertGoal(Goal(title = title, target = target)) }
    }
    fun addToGoal(g: Goal, amount: Long) { viewModelScope.launch { dao.updateGoal(g.copy(saved = g.saved + amount)) } }
    fun deleteGoal(g: Goal) { viewModelScope.launch { dao.deleteGoal(g) } }
}
