package com.ifpr.MontarCurriculo.baseclasses

import android.os.Parcelable

data class Item(
    var endereco: String? = null,
    val base64Image: String? = null,
    val imageUrl: String? = null
)
