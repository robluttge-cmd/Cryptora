package com.cryptora.app

import com.cryptora.app.data.CustomWordStore
import com.cryptora.app.data.IndexedWordSource
import com.cryptora.app.data.SearchEngine
import org.junit.Assert.*
import org.junit.Test

class CustomWordStoreTest {
    private val disk = mutableMapOf<String, String>()
    private fun openStore() = CustomWordStore(read = { disk[it] }, write = { language, raw -> disk[language] = raw })

    @Test
    fun `added words persist after reopening and participate in searches`() {
        openStore().add("nl", "IJscafé, naiëf\nWoordenzoeker")
        val restored = openStore().words("nl")
        assertEquals(listOf("ijscafé", "naiëf", "woordenzoeker"), restored)
        assertEquals(listOf("ijscafé"), SearchEngine.search(
            SearchEngine.Query(pattern = "ijscafe", customWords = restored.joinToString("\n")),
            IndexedWordSource(emptyList())
        ).words)
    }

    @Test
    fun `case variants and composed accent duplicates are stored only once`() {
        val store = openStore()
        store.add("nl", "Café café cafe\u0301")
        store.add("nl", "CAFÉ, Café")
        assertEquals(listOf("café"), openStore().words("nl"))
    }

    @Test
    fun `removing a word persists and leaves other languages intact`() {
        val store = openStore()
        store.add("nl", "fiets café")
        store.add("fr", "café")
        assertEquals(listOf("fiets"), store.remove("nl", "café"))
        assertEquals(listOf("fiets"), openStore().words("nl"))
        assertEquals(listOf("café"), openStore().words("fr"))
        assertTrue(store.remove("nl", "fiets").isEmpty())
        assertTrue(openStore().words("nl").isEmpty())
    }

    @Test
    fun `empty and punctuation only input do not add empty words`() {
        assertTrue(openStore().add("nl", "  , \n -- ...").isEmpty())
    }
}
