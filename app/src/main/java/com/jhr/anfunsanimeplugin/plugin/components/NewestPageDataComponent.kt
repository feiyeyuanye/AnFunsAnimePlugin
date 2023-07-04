package com.jhr.anfunsanimeplugin.plugin.components

import com.jhr.anfunsanimeplugin.plugin.components.Const.host
import com.jhr.anfunsanimeplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.ICustomPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.MediaInfo2Data
import com.su.mediabox.pluginapi.data.TagData

/**
 * FileName: RecommendPageDataComponent
 * Founder: Jiang Houren
 * Create Date: 2023/4/18 10:22
 * Profile: 最新
 */
class NewestPageDataComponent : ICustomPageDataComponent {
    override val pageName: String
        get() = "最新"

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val hostUrl = Const.host + "/map.html"
        val document = JsoupUtil.getDocument(hostUrl)

        val data = mutableListOf<BaseData>()

        val li = document.select("#conch-content").select("ul").select("li")
        for (liE in li){
            val a = liE.select("a")
            val hl = a.select(".hl-item-div")[0].select(".hl-item-content").text().split("/")
//            val title = a.select(".hl-item-div")[0].select(".hl-item-title").text()
            val title = hl[0]
            val cover = a.select(".hl-item-div")[0].select("i").attr("data-original")
            val url = a.attr("href")
//            val episode = a.select(".hl-item-div")[0].select(".hl-text-subs").text()
            var episode = hl[1]
            episode += "["+ a.select(".hl-item-div")[1].text() +"]"
            episode += " "+ a.select(".hl-item-div")[4].select("span")[0].text()
            episode += " "+ a.select(".hl-item-div")[4].select("span")[1].text()
            val describe = a.select(".hl-item-div")[3].text()
            val tag = a.select(".hl-item-div")[2].text().split("/")
            val tags = mutableListOf<TagData>()
            for (type in tag) if (type.isNotBlank()) tags.add(TagData(type))
            data.add(MediaInfo2Data(
                    title, cover, host + url, episode, describe, tags
            ).apply {
                action = DetailAction.obtain(url)
            })
        }
        return data
    }

}