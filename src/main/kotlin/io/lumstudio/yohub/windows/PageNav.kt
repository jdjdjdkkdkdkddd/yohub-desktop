package io.lumstudio.yohub.windows

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.*
import io.appoutlet.karavel.Page
import io.lumstudio.yohub.R

enum class PageNav(
    val page: NavPage,
) {
    Home(page = HomePage()),
    PayloadDumper(
        page = PayloadPage(),
    ),
    MagicMaskModule(
        page = MagicMaskModulesPage()
    ),
    MagiskPatcher(
        page = MagiskPatcherPage().apply { parent = MagicMaskModule.page }
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
    val title: String? = null,
    val subtitle: String? = null,
    val isNavigation: Boolean = true
) : Page() {
    var nestedItems: List<NavPage>? = null
    var parent: NavPage? = null
    abstract fun icon(): @Composable () -> Unit
}

class HomePage : NavPage("��ҳ") {

    override fun icon(): @Composable () -> Unit = { Icon(Icons.Default.Home, null) }

    @Composable
    override fun content() {
        HomeScreen(this)
    }
}

class PayloadPage : NavPage("Payload������ȡ", "�����ļ���ȡ", "����ҲࡾPayload�ļ���ȡ��") {
    override fun icon(): @Composable () -> Unit = { Icon(Icons.Default.FolderZip, null) }


    @Composable
    override fun content() {
        PayloadScreen(this)
    }
}

class MagicMaskModulesPage: NavPage("Magiskר��", "Magisk��ع���", "����ҲࡾMagiskר����") {

    override fun icon(): @Composable () -> Unit = { Icon(painter = painterResource(R.icon.icMagisk), null) }

    init {
        nestedItems = arrayListOf(
            MagiskPatcherPage().apply { parent = this@MagicMaskModulesPage }
        )
    }

    @Composable
    override fun content() {
        MagicMaskModulesScreen(this)
    }

}

class MagiskPatcherPage : NavPage("Boot�޲���topjohnwu��", "�޲�Boot����Root��", "����ҲࡾBoot�޲���topjohnwu����", isNavigation = false) {
    override fun icon(): @Composable () -> Unit = { Icon(Icons.Default.MobileOptimized, null) }

    @Composable
    override fun content() {
        MagiskPatcherScreen(this)
    }

}

class SettingsPage : NavPage("����", isNavigation = false) {
    override fun icon(): @Composable () -> Unit = { Icon(Icons.Default.Settings, null) }

    init {
        nestedItems = arrayListOf(
            ThemeSetting(),
            VersionSetting(),
            OpenSourceLicense()
        )
    }

    @Composable
    override fun content() {
        SettingsScreen(this)
    }
}

class FlashImagePage : NavPage("ˢд����", title = "Ϊ�豸ˢ�뾵���ļ�", "����Ҳࡾˢд����") {
    override fun icon(): @Composable () -> Unit = { Icon(Icons.Default.Flash, null) }


    @Composable
    override fun content() {
        FlashImageScreen(this)
    }
}