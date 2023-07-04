package com.jhr.anfunsanimeplugin.plugin.components

import com.jhr.anfunsanimeplugin.plugin.components.Const.layoutSpanCount
import com.jhr.anfunsanimeplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.CustomPageAction
import com.su.mediabox.pluginapi.components.ICustomPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.MediaInfo1Data

/**
 * FileName: RecommendPageDataComponent
 * Founder: Jiang Houren
 * Create Date: 2023/4/18 10:22
 * Profile: 精彩专题
 */
class TopicPageDataComponent : ICustomPageDataComponent {
    override val pageName: String
        get() = "精彩专题"

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val hostUrl = Const.host + "/topic"
        val document = JsoupUtil.getDocument(hostUrl)

        val data = mutableListOf<BaseData>()

        val li = document.select("#conch-content").select("ul").select("li")
        for (liE in li){
            val a = liE.select("a")
            val title = a.select(".hl-item-title").text()
            val cover = a.attr("data-original")
            val url = a.attr("href")
            val episode = a.select(".remarks").text()
            val item = MediaInfo1Data(
                title, cover, Const.host + url,episode
            ).apply {
                action = CustomPageAction.obtain(TdetailPageDataComponent::class.java)
                action?.extraData = url
            }
            data.add(item)
        }
        data[0].layoutConfig = BaseData.LayoutConfig(layoutSpanCount / 2)
        return data
    }
}