package hua.lee.herbmind.test

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import hua.lee.herbmind.data.model.Herb
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 用于单元测试的内存版 SqlDriver
 *
 * HerbRepository 是 final class 无法继承覆写，因此改为拦截底层 SQL：
 * 根据 SQL 文本从内存药材列表返回行，配合真实生成的 HerbQueries 构造仓库。
 * 仅支持 herb 表的查询，其他语句返回空结果。
 */
class FakeSqlDriver(
    private val herbs: List<Herb> = emptyList()
) : SqlDriver {

    private val json = Json { ignoreUnknownKeys = true }

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<R> {
        val binds = mutableMapOf<Int, Any?>()
        val statement = object : SqlPreparedStatement {
            override fun bindBytes(index: Int, bytes: ByteArray?) { binds[index] = bytes }
            override fun bindLong(index: Int, long: Long?) { binds[index] = long }
            override fun bindDouble(index: Int, double: Double?) { binds[index] = double }
            override fun bindString(index: Int, string: String?) { binds[index] = string }
            override fun bindBoolean(index: Int, boolean: Boolean?) { binds[index] = boolean }
        }
        binders?.let { statement.apply(it) }

        val rows = when {
            "FROM herb" in sql -> queryHerbs(sql, binds)
            else -> emptyList()
        }
        return mapper(FakeSqlCursor(rows))
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<Long> = QueryResult.Value(0)

    override fun newTransaction(): QueryResult<Transacter.Transaction> =
        QueryResult.Value(object : Transacter.Transaction() {
            override val enclosingTransaction: Transacter.Transaction? get() = null
            override fun endTransaction(successful: Boolean): QueryResult<Unit> =
                QueryResult.Value(Unit)
        })

    override fun currentTransaction(): Transacter.Transaction? = null

    override fun addListener(vararg queryKeys: String, listener: app.cash.sqldelight.Query.Listener) = Unit

    override fun removeListener(vararg queryKeys: String, listener: app.cash.sqldelight.Query.Listener) = Unit

    override fun notifyListeners(vararg queryKeys: String) = Unit

    override fun close() = Unit

    private fun queryHerbs(sql: String, binds: Map<Int, Any?>): List<List<String?>> {
        var rows = herbs

        if ("WHERE id = ?" in sql) {
            val id = binds[0] as? String
            rows = rows.filter { it.id == id }
        }
        if ("WHERE category = ?" in sql) {
            val category = binds[0] as? String
            rows = rows.filter { it.category == category }
        }

        if ("LIMIT" in sql) {
            val categoryBound = "WHERE category = ?" in sql
            val limit = ((if (categoryBound) binds[1] else binds[0]) as? Long ?: Long.MAX_VALUE).toInt()
            val offset = ((if (categoryBound) binds[2] else binds[1]) as? Long ?: 0L).toInt()
            rows = rows.drop(offset).take(limit)
        }

        return rows.map { it.toRow() }
    }

    /** 列顺序与 herb 表定义（SELECT *）一致 */
    private fun Herb.toRow(): List<String?> = listOf(
        id,
        name,
        pinyin,
        latinName.ifEmpty { null },
        json.encodeToString(aliases),
        category,
        nature.ifEmpty { null },
        json.encodeToString(flavor),
        json.encodeToString(meridians),
        json.encodeToString(effects),
        json.encodeToString(indications),
        origin.ifEmpty { null },
        traits.ifEmpty { null },
        quality.ifEmpty { null },
        json.encodeToString(images),
        sourceUrl.ifEmpty { null },
        json.encodeToString(relatedFormulas)
    )

    private class FakeSqlCursor(
        private val rows: List<List<String?>>
    ) : SqlCursor {
        private var index = -1

        override fun next(): QueryResult<Boolean> = QueryResult.Value(++index < rows.size)

        override fun getString(index: Int): String? = rows.getOrNull(this.index)?.getOrNull(index)

        override fun getLong(index: Int): Long? = null

        override fun getBytes(index: Int): ByteArray? = null

        override fun getDouble(index: Int): Double? = null

        override fun getBoolean(index: Int): Boolean? = null
    }
}
