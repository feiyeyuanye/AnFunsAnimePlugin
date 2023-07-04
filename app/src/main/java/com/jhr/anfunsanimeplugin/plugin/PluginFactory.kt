package com.jhr.anfunsanimeplugin.plugin

import com.jhr.anfunsanimeplugin.plugin.components.Const
import com.jhr.anfunsanimeplugin.plugin.components.HomePageDataComponent
import com.jhr.anfunsanimeplugin.plugin.components.MediaClassifyPageDataComponent
import com.jhr.anfunsanimeplugin.plugin.components.MediaDetailPageDataComponent
import com.jhr.anfunsanimeplugin.plugin.components.MediaSearchPageDataComponent
import com.jhr.anfunsanimeplugin.plugin.components.MediaUpdateDataComponent
import com.jhr.anfunsanimeplugin.plugin.components.NewestPageDataComponent
import com.jhr.anfunsanimeplugin.plugin.components.RankMonthPageDataComponent
import com.jhr.anfunsanimeplugin.plugin.components.RankPageDataComponent
import com.jhr.anfunsanimeplugin.plugin.components.RankWeekPageDataComponent
import com.jhr.anfunsanimeplugin.plugin.components.TdetailPageDataComponent
import com.jhr.anfunsanimeplugin.plugin.components.TopicPageDataComponent
import com.jhr.anfunsanimeplugin.plugin.components.UpdateTablePageDataComponent
import com.jhr.anfunsanimeplugin.plugin.components.VideoPlayPageDataComponent
import com.jhr.anfunsanimeplugin.plugin.danmaku.OyydsDanmaku
import com.su.mediabox.pluginapi.IPluginFactory
import com.su.mediabox.pluginapi.components.IBasePageDataComponent
import com.su.mediabox.pluginapi.components.IHomePageDataComponent
import com.su.mediabox.pluginapi.components.IMediaClassifyPageDataComponent
import com.su.mediabox.pluginapi.components.IMediaDetailPageDataComponent
import com.su.mediabox.pluginapi.components.IMediaSearchPageDataComponent
import com.su.mediabox.pluginapi.components.IMediaUpdateDataComponent
import com.su.mediabox.pluginapi.components.IVideoPlayPageDataComponent
import com.su.mediabox.pluginapi.util.PluginPreferenceIns

/**
 * 每个插件必须实现本类
 *
 * 注意包和类名都要相同，且必须提供公开的无参数构造方法
 */
class PluginFactory : IPluginFactory() {

    override val host: String = Const.host

    override fun pluginLaunch() {
        PluginPreferenceIns.initKey(OyydsDanmaku.OYYDS_DANMAKU_ENABLE, defaultValue = true)
    }

    override fun <T : IBasePageDataComponent> createComponent(clazz: Class<T>) = when (clazz) {
        IHomePageDataComponent::class.java -> HomePageDataComponent()  // 主页
        IMediaSearchPageDataComponent::class.java -> MediaSearchPageDataComponent()  // 搜索
        IMediaDetailPageDataComponent::class.java -> MediaDetailPageDataComponent()  // 详情
        IMediaClassifyPageDataComponent::class.java -> MediaClassifyPageDataComponent()  // 媒体分类
        IMediaUpdateDataComponent::class.java -> MediaUpdateDataComponent
        IVideoPlayPageDataComponent::class.java -> VideoPlayPageDataComponent() // 视频播放
        //自定义页面，需要使用具体类而不是它的基类（接口）
        RankPageDataComponent::class.java -> RankPageDataComponent()
        RankMonthPageDataComponent::class.java -> RankMonthPageDataComponent()
        RankWeekPageDataComponent::class.java -> RankWeekPageDataComponent()
        UpdateTablePageDataComponent::class.java -> UpdateTablePageDataComponent()
        NewestPageDataComponent::class.java -> NewestPageDataComponent()  // 最新
        TopicPageDataComponent::class.java -> TopicPageDataComponent()  // 精彩专题
        TdetailPageDataComponent::class.java -> TdetailPageDataComponent()  // 专题影片
        else -> null
    } as? T

}