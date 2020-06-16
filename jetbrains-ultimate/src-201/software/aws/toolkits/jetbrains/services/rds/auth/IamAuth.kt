// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.rds.auth

import com.intellij.credentialStore.Credentials
import com.intellij.database.Dbms
import com.intellij.database.access.DatabaseCredentials
import com.intellij.database.dataSource.DatabaseAuthProvider
import com.intellij.database.dataSource.DatabaseAuthProvider.AuthWidget
import com.intellij.database.dataSource.DatabaseConnectionInterceptor.ProtoConnection
import com.intellij.database.dataSource.DatabaseCredentialsAuthProvider
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.url.JdbcUrlParserUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import software.amazon.awssdk.auth.signer.Aws4Signer
import software.amazon.awssdk.auth.signer.params.Aws4PresignerParams
import software.amazon.awssdk.http.SdkHttpFullRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.regions.Region
import software.aws.toolkits.core.utils.getLogger
import software.aws.toolkits.core.utils.info
import software.aws.toolkits.jetbrains.core.credentials.ConnectionSettings
import software.aws.toolkits.jetbrains.core.datagrip.getAwsConnectionSettings
import software.aws.toolkits.jetbrains.utils.ApplicationThreadPoolScope
import software.aws.toolkits.resources.message
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletionStage

data class RdsAuth(
    val hostname: String,
    val port: Int,
    val user: String,
    val connectionSettings: ConnectionSettings
)

// [DatabaseAuthProvider] is marked as internal, but JetBrains advised this was a correct usage
class IamAuth : DatabaseAuthProvider, CoroutineScope by ApplicationThreadPoolScope("RdsIamAuth") {
    override fun getId(): String = providerId
    override fun getDisplayName(): String = message("rds.iam_connection_display_name")

    override fun isApplicable(dataSource: LocalDataSource): Boolean = dataSource.dbms == Dbms.MYSQL || dataSource.dbms == Dbms.POSTGRES

    override fun createWidget(credentials: DatabaseCredentials, dataSource: LocalDataSource): AuthWidget? = RdsAwsAuthWidget()

    override fun intercept(
        connection: ProtoConnection,
        silent: Boolean
    ): CompletionStage<ProtoConnection>? {
        LOG.info { "Intercepting db connection [$connection]" }
        return future {
            val credentials = getCredentials(connection)
            DatabaseCredentialsAuthProvider.applyCredentials(connection, credentials, true)
        }
    }

    private fun getCredentials(connection: ProtoConnection): Credentials? {
        val authInformation = getAuthInformation(connection)
        val authToken = generateAuthToken(authInformation)
        return Credentials(authInformation.user, authToken)
    }

    internal fun getAuthInformation(connection: ProtoConnection): RdsAuth {
        val awsConnection = connection.getAwsConnectionSettings()
        val parsedUrl = JdbcUrlParserUtil.parsed(connection.connectionPoint.dataSource)
            ?: throw IllegalArgumentException(message("rds.validation.failed_to_parse_url"))
        val host = parsedUrl.getParameter("host")
            ?: throw IllegalArgumentException(message("rds.validation.no_host_specified"))
        val port = parsedUrl.getParameter("port")?.toInt()
            ?: throw IllegalArgumentException(message("rds.validation.no_port_specified"))
        val user = connection.connectionPoint.dataSource.username

        if (user.isBlank()) {
            throw IllegalArgumentException(message("rds.validation.username"))
        }

        return RdsAuth(
            host,
            port,
            user,
            awsConnection
        )
    }

    internal fun generateAuthToken(auth: RdsAuth): String {
        // TODO: Replace when SDK V2 backfills the pre-signer for rds auth token
        val httpRequest = SdkHttpFullRequest.builder()
            .method(SdkHttpMethod.GET)
            .protocol("https")
            .host(auth.hostname)
            .port(auth.port)
            .encodedPath("/")
            .putRawQueryParameter("DBUser", auth.user)
            .putRawQueryParameter("Action", "connect")
            .build()

        // TODO consider configurable expiration time
        val expirationTime = Instant.now().plus(15, ChronoUnit.MINUTES)
        val presignRequest = Aws4PresignerParams.builder()
            .expirationTime(expirationTime)
            .awsCredentials(auth.connectionSettings.credentials.resolveCredentials())
            .signingName("rds-db")
            .signingRegion(Region.of(auth.connectionSettings.region.id))
            .build()

        return Aws4Signer.create().presign(httpRequest, presignRequest).uri.toString().removePrefix("https://")
    }

    companion object {
        const val providerId = "aws.rds.iam"
        private val LOG = getLogger<IamAuth>()
    }
}