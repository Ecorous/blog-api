package org.ecorous.plugins

import org.ecorous.Tables
import org.ecorous.Utils
import org.ecorous.types.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object DB {
    var db: Database? = null
    fun setupDatabase() {
        db = Database.connect(
            url = "jdbc:postgresql://localhost:5432/ecorousblog",
            user = "postgres",
            driver = "org.postgresql.Driver",
            password = "example"
        )
        transaction(db) {
            SchemaUtils.create(Tables.Posts)
        }
    }

    fun getPosts(): List<Post> {
        return transaction(db) {
            Tables.Posts.selectAll().map {
                Post(
                    id = it[Tables.Posts.id],
                    title = it[Tables.Posts.title],
                    content = it[Tables.Posts.content],
                    createdAt = it[Tables.Posts.createdAt],
                    updatedAt = it[Tables.Posts.updatedAt]
                )
            }
        }
    }

    fun createPost(post: CreatePost) {
        transaction(db) {
            Tables.Posts.insert {
                it[id] = UUID.randomUUID()
                it[title] = post.title
                it[content] = post.content
                it[createdAt] = Utils.currentTime()
                it[updatedAt] = Utils.currentTime()
            }
        }
    }

    fun updatePost(id: UUID, post: CreatePost) {
        transaction(db) {
            Tables.Posts.update({ Tables.Posts.id eq id }) {
                it[title] = post.title
                it[content] = post.content
                it[updatedAt] = Utils.currentTime()
            }
        }
    }

    fun deletePost(id: UUID) {
        transaction(db) {
            Tables.Posts.deleteWhere { Tables.Posts.id eq id }
        }
    }
}