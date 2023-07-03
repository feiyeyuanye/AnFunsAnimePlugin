package com.jhr.anfunsanimeplugin.plugin.components

import android.util.Log
import com.jhr.anfunsanimeplugin.plugin.components.Const.host
import com.jhr.anfunsanimeplugin.plugin.util.JsoupUtil
import com.jhr.anfunsanimeplugin.plugin.util.ParseHtmlUtil
import com.su.mediabox.pluginapi.components.IMediaSearchPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData

class MediaSearchPageDataComponent : IMediaSearchPageDataComponent {

    override suspend fun getSearchData(keyWord: String, page: Int): List<BaseData> {
        val searchResultList = mutableListOf<BaseData>()
        // https://www.anfuns.cc/search/page/2/wd/%E9%BE%99.html
        val url = "${host}/search/page/${page}/wd/${keyWord}.html"
        Log.e("TAG", url)

        val document = JsoupUtil.getDocument(url)
        searchResultList.addAll(ParseHtmlUtil.parseSearchEm(document, url))
        return searchResultList
    }

}