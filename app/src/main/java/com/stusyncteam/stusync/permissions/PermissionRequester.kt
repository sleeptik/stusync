package com.stusyncteam.stusync.permissions

import android.Manifest.permission.GET_ACCOUNTS
import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.os.Build
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.models.PermissionRequest

class PermissionRequester {
    companion object {
        fun requestGetAccountsPermission(host: Activity) {
            requestPermissions(host, GET_ACCOUNTS)
        }

        fun requestPostNotificationsPermission(host: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(host, POST_NOTIFICATIONS)
            }
        }

        private fun requestPermissions(host: Activity, vararg permissions: String) {
            if (!EasyPermissions.hasPermissions(host, *permissions)) {
                val permissionRequest = PermissionRequest.Builder(host)
                    .perms(permissions)
                    .build()

                EasyPermissions.requestPermissions(host, permissionRequest)
            }
        }
    }
}