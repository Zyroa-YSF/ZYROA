package com.skingold.market

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

/**
 * Tüm veritabanı işlemleri tek bir iş parçacığında sırayla çalışır (arayüz donmaz,
 * çift dokunuş gibi durumlarda işlemler birbirine karışmaz).
 */
class GameVM(app: Application) : AndroidViewModel(app) {
    private val db = GameDb(app)
    private val worker = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    var snap by mutableStateOf<Snap?>(null)
        private set
    var toast by mutableStateOf<String?>(null)

    init {
        viewModelScope.launch(worker) {
            db.ensureWorld()
            val s = db.snapshot()
            withContext(Dispatchers.Main) { snap = s }
        }
    }

    fun act(block: GameDb.() -> OpResult, done: (OpResult) -> Unit = {}) {
        viewModelScope.launch(worker) {
            val r = db.block()
            val s = db.snapshot()
            withContext(Dispatchers.Main) {
                snap = s
                if (r.message.isNotEmpty()) toast = r.message
                done(r)
            }
        }
    }

    fun open(crateId: String, done: (OpResult) -> Unit) = act({ openCrate(crateId) }, done)
    fun sell(uid: Long, done: (OpResult) -> Unit = {}) = act({ sell(uid) }, done)
    fun buy(id: Long) = act({ buyListing(id) })
    fun visitMarket() = act({ visitMarket() })
    fun trade(offerId: Long, uids: List<Long>, done: (OpResult) -> Unit) = act({ trade(offerId, uids) }, done)
    fun decline(offerId: Long, done: (OpResult) -> Unit) = act({ declineOffer(offerId) }, done)
    fun claimDaily() = act({ claimDaily() })
    fun claimQuest(id: String) = act({ claimQuest(id) })
    fun claimAch(id: String) = act({ claimAch(id) })
    fun pin(uid: Long, p: Boolean) = act({ setPinned(uid, p) })
    fun seen(uid: Long) = act({ markSeen(uid) })
    fun rename(n: String) = act({ rename(n) })
    fun setAnim(on: Boolean) = act({ setAnim(on) })
    fun reset(done: (OpResult) -> Unit) = act({ resetAll().also { ensureWorld() } }, done)

    override fun onCleared() {
        worker.close()
        db.close()
    }
}
