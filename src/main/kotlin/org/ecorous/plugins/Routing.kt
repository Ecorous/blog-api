package org.ecorous.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ecorous.Utils
import org.ecorous.Utils.ensureAuthenticated
import org.ecorous.types.CreatePost
import org.ecorous.types.Post
import kotlinx.html.*
import org.ecorous.authToken
import java.util.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(mapOf("message" to "Hello, world!"))
        }
        get("/posts") {
            // get all posts
            val posts = DB.getPosts()
            posts.sortedBy {
                it.createdAt
            }
            call.respond(mapOf("posts" to posts))
        }
        get("/posts/{id}") {
            // get a post
            val id = call.parameters["id"]?.let {
                Utils.uuidOrNull(it)
            } ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No valid ID provided. (type: uuid)"))
            val post = DB.getPosts().find {
                it.id == id
            } ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post not found."))
            call.respond(post)
        }
        post("/posts") {
            // create a post
            ensureAuthenticated()
            val form = call.receiveParameters()
            if (form["title"] != null && form["content"] != null) {
                val post = CreatePost(
                    title = form["title"]!!,
                    content = form["content"]!!
                )
                DB.createPost(post)
                call.respond(mapOf("message" to "Post created."))
                return@post
            }
            val post = call.receive<CreatePost>()
            DB.createPost(post)
            call.respond(mapOf("message" to "Post created."))
        }
        patch("/posts/{id}") {
            // update a post
            ensureAuthenticated()
            val id = call.parameters["id"]?.let {
                Utils.uuidOrNull(it)
            } ?: return@patch call.respond(HttpStatusCode.Companion.BadRequest, mapOf("error" to "No valid ID provided. (type: uuid)"))
            val post = call.receive<CreatePost>()
            DB.updatePost(id, post)
            call.respond(mapOf("message" to "Post updated."))
        }
        delete("/posts/{id}") {
            // delete a post
            ensureAuthenticated()
            val id: UUID = call.parameters["id"]?.let {
                Utils.uuidOrNull(it)
            } ?: return@delete call.respond(HttpStatusCode.Companion.BadRequest, mapOf("error" to "No valid ID provided. (type: uuid)"))
            DB.deletePost(id)
            call.respond(mapOf("message" to "Post deleted."))
        }
        get("/gui/create/post") {
            call.respondHtml {
                head {
                    title { +"Create Post" }
                }
                body {
                    form(action = "/posts", method = FormMethod.post) {
                        headers {
                            append(HttpHeaders.ContentType, ContentType.Application.Json)
                            append(HttpHeaders.Authorization, authToken)
                        }
                        textInput(name = "authToken") {
                            placeholder = "API Key"
                        }
                        br
                        textInput(name = "title") {
                            placeholder = "Title"
                        }
                        br
                        textArea {
                            name = "content"
                            placeholder = "Content"
                        }
                        br
                        submitInput { value = "Create Post" }
                    }
                }
            }
        }
        get("/feed.xml") {
            // create an RSS feed
            val posts = DB.getPosts().sortedBy { it.createdAt }
            // use stringbuilder
            val sb = StringBuilder()
            sb.appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            sb.appendLine("<rss version=\"2.0\">")
            sb.appendLine("<channel>")
            sb.appendLine("<title>Dawn's Blog</title>")
            sb.appendLine("<link>https://ecorous.org/blog</link>")
            sb.appendLine("<description>Dawn's Blog for various things and stuffs</description>")
            sb.appendLine("<language>en-gb</language>")
            posts.forEach {
                sb.appendLine("<item>")
                sb.appendLine("\t<title>${it.title}</title>")
                sb.appendLine("\t<link>https://ecorous.org/blog/posts/${it.id}</link>")
                sb.appendLine("\t<description>${it.content}</description>")
                sb.appendLine("\t<pubDate>${it.createdAt}</pubDate>")
                sb.appendLine("</item>")
            }
            sb.appendLine("</channel>")
            sb.appendLine("</rss>")
            headers {
                append(HttpHeaders.ContentType, ContentType.Application.Rss)
            }
            call.respondText(sb.toString())


        }
    }
}
