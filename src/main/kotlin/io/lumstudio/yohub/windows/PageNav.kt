package io.lumstudio.yohub.windows

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.*
import io.appoutlet.karavel.Page

enum class PageNav(
    val page: NavPage,
) {
    Home(page = HomePage()),
    PayloadDumper(
        page = PayloadPage(),
    ),
    MagiskPatcher(
        page = MagiskPatcherPage(),
    ),
    FlashImage(
        page = FlashImagePage()
    ),
    Settings(
        page = SettingsPage(),
    )
}

abstract class NavPage(
    val label: String,
    val icon: ImageVector? = null,
    val title: String? = null,
    val subtitle: String? = null,
) : Page() {
    var nestedItems: List<NavPage>? = null
}

class HomePage : NavPage("��ҳ", icon = Icons.Default.Home) {

    @Composable
    override fun content() {
        HomeScreen(this)
    }
}

class PayloadPage : NavPage("Payload������ȡ", icon = Icons.Default.FolderZip, "�����ļ���ȡ", "����ҲࡾPayload�ļ���ȡ��") {
    @Composable
    override fun content() {
        PayloadScreen(this)
    }
}

class MagiskPatcherPage : NavPage("Boot�޲���topjohnwu��", icon = Icons.Default.MobileOptimized, "�޲�Boot����Root��", "����ҲࡾBoot�޲���topjohnwu����") {
    @Composable
    override fun content() {
        MagiskPatcherScreen(this)
    }

}

class SettingsPage : NavPage("����", icon = Icons.Default.Settings,) {

    init {
        nestedItems = arrayListOf(
            ThemeSetting()
        )
    }

    @Composable
    override fun content() {
        SettingsScreen(this)
    }
}

class FlashImagePage : NavPage("ˢд����", icon = Icons.Default.Flash, title = "Ϊ�豸ˢ�뾵���ļ�", "����Ҳࡾˢд����") {

    @Composable
    override fun content() {
        FlashImageScreen(this)
    }
}