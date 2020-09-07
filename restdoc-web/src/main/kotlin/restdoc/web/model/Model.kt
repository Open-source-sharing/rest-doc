package restdoc.web.model

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.HashIndexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.http.HttpMethod

@Document(collection = "restdoc_project_config")
data class ProjectConfig(
        @Id var id: String?,
        val projectId: String,
        val testURIPrefix: String
)

data class Resource(@Id var id: String?,
                    var tag: String?,
                    var name: String?,
        // 父ID
                    var pid: String?,
                    @HashIndexed
                    var projectId: String?,
                    var createTime: Long?,
                    var createBy: String?
)

@Document(collection = "restdoc_project")
data class Project(
        @Id val id: String,
        val name: String,
        val desc: String?,
        val createTime: Long?,
        val accessPwd: String? = null
)

@Document(collection = "restdoc_group")
data class Group(
        @Id val id: String,
        val name: String,
        val desc: String,
        val createTime: Long
)

/**
 * @sample HttpHeaders
 */
@Document(collection = "restdoc_document")
data class Document(
        @Id var id: String?,
        var projectId: String?,
        var name: String?,
        var resource: String,
        val url: String,
        val description: String? = null,
        var requestHeaderDescriptor: List<HeaderFieldDescriptor>?,
        var requestBodyDescriptor: List<BodyFieldDescriptor>?,
        var responseBodyDescriptors: List<BodyFieldDescriptor>?,
        val method: HttpMethod = HttpMethod.GET,
        val uriVarDescriptors: List<URIVarDescriptor>?,
        val executeResult: Map<String, Any?>? = null,
        val content: String? = null,
        var responseHeaderDescriptor: List<HeaderFieldDescriptor>? = null,
        val docType: DocType = DocType.API
)


@Document(collection = "restdoc_menu")
data class Menu(
        @Id val id: Int,
        val title: String,
        val type: Int,
        val openType: String? = "_iframe",
        val icon: String,
        val href: String = "",
        val children: MutableList<Menu>? = null
)

@Document(collection = "restdoc_team")
data class Team(
        @Id val id: String,
        var name: String,
        var createTime: Long,
        var createBy: String,
        var owner: String,
        var cover: String
)

@Document(collection = "restdoc_user")
data class User(
        @Id val id: String,
        var name: String?,
        var createTime: Long?,
        var status: AccountStatus = AccountStatus.NORMAL,
        var teamId: String
)

@Deprecated(message = "")
data class ExecuteResult(
        var status: Int, var url: String,
        var method: String,
        var requestHeader: Map<String, Any>,
        var responseHeader: Map<String, Any>,

        // request body
        var content: JsonNode?,

        // response body
        var body: JsonNode?,

        //
        var uriVariable: Map<String, String>? = null
)