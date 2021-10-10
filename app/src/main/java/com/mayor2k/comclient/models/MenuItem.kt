package com.mayor2k.comclient.models

import com.google.gson.annotations.SerializedName

class MenuItem {
    @SerializedName("id")
    var id: Int? = null
    @SerializedName("title")
    var title: String? = null
    @SerializedName("category")
    var category: String? = null
    @SerializedName("composition")
    var composition: String? = null
    @SerializedName("price")
    var price: Int? = null
}