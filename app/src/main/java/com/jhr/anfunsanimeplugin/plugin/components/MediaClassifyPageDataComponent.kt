package com.jhr.anfunsanimeplugin.plugin.components

import android.util.Log
import com.jhr.anfunsanimeplugin.plugin.util.JsoupUtil
import com.jhr.anfunsanimeplugin.plugin.util.ParseHtmlUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.components.IMediaClassifyPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.ClassifyItemData
import com.su.mediabox.pluginapi.util.PluginPreferenceIns
import com.su.mediabox.pluginapi.util.TextUtil.urlDecode
import com.su.mediabox.pluginapi.util.WebUtil
import com.su.mediabox.pluginapi.util.WebUtilIns
import org.jsoup.Jsoup

class MediaClassifyPageDataComponent : IMediaClassifyPageDataComponent {
    var classify : String = Const.host +"/show/1---.html"

    override suspend fun getClassifyItemData(): List<ClassifyItemData> {
        val classifyItemDataList = mutableListOf<ClassifyItemData>()
        //示例：使用WebUtil解析动态生成的分类项
        // 新旧番剧
        // https://www.anfuns.cc/show/1---.html
        // 蓝光无修
        // https://www.anfuns.cc/show/2---.html
        // 动漫剧场
        // https://www.anfuns.cc/show/3---.html
        // 欧美动漫
        // https://www.anfuns.cc/show/4---.html
        val cookies = mapOf("cookie" to PluginPreferenceIns.get(JsoupUtil.cfClearanceKey, ""))
        Log.e("TAG","classify $classify")
        val document = Jsoup.parse(
            WebUtilIns.getRenderedHtmlCode(
                 classify, loadPolicy = object :
                    WebUtil.LoadPolicy by WebUtil.DefaultLoadPolicy {
                    override val headers = cookies
                    override val userAgentString = Const.ua
                    override val isClearEnv = false
                }
            )
        )
        document.select("#conch-content").select(".hl-row-box")[0]
            .select("div[class='hl-filter-wrap hl-navswiper swiper-container-initialized swiper-container-horizontal swiper-container-free-mode']").forEach {
            classifyItemDataList.addAll(ParseHtmlUtil.parseClassifyEm(it))
        }
        document.select("#conch-content").select(".hl-row-box")[1].select(".hl-rb-title").select("a").forEach { a ->
            classifyItemDataList.add(ClassifyItemData().apply {
                action = ClassifyAction.obtain(
                    a.attr("href"), "筛选", a.text()
                )
            })
        }
        return classifyItemDataList
    }

    override suspend fun getClassifyData(
        classifyAction: ClassifyAction,
        page: Int
    ): List<BaseData> {
        val classifyList = mutableListOf<BaseData>()
        Log.e("TAG", "获取分类数据 ${classifyAction.url}")
        var str = classifyAction.url?.urlDecode() ?:""
        str = when(str){
            // 全部 href
            "javascript:void(0)" -> {
                classify
            }
            // 新旧番剧
            "/type/1.html" -> {
                // 修改分类项
                classify = Const.host +"/show/1---.html"
                classify
            }
            // 蓝光无修
            "/type/2.html" -> {
                classify = Const.host +"/show/2---.html"
                classify
            }
            // 动漫剧场
            "/type/3.html" -> {
                classify = Const.host +"/show/3---.html"
                classify
            }
            // 欧美动漫
            "/type/4.html" -> {
                classify = Const.host +"/show/4---.html"
                classify
            }
            else -> { str }
        }

        // 指定要插入的字符 charToInsert
        val charToInsert = "/page/${page}"
        val indexToInsert = str.length - 5

        // 使用 StringBuilder 创建一个可变的字符串，调用 insert() 方法将字符插入到指定位置，最后将结果转换回不可变字符串。
        var url = StringBuilder(str).insert(indexToInsert, charToInsert).toString()
        if (!url.startsWith(Const.host)){
            url = Const.host + url
        }
        Log.e("TAG", "获取分类数据 $url")
        JsoupUtil.getDocument(url).also {
            classifyList.addAll(ParseHtmlUtil.parseClassifyEm(it, url))
        }
        return classifyList
    }
}