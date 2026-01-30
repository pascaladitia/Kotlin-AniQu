package com.pascal.feature.profile

import com.pascal.contants.AppConstants
import com.pascal.model.request.UserProfileRequest
import com.pascal.plugin.RoleManagement
import com.pascal.utils.ApiResponse
import com.pascal.utils.extension.currentUser
import com.pascal.utils.extension.fileExtension
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

fun Route.profileRoutes(controller: ProfileService) {
    authenticate(
        RoleManagement.ADMIN.role,
        RoleManagement.USER.role
    ) {
        route("/route") {
            get {
                call.respond(
                    ApiResponse.success(
                        controller.getProfile(call.currentUser().userId), HttpStatusCode.OK
                    )
                )
            }

            put {
                val param = UserProfileRequest(
                    firstName = call.parameters["firstName"],
                    lastName = call.parameters["lastName"],
                    mobile = call.parameters["mobile"],
                    streetAddress = call.parameters["streetAddress"],
                    city = call.parameters["city"],
                    occupation = call.parameters["occupation"],
                    postCode = call.parameters["postCode"],
                    gender = call.parameters["gender"],
                )
                call.respond(
                    ApiResponse.success(
                        controller.updateProfile(call.currentUser().userId, param), HttpStatusCode.OK
                    )
                )
            }

            post("image-upload") {
                val multipartData = call.receiveMultipart()

                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            UUID.randomUUID()?.let { imageId ->
                                val fileName = part.originalFileName as String
                                val fileLocation = fileName.let {
                                    "${AppConstants.ImageFolder.PROFILE_IMAGE_LOCATION}$imageId${it.fileExtension()}"
                                }
                                fileLocation.let {
                                    File(it).writeBytes(withContext(Dispatchers.IO) {
                                        part.streamProvider().readBytes()
                                    })
                                }
                                val fileNameInServer = imageId.toString().plus(fileLocation.fileExtension())
                                controller.updateProfileImage(call.currentUser().userId, fileNameInServer)
                            }
                        }

                        else -> {}
                    }
                    part.dispose()
                }
            }
        }
    }
}