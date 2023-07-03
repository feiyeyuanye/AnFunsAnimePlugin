package com.jhr.anfunsanimeplugin.plugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import com.jhr.anfunsanimeplugin.plugin.components.Const.host
import com.jhr.anfunsanimeplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.components.IMediaDetailPageDataComponent
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.action.PlayAction
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.TextUtil.urlEncode
import com.su.mediabox.pluginapi.util.UIUtil.dp
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class MediaDetailPageDataComponent : IMediaDetailPageDataComponent {

    override suspend fun getMediaDetailData(partUrl: String): Triple<String, String, List<BaseData>> {
        var cover = ""
        var title = ""
        var desc = ""
        var score = -1F
        // 导演
        var director = ""
        // 主演
        var protagonist = ""
        // 地区 语言
        var animeLanguage = ""
        // 更新时间
        var time = ""
        var upState = ""
        // 时长
        var duration = ""
        // 上映
        var show = ""
        val url = Const.host + partUrl
        val tags = mutableListOf<TagData>()
        val details = mutableListOf<BaseData>()

        val document = JsoupUtil.getDocument(url)

        // ------------- 番剧头部信息
        cover = document.select(".hl-dc-pic").select("span").attr("data-original")
        title = document.select(".hl-dc-headwrap").select(".hl-dc-title").text() + "\n" +
                document.select(".hl-dc-headwrap").select(".hl-dc-sub").text()
        // 更新状况
        val upStateItems = document.select(".hl-dc-content")
            .select(".hl-vod-data").select(".hl-full-box").select("ul").select("li")
        for (upStateEm in upStateItems){
            val t = upStateEm.text()
            when{
                t.contains("导演：") -> director = t
                t.contains("主演：") -> protagonist = t
                t.contains("状态：") -> upState = t
                t.contains("地区：") -> {
                    animeLanguage += t
                }
                t.contains("语言：") -> {
                    animeLanguage += " | " + t.substringAfter("语言：")
                }
                t.contains("连载：") -> time = t
                t.contains("时长：") -> duration = t
                t.contains("上映：") -> show = t
                t.contains("类型：") -> {
                    //类型
                    val typeElements: Elements = upStateEm.select("a")
                    for (l in typeElements.indices) {
                        tags.add(TagData(typeElements[l].text()).apply {
                            action = ClassifyAction.obtain(typeElements[l].attr("href"), "", typeElements[l].text())
                        })
                    }
                }
                t.contains("简介：") -> desc = t
            }
        }
        //评分
        score = document.select(".hl-score-nums")[1].text().toFloatOrNull() ?: -1F

        // ---------------------------------- 播放列表+header
        val module = document.select(".hl-play-source")
        val playNameList = module.select(".hl-plays-wrap").select("a")
        val playEpisodeList = module.select(".hl-tabs-box")
        for (index in 0..playNameList.size) {
            val playName = playNameList.getOrNull(index)
            val playEpisode = playEpisodeList.getOrNull(index)
            if (playName != null && playEpisode != null) {
                val episodes = parseEpisodes(playEpisode)
                if (episodes.isNullOrEmpty())
                    continue
                details.add(
                    SimpleTextData(
                        playName.text() + "(${episodes.size}集)"
                    ).apply {
                        fontSize = 16F
                        fontColor = Color.WHITE
                    }
                )
                details.add(EpisodeListData(episodes))
            }
        }
        // ----------------------------------  系列动漫推荐
            val series = parseSeries(document)
            if (series.isNotEmpty()) {
                details.add(
                    SimpleTextData("其他系列作品").apply {
                        fontSize = 16F
                        fontColor = Color.WHITE
                    }
                )
                details.addAll(series)
            }
        return Triple(cover, title, mutableListOf<BaseData>().apply {
            add(Cover1Data(cover, score = score).apply {
                layoutConfig =
                    BaseData.LayoutConfig(
                        itemSpacing = 12.dp,
                        listLeftEdge = 12.dp,
                        listRightEdge = 12.dp
                    )
            })
            add(
                SimpleTextData(title).apply {
                    fontColor = Color.WHITE
                    fontSize = 20F
                    gravity = Gravity.CENTER
                    fontStyle = 1
                }
            )
            add(TagFlowData(tags))
            add(
                LongTextData(desc).apply {
                    fontColor = Color.WHITE
                }
            )
            add(SimpleTextData("·$director").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$protagonist").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$animeLanguage").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$show").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$time").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$duration").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$upState").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(LongTextData(douBanSearch(title)).apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            addAll(details)
        })
    }

    private fun parseEpisodes(element: Element): List<EpisodeData> {
        val episodeList = mutableListOf<EpisodeData>()
        val elements: Elements = element.select("li").select("a")
        for (k in elements.indices) {
            val episodeUrl = elements[k].attr("href")
            episodeList.add(
                EpisodeData(elements[k].text(), episodeUrl).apply {
                    action = PlayAction.obtain(episodeUrl)
                }
            )
        }
        return episodeList
    }

    private fun parseSeries(element: Element): List<MediaInfo1Data> {
        val videoInfoItemDataList = mutableListOf<MediaInfo1Data>()
        val results = element.select(".hl-change-box1").select("ul").select("li").select(".hl-item-thumb")
        for (i in results.indices) {
            val cover = results[i].attr("data-original")
            val title = results[i].attr("title")
            val url = results[i].attr("href")
            val item = MediaInfo1Data(
                title, cover, Const.host + url,
                nameColor = Color.WHITE, coverHeight = 120.dp
            ).apply {
                action = DetailAction.obtain(url)
            }
            videoInfoItemDataList.add(item)
        }
        return videoInfoItemDataList
    }

    private fun douBanSearch(name: String) =
        "·豆瓣评分：https://m.douban.com/search/?query=${name.urlEncode()}"
}