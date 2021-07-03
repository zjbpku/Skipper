package com.fidroid.skipper.data

enum class IdentityType {
    Id,
    Text
}

data class AppInfo(val packageName: String, val skipViews: List<SkipView>)
data class SkipView(val skipIdentity: String, val parentLayoutId: String?, val identityType: IdentityType = IdentityType.Id)
