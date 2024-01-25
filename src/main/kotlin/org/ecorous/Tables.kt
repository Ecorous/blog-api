package org.ecorous

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Tables {
    object Posts : Table() {
        val id = uuid("id")
        val title = varchar("title", 255)
        val content = text("content")
        val createdAt = datetime("created_at")
        val updatedAt = datetime("updated_at")
        override val primaryKey = PrimaryKey(id)
    }
}