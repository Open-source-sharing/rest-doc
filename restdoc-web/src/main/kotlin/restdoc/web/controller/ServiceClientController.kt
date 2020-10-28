package restdoc.web.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.*
import restdoc.remoting.common.ApplicationType
import restdoc.web.base.auth.Verify
import restdoc.web.controller.obj.*
import restdoc.web.core.Status
import restdoc.web.core.ok
import restdoc.web.core.schedule.ClientAPIMemoryUnit
import restdoc.web.core.schedule.ClientChannelManager
import restdoc.web.core.schedule.ScheduleController
import restdoc.web.model.*
import restdoc.web.repository.ProjectRepository
import restdoc.web.repository.ResourceRepository
import restdoc.web.repository.RestWebDocumentRepository
import restdoc.web.util.IDUtil
import restdoc.web.util.IDUtil.id
import restdoc.web.util.IDUtil.now
import restdoc.web.util.MD5Util
import sun.security.provider.MD5


@RestController
@Verify
class ServiceClientController {

    @Autowired
    lateinit var clientChannelManager: ClientChannelManager

    @Autowired
    lateinit var scheduleController: ScheduleController

    @Autowired
    lateinit var resourceRepository: ResourceRepository

    @Autowired
    lateinit var restWebDocumentRepository: RestWebDocumentRepository

    @Autowired
    lateinit var clientAPIMemoryUnit: ClientAPIMemoryUnit

    @Autowired
    lateinit var projectRepository: ProjectRepository

    @GetMapping("/serviceClient/list")
    fun list(@RequestParam(defaultValue = "DUBBO") type: ApplicationType): Any {

        val services = clientChannelManager.clients
                .filter { it.value.applicationType == type }
                .map {
                    mapOf(
                            "id" to it.value.id,
                            "remoteAddress" to it.value.clientId,
                            "hostname" to it.value.hostname,
                            "osname" to it.value.osname,
                            "service" to it.value.service,
                            "applicationType" to it.value.applicationType,
                            "serializationProtocol" to it.value.serializationProtocol
                    )
                }
                .toList()

        val res = mutableMapOf<String, Any>()
        res["code"] = 0
        res["count"] = clientChannelManager.clients.size
        res["msg"] = ""
        res["data"] = services

        return res
    }

    /*  @PostMapping("/serviceClient/apiEmptyTemplate/sync")
      fun syncClientApiEmptyTemplateToExistProject(
              @RequestBody dto: SyncApiEmptyTemplateDto
      ): Any {

          val project = Project(
                  id = IDUtil.id(),
                  name = dto.name!!,
                  desc = dto.name,
                  createTime = now(),
                  accessPwd = null)

          val resourceKeyDocumentMap = syncApiEmptyTemplates(dto.remoteAddress, project.id)

          resourceRepository.saveAll(resourceKeyDocumentMap.keys)
          restWebDocumentRepository.saveAll(resourceKeyDocumentMap.values.flatten())

          projectRepository.save(project)

          return ok(project.id)
      }*/

    @PostMapping("/{projectId}/serviceClient/apiEmptyTemplate/sync")
    fun syncClientApiEmptyTemplate(@RequestBody dto: SyncApiEmptyTemplateDto): Any {

        val resourceKeyDocumentMap = syncApiEmptyTemplates(dto.remoteAddress, dto.projectId!!)

        resourceRepository.saveAll(resourceKeyDocumentMap.keys)
        restWebDocumentRepository.saveAll(resourceKeyDocumentMap.values.flatten())

        return ok(dto.projectId)
    }

    private fun syncApiEmptyTemplates(clientId: String, projectId: String): Map<Resource, List<RestWebDocument>> {

        // Invoke remote client api info
        val emptyApiTemplates = scheduleController.syncGetEmptyApiTemplates(clientId)

        val pid = "root"

        return emptyApiTemplates
                // Group by resource
                .groupBy { it.controller }
                .map {

                    // Map key to resource
                    val resource = Resource(
                            id = IDUtil.id(),
                            tag = it.key,
                            name = it.key,
                            pid = pid,
                            projectId = projectId,
                            createTime = now(),
                            createBy = "Default"
                    )

                    // Map value to document
                    val documents = it.value.map { template ->

                        val uriVarDescriptors = template.uriVarFields.map { uriField ->
                            URIVarDescriptor(uriField, "", null)
                        }

                        RestWebDocument(id = IDUtil.id(),
                                projectId = projectId,
                                name = template.function,
                                resource = resource.id!!,
                                url = template.pattern,
                                description = String.format("%s:%s", template.controller, template.function),
                                requestHeaderDescriptor = mutableListOf(),
                                requestBodyDescriptor = mutableListOf(),
                                responseBodyDescriptors = mutableListOf(),
                                method = method(template.supportMethod.map { HttpMethod.valueOf(it) }),
                                uriVarDescriptors = uriVarDescriptors
                        )
                    }
                    resource to documents
                }
                .toMap()
    }

    private fun method(methods: Collection<HttpMethod>): HttpMethod {
        if (methods.isEmpty()) return HttpMethod.GET
        if (methods.size == 1) return methods.toTypedArray()[0]
        return if (methods.contains(HttpMethod.GET)) HttpMethod.GET else HttpMethod.POST
    }


    // 狗屎一样的代码
    @GetMapping("/serviceClient/{clientId}/apiList")
    fun apiList(@PathVariable clientId: String,
                @RequestParam(required = false, defaultValue = "REST_WEB") ap: ApplicationType): Any {

        val client = clientChannelManager.clients.filter { it.value.id == clientId }.map { it.value }
                .first()

        if (ApplicationType.REST_WEB == ap) {
            val restwebAPIList = clientAPIMemoryUnit.restWebExposedExposedAPI
                    .filterKeys { it.remoteAddress == client.clientId }
                    .flatMap { it.value }
            val resources = restwebAPIList
                    .groupBy { it.controller }
                    .map { it.key }
                    .map {
                        Resource(
                                id = it,
                                tag = it,
                                name = it.split('.').last(),
                                pid = ROOT_NAV.id,
                                projectId = null,
                                createTime = null,
                                createBy = null
                        )
                    }
            val navNodes = resources.map {
                NavNode(id = it.id!!,
                        title = it.name!!,
                        field = "name",
                        children = null,
                        pid = it.pid!!)
            }

            // TODO  BUG  ROOT_NAV是一个全局常量
            findChild(ROOT_NAV, navNodes)

            val allNode = mutableListOf<NavNode>()

            allNode.add(ROOT_NAV)
            allNode.addAll(navNodes)

            val docs = restwebAPIList.map {
                RestWebDocument(
                        id = MD5Util.MD5Encode(it.controller + it.pattern, "UTF-8"),
                        projectId = null,
                        name = it.function,
                        resource = it.controller,
                        url = it.pattern,
                        description = null,
                        requestHeaderDescriptor = null,
                        requestBodyDescriptor = null,
                        responseBodyDescriptors = null,
                        queryParam = null,
                        method = HttpMethod.valueOf(it.supportMethod[0]),
                        uriVarDescriptors = null,
                        executeResult = null,
                        content = null,
                        responseHeaderDescriptor = null)
            }

            for (navNode in allNode) {
                val childrenDocNode: MutableList<NavNode> = docs
                        .filter { navNode.id == it.resource }
                        .map {
                            val node = NavNode(
                                    id = it.id!!,
                                    title = it.name!!,
                                    field = "",
                                    children = mutableListOf(),
                                    href = null,
                                    pid = navNode.id,
                                    spread = true,
                                    checked = false)

                            node.type = if (DocType.API == it.docType) NodeType.API else NodeType.WIKI
                            node
                        }.toMutableList()

                if (navNode.children != null) {
                    navNode.children!!.addAll(childrenDocNode)
                } else {
                    navNode.children = childrenDocNode
                }
            }
            return ok(mutableListOf(ROOT_NAV))

        } else if (ApplicationType.DUBBO == ap) {
            val dubboAPIList = clientAPIMemoryUnit.dubboExposedExposedAPI
                    .filterKeys { it.remoteAddress == client.clientId }
                    .flatMap { it.value }


        } else {
            throw RuntimeException("Not support application type $ap")
        }

        return ok()
    }
}
