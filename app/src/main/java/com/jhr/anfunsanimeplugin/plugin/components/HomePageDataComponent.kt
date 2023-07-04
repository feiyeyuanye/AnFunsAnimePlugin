package com.jhr.anfunsanimeplugin.plugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import com.jhr.anfunsanimeplugin.plugin.components.Const.host
import com.jhr.anfunsanimeplugin.plugin.components.Const.layoutSpanCount
import com.jhr.anfunsanimeplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.CustomPageAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.IHomePageDataComponent
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.UIUtil.dp

class HomePageDataComponent : IHomePageDataComponent {

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val url = host
        val doc = JsoupUtil.getDocument(url)
        val data = mutableListOf<BaseData>()

        //1.横幅
        doc.select(".conch-br-box").select("ul").apply {
            val bannerItems = mutableListOf<BannerData.BannerItemData>()
            select("li").forEach { bannerItem ->
                val nameEm = bannerItem.select(".hl-br-title").text()
                val ext = bannerItem.select(".hl-br-sub").text()
                val videoUrl = bannerItem.select("a").attr("href")
                val bannerImage = bannerItem.select("a").attr("data-original")
                if (bannerImage.isNotBlank()) {
//                    Log.e("TAG", "添加横幅项 封面：$bannerImage 链接：$videoUrl")
                    bannerItems.add(
                        BannerData.BannerItemData(bannerImage,nameEm, ext).apply {
                            if (!videoUrl.isNullOrBlank())
                                action = DetailAction.obtain(videoUrl)
                        }
                    )
                }
            }
            if (bannerItems.isNotEmpty())
                data.add(BannerData(bannerItems, 6.dp).apply {
                    layoutConfig = BaseData.LayoutConfig(layoutSpanCount, 14.dp)
                    spanSize = layoutSpanCount
                })
        }
        //2.菜单
        //排行榜
        data.add(
            MediaInfo1Data(
                "", Const.Icon.RANK, "", "总榜",
                otherColor = 0xff757575.toInt(),
                coverScaleType = ImageView.ScaleType.FIT_CENTER,
                coverHeight = 24.dp,
                gravity = Gravity.CENTER
            ).apply {
                spanSize = layoutSpanCount / 4
                action = CustomPageAction.obtain(RankPageDataComponent::class.java)
            })
        data.add(
            MediaInfo1Data(
                "", Const.Icon.RANK, "", "月榜",
                otherColor = 0xff757575.toInt(),
                coverScaleType = ImageView.ScaleType.FIT_CENTER,
                coverHeight = 24.dp,
                gravity = Gravity.CENTER
            ).apply {
                spanSize = layoutSpanCount / 4
                action = CustomPageAction.obtain(RankMonthPageDataComponent::class.java)
            })
        data.add(
            MediaInfo1Data(
                "", Const.Icon.RANK, "", "周榜",
                otherColor = 0xff757575.toInt(),
                coverScaleType = ImageView.ScaleType.FIT_CENTER,
                coverHeight = 24.dp,
                gravity = Gravity.CENTER
            ).apply {
                spanSize = layoutSpanCount / 4
                action = CustomPageAction.obtain(RankWeekPageDataComponent::class.java)
            })
        data.add(
            MediaInfo1Data(
                "", Const.Icon.RANK, "", "时间表",
                otherColor = 0xff757575.toInt(),
                coverScaleType = ImageView.ScaleType.FIT_CENTER,
                coverHeight = 24.dp,
                gravity = Gravity.CENTER
            ).apply {
                spanSize = layoutSpanCount / 4
                action = CustomPageAction.obtain(UpdateTablePageDataComponent::class.java)
            })

        //3.各类推荐
        val modules = doc.select("#conch-content").select("div[class='container']")
        for (em in modules){
            val moduleHeading = em.select(".hl-rb-head").first()
            val type = moduleHeading?.select(".hl-rb-title")
            val typeName = type?.text()
            if (typeName == "每周更新" || typeName == "网络资讯") continue
            val typeUrl = moduleHeading?.select(".hl-rb-more")?.attr("href")
            if (!typeName.isNullOrBlank()) {
                data.add(SimpleTextData(typeName).apply {
                    fontSize = 15F
                    fontStyle = Typeface.BOLD
                    fontColor = Color.BLACK
                    spanSize = layoutSpanCount / 2
                })
                  data.add(SimpleTextData("查看更多 >").apply {
                        fontSize = 12F
                        gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                        fontColor = Const.INVALID_GREY
                        spanSize = layoutSpanCount / 2
                    }.apply {
                        action = if (typeName == "热播推荐"){
                            CustomPageAction.obtain(NewestPageDataComponent::class.java)
                        }else if(typeName == "动漫专题"){
                            // 并不是跳转到详情页，而是这个页面 https://www.anfuns.cc/topic.html
                            CustomPageAction.obtain(TopicPageDataComponent::class.java)
                        }else{
                            ClassifyAction.obtain(typeUrl, typeName)
                        }
                    })
            }
            val li = em.select(".row").select("ul").select("li")
            for ((index,video) in li.withIndex()){
                video.apply {
                    val name = select("a").attr("title")
                    val videoUrl = select("a").attr("href")
                    val coverUrl = select("a").attr("data-original")
                    val episode = select(".remarks").text()

                    if (!name.isNullOrBlank() && !videoUrl.isNullOrBlank() && !coverUrl.isNullOrBlank()) {
                         data.add(
                            MediaInfo1Data(name, coverUrl, videoUrl, episode ?: "")
                                .apply {
                                    spanSize = layoutSpanCount / 3
                                    if(typeName == "动漫专题"){
                                        // 并不是跳转到详情页，而是这些页面
                                        // https://www.anfuns.cc/tdetail-7.html
                                        // https://www.anfuns.cc/tdetail-5.html
                                        action = CustomPageAction.obtain(TdetailPageDataComponent::class.java)
                                        action?.extraData = videoUrl
                                    }else{
                                        action = DetailAction.obtain(videoUrl)
                                    }
                                })
//                        Log.e("TAG", "添加视频 ($name) ($videoUrl) ($coverUrl) ($episode)")
                    }
                }
                if (index == 11) break
            }
        }
        return data
    }
}