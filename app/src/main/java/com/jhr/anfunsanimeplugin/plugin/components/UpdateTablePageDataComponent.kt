package com.jhr.anfunsanimeplugin.plugin.components

import android.util.Log
import com.jhr.anfunsanimeplugin.plugin.components.Const.host
import com.jhr.anfunsanimeplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.ICustomPageDataComponent
import com.su.mediabox.pluginapi.data.*
import org.jsoup.select.Elements
import java.util.*

class UpdateTablePageDataComponent : ICustomPageDataComponent {

    override val pageName = "时间表"

    private val days = mutableListOf<String>()
    private lateinit var updateList: Elements

    override suspend fun getData(page: Int): List<BaseData>? {
        Log.d("抓取更新数据", "page=$page")
        if (page != 1)
            return null
        val doc = JsoupUtil.getDocument(host)
            .select("#conch-content").select("div[class='container']")[2] ?: return null
        // 星期
        days.clear()
        doc.select(".hl-rb-head").select("span").select("a").forEach {
            Log.d("星期", it.text())
            days.add(it.text())
        }
        // 当前星期
        val cal: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
        }
        val w = cal.get(Calendar.DAY_OF_WEEK).let {
            if (it == Calendar.SUNDAY) 6 else it - 2
        }
//        Log.d("当前星期", "$w ${days[w]}")
        //更新列表元素
        updateList = doc.select(".row").select(".hl-list-wrap") ?: return null
        val updateLoader = object : ViewPagerData.PageLoader {
            override fun pageName(page: Int): String = days[page]

            override suspend fun loadData(page: Int): List<BaseData> {
//                Log.e("TAG", "获取更新列表 $page ${updateList[page]}")
                val target = updateList[page].select("ul").select("li")
                val ups = mutableListOf<BaseData>()
                var index = 0
                for (em in target) {
                    index++
                    val titleEm = em.select("a")
                    val cover = titleEm.attr("data-original")
                    val title = titleEm.attr("title")
                    val episode = titleEm.select(".remarks").text()
                    val url = titleEm.attr("href")
                    val desc = em.select(".hl-item-sub").text()
                    val tags = mutableListOf<TagData>()
                    val tag = em.select(".hl-pic-tag").text()
                    tags.add(TagData(tag))
                    if (!title.isNullOrBlank() && !episode.isNullOrBlank() && !url.isNullOrBlank()) {
//                        Log.e("TAG", "添加更新 $title $episode $url")
                        val item = MediaInfo2Data(title, cover, host + url, episode, desc, tags).apply {
                            action = DetailAction.obtain(url)
                        }
                        ups.add(item)
                    }
                }
                return ups
            }
        }

        return listOf(ViewPagerData(mutableListOf<ViewPagerData.PageLoader>().apply {
            repeat(7) {
                add(updateLoader)
            }
        }, w).apply {
            layoutConfig = BaseData.LayoutConfig(
                itemSpacing = 0,
                listLeftEdge = 0,
                listRightEdge = 0
            )
        })
    }
}