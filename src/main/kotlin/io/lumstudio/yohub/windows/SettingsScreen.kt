package io.lumstudio.yohub.windows

import androidx.compose.foundation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.konyaco.fluent.component.NavigationItemSeparator
import com.konyaco.fluent.component.ScrollbarContainer
import com.konyaco.fluent.component.rememberScrollbarAdapter
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.*
import io.lumstudio.yohub.common.LocalContext
import io.lumstudio.yohub.common.LocalIOCoroutine
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.common.utils.*
import io.lumstudio.yohub.runtime.LocalInstallThemesPath
import io.lumstudio.yohub.theme.*
import io.lumstudio.yohub.ui.component.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.swing.JFrame

@Composable
fun SettingsScreen(settingsPage: SettingsPage) {
    val scrollState = rememberScrollState()
    ScrollbarContainer(
        adapter = rememberScrollbarAdapter(scrollState),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(scrollState).padding(16.dp)
        ) {
            settingsPage.nestedItems?.onEach {
                it.content()
            }
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Copyright @ 2023 ��̴������Ƽ� All Rights Reserved", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

class ThemeSetting : NavPage("��������", isNavigation = false) {

    override fun icon(): () -> Unit = {  }

    private val gson by lazy { Gson() }

    @OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun content() {
        val themeStore = LocalTheme.current
        val preferencesStore = LocalPreferences.current
        val ioCoroutine = LocalIOCoroutine.current
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Toolbar(label, enableAnimate = false)
            FluentItem(
                Icons.Default.DarkTheme,
                "��ɫģʽ"
            ) {
                DarkTheme.values().onEach {
                    Row(
                        modifier = Modifier.padding(start = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                themeStore.theme = it
                                preferencesStore.preference[PreferencesName.DARK_MODEL.toString()] = gson.toJson(it)
                                ioCoroutine.ioScope.launch {
                                    preferencesStore.submit()
                                }
                            },
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeStore.theme == it,
                                onClick = null,
                                enabled = false
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(it.annotation)
                        }
                    }
                }
            }

            val colorThemeItems = remember { mutableStateListOf<ColorLoader.ColorThemeItem>() }
            val selectTheme = remember { mutableStateOf("") }
            selectTheme.value = preferencesStore.preference[PreferencesName.COLOR_THEME.toString()] ?: "YoHub Color"
            val targetThemeFileName = remember { mutableStateOf("") }
            val targetThemeName = remember { mutableStateOf("") }
            val installThemesPathStore = LocalInstallThemesPath.current
            val colorThemeStore = LocalColorTheme.current
            val uninstallState = remember { mutableStateOf(false) }
            val generateState = remember { mutableStateOf(false) }
            val helpState = remember { mutableStateOf(false) }
            val colorLoader by remember {
                mutableStateOf(
                    ColorLoader(
                        preferencesStore,
                        installThemesPathStore,
                        colorThemeStore
                    )
                )
            }

            LaunchedEffect(Unit) {
                colorThemeItems.clear()
                colorThemeItems.addAll(colorLoader.loadInstalledColorThemes())
            }

            FluentFold(
                Icons.Default.Color,
                "������ɫ",
                content = {
                    TextButton(
                        onClick = {
                            generateState.value = true
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("��������")
                    }
                }
            ) {
                NavigationItemSeparator(modifier = Modifier.padding(bottom = 4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    ColorThemeItemDefault(colorThemeStore, selectTheme, preferencesStore)
                    colorThemeItems.onEach {
                        ColorThemeItem(
                            colorThemeStore,
                            preferencesStore,
                            selectTheme,
                            it,
                            uninstallState,
                            targetThemeFileName,
                            targetThemeName
                        )
                    }
                    ColorThemeItemInstall(colorLoader, colorThemeItems)
                }
            }

            Dialog(
                title = "��ʾ",
                visible = uninstallState.value,
                cancelButtonText = "ȡ��",
                confirmButtonText = "ȷ��",
                onCancel = {
                    uninstallState.value = false
                },
                onConfirm = {
                    uninstallState.value = false
                    if (targetThemeFileName.value == selectTheme.value) {
                        sendNotice("ж��ʧ�ܣ�", "�޷�ж�ص�ǰ����ʹ�õ�����")
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            colorLoader.uninstallColorTheme(targetThemeFileName.value, targetThemeName.value)
                            colorThemeItems.clear()
                            colorThemeItems.addAll(colorLoader.loadInstalledColorThemes())
                        }
                    }
                },
                content = {
                    Text("�Ƿ�ж�����⡾${targetThemeName.value}��\n�ļ�����${targetThemeFileName.value}?")
                }
            )

            val themeName = remember { mutableStateOf("") }
            val colorPath = remember { mutableStateOf("") }
            Dialog(
                title = "��������",
                visible = generateState.value,
                cancelButtonText = "ȡ��",
                confirmButtonText = "���������ļ�",
                onCancel = {
                    generateState.value = false
                },
                onConfirm = {
                    generateState.value = false
                    if (colorPath.value.trim().isEmpty()) {
                        sendNotice("����ʧ�ܣ�", "������Color�ļ�·��")
                        return@Dialog
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        val colorFile = File(colorPath.value)
                        if (colorFile.exists()) {
                            val light = HashMap<String, String>()
                            val dark = HashMap<String, String>()
                            val kt = String(readBytes(FileInputStream(colorFile)))
                            val list = kt.split("\n").toList().filter { it.contains("val md_theme_") }
                            if (list.isEmpty()) {
                                sendNotice("�������ʧ�ܣ�", "������Ч��Color�ļ�")
                            } else if (themeName.value.trim().isEmpty()) {
                                sendNotice("���ⴴ��ʧ�ܣ�", "�������Ʋ���Ϊ�գ�")
                            } else {
                                list.onEach {
                                        when {
                                            it.contains("light") -> {
                                                light[it.name()] = it.argb()
                                            }
                                            it.contains("dark") -> {
                                                dark[it.name()] = it.argb()
                                            }
                                        }
                                    }
                                try {
                                    val lightColor = light.toColorTheme()
                                    val darkColor = dark.toColorTheme()
                                    val customColorTheme = CustomColorTheme(name = themeName.value, light = lightColor, dark = darkColor)
                                    val json = gson.toJson(customColorTheme)
                                    val fileDialog = FileDialog(JFrame())
                                    fileDialog.file = "${themeName.value}.json"
                                    fileDialog.mode = FileDialog.SAVE
                                    fileDialog.isVisible = true
                                    val path = fileDialog.directory + fileDialog.file
                                    writeBytes(FileOutputStream(path), json.toByteArray())
                                    sendNotice("�������ɳɹ���", "�ѽ��������⡾${themeName.value}��������${fileDialog.directory}Ŀ¼��")
                                    themeName.value = ""
                                    colorPath.value = ""
                                }catch (e: Exception) {
                                    e.printStackTrace()
                                    sendNotice("�����������", "${e.message}")
                                }
                            }
                        }else {
                            sendNotice("��������ʧ�ܣ�", "Ŀ���ļ���${colorPath.value}��������")
                        }
                    }
                },
                content = {
                    GenerateTheme(helpState, themeName, colorPath)
                }
            )

            val contextStore = LocalContext.current
            Dialog(
                title = "����",
                visible = helpState.value,
                cancelButtonText = "֪����",
                confirmButtonText = "��ȡColors�ļ�",
                onCancel = {
                    helpState.value = false
                },
                onConfirm = {
                    helpState.value = false
                    contextStore.startBrowse("https://m3.material.io/theme-builder")
                },
                content = {
                    Text("�����������ɫ�ܹ�����Material Design 3��MD3����ƹ淶������ƣ�Ϊ�˷�����������ߴ������������ļ����������ֱ�ӽ����ɡ�Material Theme Builder�����ɵġ�Color.kt���ļ������Զ�ת�������������ļ���\n\n��ȡColor.kt������·���ȡ��ť��Ȼ������վ��������������ɫ����󵼳�Ϊ��Jetpack Compose����������ص��ļ���ѹ��Ŀ¼��ʹ�ñ��������Color.kt�ļ����ɡ�\n\nС��ʿ���ڡ�Figma����ʹ�á�Material Theme Builder����������ɫ�շ���Ŷ~")
                }
            )
        }
    }

    private fun String.name(): String = this.substring(this.lastIndexOf("_") + 1, this.indexOf("=")).trim()

    private fun String.argb(): String = this.substring(this.indexOf("(") + 1, this.indexOf(")")).replace("0x", "#").replace("0X", "#")

    private fun HashMap<String, String>.toColorTheme(): CustomColorTheme.LocalhostColorTheme = CustomColorTheme.LocalhostColorTheme(
        primary = this["primary"] ?: throw NullPointerException("��ɫ����ʧ��"),
        onPrimary = this["onPrimary"] ?: throw NullPointerException("��ɫ����ʧ��"),
        primaryContainer = this["primaryContainer"] ?: throw NullPointerException("��ɫ����ʧ��"),
        onPrimaryContainer = this["onPrimaryContainer"] ?: throw NullPointerException("��ɫ����ʧ��"),
        secondary = this["secondary"] ?: throw NullPointerException("��ɫ����ʧ��"),
        onSecondary = this["onSecondary"] ?: throw NullPointerException("��ɫ����ʧ��"),
        secondaryContainer = this["secondaryContainer"] ?: throw NullPointerException("��ɫ����ʧ��"),
        onSecondaryContainer = this["onSecondaryContainer"] ?: throw NullPointerException("��ɫ����ʧ��"),
        tertiary = this["tertiary"] ?: throw NullPointerException("��ɫ����ʧ��"),
        onTertiary = this["onTertiary"] ?: throw NullPointerException("��ɫ����ʧ��"),
        tertiaryContainer = this["tertiaryContainer"] ?: throw NullPointerException("��ɫ����ʧ��"),
        onTertiaryContainer = this["onTertiaryContainer"] ?: throw NullPointerException("��ɫ����ʧ��"),
        error = this["error"] ?: throw NullPointerException("��ɫ����ʧ��"),
        errorContainer = this["errorContainer"] ?: throw NullPointerException("��ɫ����ʧ��"),
        onError = this["onError"] ?: throw NullPointerException("��ɫ����ʧ��"),
        onErrorContainer = this["onErrorContainer"] ?: throw NullPointerException("��ɫ����ʧ��"),
        background = this["background"] ?: throw NullPointerException("��ɫ����ʧ��"),
        onBackground = this["onBackground"] ?: throw NullPointerException("��ɫ����ʧ��"),
        outline = this["outline"] ?: throw NullPointerException("��ɫ����ʧ��"),
        inverseOnSurface = this["inverseOnSurface"] ?: throw NullPointerException("��ɫ����ʧ��"),
        inverseSurface = this["inverseSurface"] ?: throw NullPointerException("��ɫ����ʧ��"),
        inversePrimary = this["inversePrimary"] ?: throw NullPointerException("��ɫ����ʧ��"),
        surfaceTint = this["surfaceTint"] ?: throw NullPointerException("��ɫ����ʧ��"),
        outlineVariant = this["outlineVariant"] ?: throw NullPointerException("��ɫ����ʧ��"),
        scrim = this["scrim"] ?: throw NullPointerException("��ɫ����ʧ��"),
        surface = this["surface"] ?: throw NullPointerException("��ɫ����ʧ��"),
        onSurface = this["onSurface"] ?: throw NullPointerException("��ɫ����ʧ��"),
        surfaceVariant = this["surfaceVariant"] ?: throw NullPointerException("��ɫ����ʧ��"),
        onSurfaceVariant = this["onSurfaceVariant"] ?: throw NullPointerException("��ɫ����ʧ��"),
    )

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    private fun GenerateTheme(
        helpState: MutableState<Boolean>,
        themeName: MutableState<String>,
        colorPath: MutableState<String>
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = themeName.value,
                    onValueChange = { themeName.value = it },
                    label = {
                        Text("*��������")
                    },
                    textStyle = MaterialTheme.typography.labelMedium,
                    singleLine = true
                )
                Spacer(modifier = Modifier.size(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TooltipArea(
                        tooltip = {
                            TooltipText {
                                Text("����")
                            }
                        }
                    ) {
                        IconButton(
                            onClick = {
                                helpState.value = true
                            }
                        ) {
                            Icon(Icons.Default.Info, null)
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = colorPath.value,
                    onValueChange = { colorPath.value = it },
                    label = {
                        Text("*���롰Color.kt���ļ�·��")
                    },
                    textStyle = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = {
                        val fileDialog = FileDialog(JFrame())
                        fileDialog.mode = FileDialog.LOAD
                        fileDialog.isVisible = true
                        val path = fileDialog.directory + fileDialog.file
                        if (File(path).exists()) {
                            colorPath.value = path
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ѡ���ļ�")
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ColorThemeItemDefault(
        colorThemeStore: ColorThemeStore,
        selectTheme: MutableState<String>,
        preferencesStore: PreferencesStore,
    ) {
        val darkStore = LocalDark.current
        TooltipArea(
            tooltip = {
                TooltipText {
                    Text("YoHub Color��sRGB��")
                }
            }
        ) {
            Card(
                modifier = Modifier.size(70.dp)
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        selectTheme.value = "YoHub Color"
                        colorThemeStore.colorSchemes = ColorThemeStore.ColorTheme(LightColorScheme, DarkColorScheme)
                        CoroutineScope(Dispatchers.IO).launch {
                            preferencesStore.preference[PreferencesName.COLOR_THEME.toString()] = "YoHub Color"
                            preferencesStore.submit()
                        }
                    },
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (darkStore.darkMode) DarkColorScheme.primary else LightColorScheme.primary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Checkbox(
                        checked = selectTheme.value == "YoHub Color",
                        onCheckedChange = null,
                        enabled = false,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ColorThemeItem(
        colorThemeStore: ColorThemeStore,
        preferencesStore: PreferencesStore,
        selectTheme: MutableState<String>,
        colorThemeItem: ColorLoader.ColorThemeItem,
        uninstallState: MutableState<Boolean>,
        targetTheme: MutableState<String>,
        targetThemeName: MutableState<String>
    ) {
        val darkStore = LocalDark.current
        TooltipArea(
            tooltip = {
                TooltipText {
                    Text("${colorThemeItem.customColorTheme.name}��${colorThemeItem.customColorTheme.type}��")
                }
            }
        ) {
            Card(
                modifier = Modifier.size(70.dp)
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {}
                    .clickable(
                        left = {
                            selectTheme.value = colorThemeItem.fileName
                            colorThemeStore.colorSchemes = ColorThemeStore.ColorTheme(
                                colorThemeItem.customColorTheme.getLightColorScheme(),
                                colorThemeItem.customColorTheme.getDarkColorScheme()
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                preferencesStore.preference[PreferencesName.COLOR_THEME.toString()] =
                                    colorThemeItem.fileName
                                preferencesStore.submit()
                            }
                        },
                        right = {
                            uninstallState.value = true
                            targetTheme.value = colorThemeItem.fileName
                            targetThemeName.value = colorThemeItem.customColorTheme.name
                        }
                    ),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (darkStore.darkMode) colorThemeItem.customColorTheme.getDarkColorScheme().primary else colorThemeItem.customColorTheme.getLightColorScheme().primary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Checkbox(
                        checked = selectTheme.value == colorThemeItem.fileName,
                        onCheckedChange = null,
                        enabled = false,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColorThemeItemInstall(
    colorLoader: ColorLoader,
    colorThemeItems: SnapshotStateList<ColorLoader.ColorThemeItem>
) {
    TooltipArea(
        tooltip = {
            TooltipText {
                Text("��װ����")
            }
        }
    ) {
        OutlinedCard(
            modifier = Modifier.size(70.dp)
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable {
                    CoroutineScope(Dispatchers.IO).launch {
                        colorLoader.installColorTheme()
                        colorThemeItems.clear()
                        colorThemeItems.addAll(colorLoader.loadInstalledColorThemes())
                    }
                },
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(24.dp).align(Alignment.Center))
            }
        }
    }
}

class VersionSetting : NavPage("�汾", isNavigation = false) {

    override fun icon(): () -> Unit = {  }

    @Composable
    override fun content() {
        val contextStore = LocalContext.current
        Column {
            Toolbar(label, enableAnimate = false)
            FluentItem(
                Icons.Default.Open,
                "��Դ��ַ"
            ) {
                TextButton(
                    onClick = {
                        contextStore.startBrowse("https://github.com/lumyuan/yohub-desktop")
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ȥΧ��")
                }
            }
        }
    }

}

class OpenSourceLicense: NavPage("��Դ���", isNavigation = false) {

    override fun icon(): () -> Unit = {  }

    data class LicenseBean(var title: String, var author: String, var tip: String, var url: String)

    private val oss by lazy {
        arrayListOf(
            LicenseBean(
                title = "Compose for Desktop",
                author = "JetBrains",
                tip = "Compose Multiplatform is a declarative framework for sharing UIs across multiple platforms with Kotlin. It is based on Jetpack Compose and developed by JetBrains and open-source contributors.",
                url = "https://github.com/JetBrains/compose-multiplatform"
            ),
            LicenseBean(
                title = "vtools��Scene 4��",
                author = "helloklf",
                tip = "һ�����߼�������Ӧ�ð�װ�Զ������CPU��Ƶ�ȶ������һ��Ĺ����䡣",
                url = "https://github.com/helloklf/vtools"
            ),
            LicenseBean(
                title = "ComposeWindowStyler",
                author = "MayakaApps",
                tip = "Compose Window Styler is a library that lets you style your Compose for Desktop window to have more native and modern UI. This includes styling the window to use acrylic, mica ...etc.",
                url = "https://github.com/MayakaApps/ComposeWindowStyler"
            ),
            LicenseBean(
                title = "compose-fluent-ui",
                author = "Konyaco",
                tip = "Fluent Design UI library for Compose Multiplatform",
                url = "https://github.com/Konyaco/compose-fluent-ui"
            ),
            LicenseBean(
                title = "gson",
                author = "google",
                tip = "A Java serialization/deserialization library to convert Java Objects into JSON and back",
                url = "https://github.com/google/gson"
            ),
            LicenseBean(
                title = "jna",
                author = "java-native-access",
                tip = "Java Native Access (JNA)",
                url = "https://github.com/java-native-access/jna"
            ),
            LicenseBean(
                title = "karavel",
                author = "AppOutlet",
                tip = "Lightweight navigation library for Compose for Desktop",
                url = "https://github.com/AppOutlet/karavel"
            ),
        )
    }

    @Composable
    override fun content() {
        val contextStore = LocalContext.current
        FluentFold(
            icon = Icons.Default.Code,
            title = label
        ) {
            NavigationItemSeparator(modifier = Modifier.padding(bottom = 4.dp))

            oss.onEach {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {  },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp)
                    ) {
                        Text(it.title, style = MaterialTheme.typography.bodyLarge)
                        Divider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                        Text(it.tip, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(it.author, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f))
                    Spacer(modifier = Modifier.size(8.dp))
                    IconButton(
                        onClick = {
                            contextStore.startBrowse(it.url)
                        }
                    ) {
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
        }
    }

}