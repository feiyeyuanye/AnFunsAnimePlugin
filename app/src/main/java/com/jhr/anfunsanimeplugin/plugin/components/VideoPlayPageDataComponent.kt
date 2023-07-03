package com.jhr.anfunsanimeplugin.plugin.components

import android.util.Log
import com.jhr.anfunsanimeplugin.plugin.components.Const.host
import com.jhr.anfunsanimeplugin.plugin.components.Const.ua
import com.jhr.anfunsanimeplugin.plugin.danmaku.OyydsDanmaku
import com.jhr.anfunsanimeplugin.plugin.danmaku.OyydsDanmakuParser
import com.jhr.anfunsanimeplugin.plugin.util.JsoupUtil
import com.jhr.anfunsanimeplugin.plugin.util.Text.trimAll
import com.jhr.anfunsanimeplugin.plugin.util.oyydsDanmakuApis
import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.su.mediabox.pluginapi.components.IVideoPlayPageDataComponent
import com.su.mediabox.pluginapi.data.VideoPlayMedia
import com.su.mediabox.pluginapi.util.PluginPreferenceIns
import com.su.mediabox.pluginapi.util.TextUtil.urlDecode
import com.su.mediabox.pluginapi.util.WebUtil
import com.su.mediabox.pluginapi.util.WebUtilIns
import kotlinx.coroutines.*

class VideoPlayPageDataComponent : IVideoPlayPageDataComponent {

    private var episodeDanmakuId = ""
    override suspend fun getDanmakuData(
        videoName: String,
        episodeName: String,
        episodeUrl: String
    ): List<DanmakuItemData>? {
        try {
            val config = PluginPreferenceIns.get(OyydsDanmaku.OYYDS_DANMAKU_ENABLE, true)
            if (!config)
                return null
            val name = videoName.trimAll()
            var episode = episodeName.trimAll()
            //剧集对集去除所有额外字符，增大弹幕适应性
            val episodeIndex = episode.indexOf("集")
            if (episodeIndex > -1 && episodeIndex != episode.length - 1) {
                episode = episode.substring(0, episodeIndex + 1)
            }
            Log.d("请求Oyyds弹幕", "媒体:$name 剧集:$episode")
            return oyydsDanmakuApis.getDanmakuData(name, episode).data.let { danmukuData ->
                val data = mutableListOf<DanmakuItemData>()
                danmukuData?.data?.forEach { dataX ->
                    OyydsDanmakuParser.convert(dataX)?.also { data.add(it) }
                }
                episodeDanmakuId = danmukuData?.episode?.id ?: ""
                data
            }
        } catch (e: Exception) {
            throw RuntimeException("弹幕加载错误：${e.message}")
        }
    }

    override suspend fun putDanmaku(
        videoName: String,
        episodeName: String,
        episodeUrl: String,
        danmaku: String,
        time: Long,
        color: Int,
        type: Int
    ): Boolean = try {
        Log.d("发送弹幕到Oyyds", "内容:$danmaku 剧集id:$episodeDanmakuId")
        oyydsDanmakuApis.addDanmaku(
            danmaku,
            //Oyyds弹幕标准时间是秒
            (time / 1000F).toString(),
            episodeDanmakuId,
            OyydsDanmakuParser.danmakuTypeMap.entries.find { it.value == type }?.key ?: "scroll",
            String.format("#%02X", color)
        )
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    /**
     * bug -> 标题会显示上一个视频的标题
     */
    override suspend fun getVideoPlayMedia(episodeUrl: String): VideoPlayMedia {
        val url = host + episodeUrl
        val document = JsoupUtil.getDocument(url)
        Log.e("TAG", url)
        val cookies = mapOf("cookie" to PluginPreferenceIns.get(JsoupUtil.cfClearanceKey, ""))
        //解析链接
        val videoUrl = withContext(Dispatchers.Main) {
            val iframeUrl = withTimeoutOrNull(10 * 1000) {
                WebUtilIns.interceptResource(
                    url, "(.*)url=(.*)",
                    loadPolicy = object : WebUtil.LoadPolicy by WebUtil.DefaultLoadPolicy {
                        override val headers = cookies
                        override val userAgentString = ua
                        override val isClearEnv = false
                    }
                )
            } ?: ""
            async {
                Log.e("TAG", iframeUrl)
                when {
                    iframeUrl.isBlank() -> iframeUrl
// https://www.anfuns.cc/vapi/eden/?url=https://media-oss.anfuns.cn/m3u8/1688213086908.m3u8&next=//www.anfuns.cc&vid=1808&title=%E9%9D%92%E6%98%A5%E7%AC%A8%E8%9B%8B%E5%B0%91%E5%B9%B4%E4%B8%8D%E4%BC%9A%E6%A2%A6%E5%88%B0%E5%A8%87%E6%80%9C%E5%A4%96%E5%87%BA%E5%A6%B9&nid=1&uid=0&name=%E6%B8%B8%E5%AE%A2&group=%E6%B8%B8%E5%AE%A2
                    iframeUrl.contains(".m3u8&") -> iframeUrl.substringAfter("url=")
                        .substringBefore("&")
                        .urlDecode()
// https://www.anfuns.cc/vapi/eden/?url=https://mysource-anfunsapi-bangumi.anfuns.cn/api/v3/file/source/5169/[CASO&SGS][Guilty_Crown][01][GB][H.264_AAC][1280x720][C6594CE1].mp4?sign=zfkW2MjQ1vAk-MqU19ypNdCXJeJn3JAI27p_euCWqTs=:0&next=//www.anfuns.cc/play/170-1-2.html&vid=170&title=%E7%BD%AA%E6%81%B6%E7%8E%8B%E5%86%A0&nid=1&uid=0&name=%E6%B8%B8%E5%AE%A2&group=%E6%B8%B8%E5%AE%A2
//                    iframeUrl.contains(".mp4?") -> iframeUrl.substringAfter("url=")
//                        .substringBefore("&next=").urlDecode()
//// https://www.anfuns.cc/vapi/eden/?url=https://mysource-anfunsapi-bangumi.anfuns.cn/f/qRRzsm/[ANi]%20%E7%AC%AC%E4%BA%8C%E6%AC%A1%E8%A2%AB%E7%95%B0%E4%B8%96%E7%95%8C%E5%8F%AC%E5%96%9A%20-%2001%20[1080P][Baha][WEB-DL][AAC%20AVC][CHT].mp4&next=//www.anfuns.cc/play/1692-1-2.html&vid=1692&title=%E7%AC%AC%E4%BA%8C%E6%AC%A1%E8%A2%AB%E5%BC%82%E4%B8%96%E7%95%8C%E5%8F%AC%E5%94%A4&nid=1&uid=0&name=%E6%B8%B8%E5%AE%A2&group=%E6%B8%B8%E5%AE%A2
//                    iframeUrl.contains(".mp4&") -> iframeUrl.substringAfter("url=")
//                        .substringBefore("&next=").urlDecode()
                    // 可以播放的格式
// https://mysource-anfunsapi-bangumi.anfuns.cn/api/v3/file/source/5172/[CASO&SGS][Guilty_Crown][03][GB][H.264_AAC][1280x720][DB791550].mp4?sign=-_3wuUHfMqYjJShgduT5U3yUFepmpVwlZnxXpxBtdxc=:0
// https://mysource-anfunsapi-bangumi.anfuns.cn/f/qRRzsm/[ANi] 第二次被異世界召喚 - 01 [1080P][Baha][WEB-DL][AAC AVC][CHT].mp4
                    iframeUrl.contains(".mp4") -> iframeUrl.substringAfter("url=")
                        .substringBefore("&next=").urlDecode()
                    // 此类不知如何解析
// 过滤 url= ：https://www.anfuns.cc/vapi/eden/?url=yanm_2023/05/6fd698618o5b08j6793b6adek42d57048375b&next=//www.anfuns.cc/play/726-1-2.html&vid=726&title=%E6%9F%90%E7%A7%91%E5%AD%A6%E7%9A%84%E8%B6%85%E7%94%B5%E7%A3%81%E7%82%AE&nid=1&uid=0&name=%E6%B8%B8%E5%AE%A2&group=%E6%B8%B8%E5%AE%A2
// 过滤 m3u8 ：https://07vod-proxy-vod101-1.07vods-proxy.top/cache/b5f48GZQ6CIHj%5Bc%5Dme6931qtQl2AK5E9HJB95K2MBLCAvxYKVG8MEzsWYM4srRG9BiHE7c8CGQgeoKV%5Ba%5DajDcOeQNLAYbD7LRf14j2CFd9i1ogtgKNl5VCg9sLl.m3u8?st=1688262771&sc=343c52a80beca388548a78ea97227202
                    else -> {}
                }
            }
        }
        //剧集名
        val name = withContext(Dispatchers.Default) {
            async {
                document.select(".hl-infos-title").select(".hl-text-muted").text()
            }
        }
        Log.e("TAG", "解析后name："+name.await())
        Log.e("TAG", "解析后url："+videoUrl.await())
        return VideoPlayMedia(name.await(), videoUrl.await() as String)
    }
}