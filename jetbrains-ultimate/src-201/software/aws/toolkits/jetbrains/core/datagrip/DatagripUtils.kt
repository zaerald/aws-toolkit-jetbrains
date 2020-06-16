// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.core.datagrip

import com.intellij.database.dataSource.DatabaseConnectionInterceptor.ProtoConnection
import software.aws.toolkits.jetbrains.core.credentials.ConnectionSettings
import software.aws.toolkits.jetbrains.core.credentials.CredentialManager
import software.aws.toolkits.jetbrains.core.region.AwsRegionProvider
import software.aws.toolkits.resources.message

const val CREDENTIAL_ID_PROPERTY = "AWS.CredentialId"
const val REGION_ID_PROPERTY = "AWS.RegionId"

fun ProtoConnection.getAwsConnectionSettings(): ConnectionSettings {
    val credentialManager = CredentialManager.getInstance()
    val regionId = connectionPoint.additionalJdbcProperties[REGION_ID_PROPERTY]
    val region = regionId?.let {
        AwsRegionProvider.getInstance().allRegions()[it]
    } ?: throw IllegalArgumentException(message("datagrip.validation.invalid_region_specified", regionId.toString()))
    val credentialId = connectionPoint.additionalJdbcProperties[CREDENTIAL_ID_PROPERTY]
    val credentials = credentialId?.let { id ->
        credentialManager.getCredentialIdentifierById(id)?.let {
            credentialManager.getAwsCredentialProvider(it, region)
        }
    } ?: throw IllegalArgumentException(message("datagrip.validation.invalid_credential_specified", credentialId.toString()))
    return ConnectionSettings(credentials, region)
}