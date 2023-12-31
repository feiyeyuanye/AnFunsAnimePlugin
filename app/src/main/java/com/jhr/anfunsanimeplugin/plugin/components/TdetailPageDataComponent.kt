package com.jhr.anfunsanimeplugin.plugin.components

import com.jhr.anfunsanimeplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.CustomPageAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.ICustomPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.MediaInfo1Data
import com.su.mediabox.pluginapi.util.UIUtil.dp

/**
 * FileName: TdetailPageDataComponent
 * Founder: Jiang Houren
 * Create Date: 2023/7/4 10:44
 * Profile: 专题影片
 */
class TdetailPageDataComponent : ICustomPageDataComponent {

    var hostUrl = Const.host

    override val pageName: String
        get() = "专题影片"

    override fun initPage(action: CustomPageAction) {
        super.initPage(action)
        hostUrl += action.extraData
    }

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val data = mutableListOf<BaseData>()
        val document = JsoupUtil.getDocument(hostUrl)

        val head = document.select(".hl-has-item").select(".row")
        val headTitle = head.select("h2").text()
        val headCover = head.select(".hl-item-thumb").attr("data-original")
        val headEpisode = head.select(".hl-content-text").text()
        data.add(MediaInfo1Data(
            headTitle, headCover, "",  headEpisode
        ).apply {
            layoutConfig = BaseData.LayoutConfig(Const.layoutSpanCount, 14.dp)
            spanSize = Const.layoutSpanCount
        })

        val li = document.select(".hl-list-wrap")[0].select("ul").select("li")
        for (liE in li){
            val a = liE.select("a")
            val title = a.attr("title")
            val cover = a.attr("data-original")
            val url = a.attr("href")
            val episode = a.select(".remarks").text()
            val item = MediaInfo1Data(
                title, cover, Const.host + url,episode
            ).apply {
                spanSize = Const.layoutSpanCount / 3
                action = DetailAction.obtain(url)
            }
            data.add(item)
        }
        return data
    }

}