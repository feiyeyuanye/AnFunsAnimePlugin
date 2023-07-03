package com.jhr.anfunsanimeplugin.plugin.components

import com.jhr.anfunsanimeplugin.plugin.actions.CustomAction
import com.jhr.anfunsanimeplugin.plugin.components.Const.host
import com.jhr.anfunsanimeplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.ICustomPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.MediaInfo2Data
import com.su.mediabox.pluginapi.data.TagData
import com.su.mediabox.pluginapi.data.ViewPagerData
import org.jsoup.nodes.Element

class RankMonthPageDataComponent : ICustomPageDataComponent {

    override val pageName = "人气排行榜-月榜"
    override fun menus() = mutableListOf(CustomAction())

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val url = "$host/label/rankmonth.html"
        val doc = JsoupUtil.getDocument(url)

        val rank1 = doc.select("div[class='hl-list-wrap hl-tabs-box']")[0].let {
                    object : ViewPagerData.PageLoader {
                        override fun pageName(page: Int): String {
                            return "全部"
                        }

                        override suspend fun loadData(page: Int): List<BaseData> {
                            return getTotalRankData(it)
                        }
                    }
                }
        val rank2 = doc.select("div[class='hl-list-wrap hl-tabs-box']")[1].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "新旧番剧"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        val rank3 = doc.select("div[class='hl-list-wrap hl-tabs-box']")[2].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "蓝光无修"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        val rank4 = doc.select("div[class='hl-list-wrap hl-tabs-box']")[3].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "动漫剧场"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        val rank5 = doc.select("div[class='hl-list-wrap hl-tabs-box']")[4].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "欧美动漫"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        return listOf(ViewPagerData(mutableListOf(rank1, rank2,rank3,rank4,rank5)).apply {
            layoutConfig = BaseData.LayoutConfig(
                itemSpacing = 0,
                listLeftEdge = 0,
                listRightEdge = 0
            )
        })
    }

    private fun getTotalRankData(element: Element): List<BaseData> {
        val data = mutableListOf<BaseData>()
        element.select("ul").select("li").forEach {
                val cover = it.select(".hl-item-thumb").attr("data-original")
                val title = it.select(".hl-item-num").text() +" "+ it.select(".hl-item-thumb").attr("title")
                val url = it.select(".hl-item-thumb").attr("href")
                var episode = it.select(".hl-item-remarks").text()
                        // 移除前面的分数
                        // removePrefix ：如果此字符串以给定的prefix开头，则返回删除了前缀的此字符串的副本。否则，返回该字符串。
                episode = episode.removePrefix(it.select(".hl-item-remarks").select("span").text())
                episode += " ["+ it.select(".hl-item-div")[1].text() +"]"
                val types = it.select(".hl-item-div")[2].text()
                val tags = mutableListOf<TagData>()
                for (type in types.split("/"))
                    if (type.isNotBlank()) tags.add(TagData(type))
                val describe = it.select(".hl-item-div")[3].text()
                val item = MediaInfo2Data(title, cover, host + url, episode, describe, tags).apply {
                    action = DetailAction.obtain(url)
                }
            data.add(item)
        }
        return data
    }
}