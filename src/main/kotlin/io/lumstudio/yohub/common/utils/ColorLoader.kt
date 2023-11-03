package io.lumstudio.yohub.common.utils

import com.google.gson.Gson
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.runtime.InstallThemesPathStore
import io.lumstudio.yohub.theme.ColorThemeStore
import io.lumstudio.yohub.theme.CustomColorTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.io.File
import java.io.FileInputStream
import java.io.FilenameFilter
import javax.swing.JFrame

class ColorLoader(
    private val preferencesStore: PreferencesStore,
    private val installThemesPathStore: InstallThemesPathStore,
    private val colorThemeStore: ColorThemeStore
) {

    private val gson by lazy { Gson() }

    suspend fun loadInstalledColorTheme(themeName: String) : ColorThemeStore.ColorTheme = withContext(Dispatchers.IO) {
        val theme = String(readBytes(FileInputStream(File(installThemesPathStore.installPathFile, themeName))))
        val customColorTheme = gson.fromJson(theme, CustomColorTheme::class.java)
        ColorThemeStore.ColorTheme(customColorTheme.getLightColorScheme(), customColorTheme.getDarkColorScheme())
    }

    data class ColorThemeItem(
        val fileName: String,
        val customColorTheme: CustomColorTheme
    )

    suspend fun loadInstalledColorThemes(): List<ColorThemeItem> = withContext(Dispatchers.IO) {
        val customColorThemes = ArrayList<ColorThemeItem>()
        try {
            (installThemesPathStore.installPathFile.listFiles() ?: arrayOf<File>())
                .filter { it.name.lowercase().endsWith(".json") }
                .sortedBy { it.lastModified() }
                .onEach {
                    try {
                        val theme = String(readBytes(FileInputStream(it)))
                        val customColorTheme = gson.fromJson(theme, CustomColorTheme::class.java)
                        customColorThemes.add(
                            ColorThemeItem(
                                fileName = it.name,
                                customColorTheme
                            )
                        )
                    }catch (e: Exception) {
                        e.printStackTrace()
                        sendNotice("�����������", "�����ļ���${it.name}������ʧ�ܣ�${e.message}")
                        it.delete()
                    }
                }
        }catch (e: Exception) {
            e.printStackTrace()
        }
        customColorThemes
    }

    suspend fun installColorTheme() = withContext(Dispatchers.IO) {
        val fileDialog = FileDialog(JFrame())
        fileDialog.filenameFilter = FilenameFilter { _, name -> name.endsWith(".json") }
        fileDialog.mode = FileDialog.LOAD
        fileDialog.isVisible = true
        if (fileDialog.file?.endsWith(".json") == true) {
            val targetPath = fileDialog.directory + fileDialog.file
            try {
                val theme = String(readBytes(FileInputStream(targetPath)))
                val colorTheme = gson.fromJson(theme, CustomColorTheme::class.java)
                colorTheme.getLightColorScheme()
                FileCopyUtils.copyFile(File(targetPath), File(installThemesPathStore.installPathFile, fileDialog.file))
                sendNotice("��װ�ɹ���", "�ɹ���װ���⡾${fileDialog.file}��")
            }catch (e: Exception) {
                e.printStackTrace()
                sendNotice("��װʧ�ܣ�", "�����ļ���${fileDialog.file}������ʧ�ܣ�${e.message}")
            }
        } else if (fileDialog.file != null) {
            sendNotice("ѡ��ʧ��", "����֧�ֵ��ļ����ͣ�${fileDialog.file}")
        }
    }

    suspend fun uninstallColorTheme(themeFileName: String, themeName: String) = withContext(Dispatchers.IO) {
        if (File(installThemesPathStore.installPathFile, themeFileName).delete()) {
            sendNotice("ж�سɹ���", "�ѽ������ļ���$themeName��ж��")
        }else {
            sendNotice("ж��ʧ�ܣ�", "�����ļ���$themeFileName�����ܲ�����")
        }
    }
}