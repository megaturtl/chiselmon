package cc.turtl.chiselmon.client.feature.pc

/**
 * Per world PC User Data.
 */
class PCUserData {
    val bookmarks = Bookmarks()

    class Bookmarks {
        companion object {
            const val MAX_BOOKMARKS = 5
        }

        private val data = LinkedHashSet<Int>()

        fun toggle(boxNo: Int): Boolean =
            data.remove(boxNo) || (data.size < MAX_BOOKMARKS && data.add(boxNo))

        fun remove(boxNo: Int) {
            data.remove(boxNo)
        }

        fun clear() {
            data.clear()
        }

        fun isFull(): Boolean = data.size >= MAX_BOOKMARKS
        fun has(boxNo: Int): Boolean = boxNo in data
        fun get(): List<Int> = data.toList()
    }
}