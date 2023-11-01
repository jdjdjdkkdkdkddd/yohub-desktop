package io.lumstudio.yohub.windows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.DeviceEq
import com.konyaco.fluent.icons.regular.Power
import io.lumstudio.yohub.common.LocalIOCoroutine
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.runtime.*
import io.lumstudio.yohub.ui.component.Dialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.skiko.hostOs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceScreen() {
    val driverStore = LocalDriver.current
    val fastbootDriverStore = LocalFastbootDriverRuntime.current
    val deviceStore = LocalDevice.current
    val devicesStore = LocalDevices.current
    val keepShellStore = LocalKeepShell.current
    val ioCoroutine = LocalIOCoroutine.current
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp)
    ) {
        val selectDevice = deviceStore.device
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            when {
                hostOs.isWindows -> InfoItem(
                    onClick = {
                        if (!driverStore.isInstall) {
                            CoroutineScope(Dispatchers.IO).launch {
                                fastbootDriverStore.install()
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.DeviceEq, null, modifier = Modifier.fillMaxSize())
                    }
                ) {
                    Text("����״̬��${if (driverStore.isInstall) "����" else "�쳣������޸���"}")
                }
            }

            InfoItem(
                onClick = {},
                icon = {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.Android,
                        null,
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                var label by remember { mutableStateOf("δѡ���豸") }
                var sub by remember { mutableStateOf("") }
                val device = selectDevice

                if (devicesStore.devices.isEmpty()) {
                    label = "δ�����豸"
                    sub = ""
                } else if (device == null) {
                    label = "δѡ���豸"
                    sub = ""
                } else {
                    label = "�����ӣ�${device.id}"
                    sub = "�豸���ͣ�${device.type}"
                }

                Column {
                    Text(label)
                    if (sub.isNotEmpty()) {
                        Text(sub, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            var contentText by remember { mutableStateOf("") }
            var displayDialog by remember { mutableStateOf(false) }
            var onConfirm by remember { mutableStateOf({ }) }

            AnimatedVisibility(
                selectDevice != null
                        && selectDevice.state != ClientState.UNAUTHORIZED
                        && (selectDevice.type == ClientType.ADB
                        || selectDevice.type == ClientType.ADB_AB
                        || selectDevice.type == ClientType.ADB_VAB)
            ) {
                InfoItem(
                    onClick = {
                        displayDialog = true
                        contentText = "ȷ��Ҫ������${selectDevice?.id}����"
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore adb "reboot"
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text("�����豸")
                }
            }

            AnimatedVisibility(
                selectDevice != null
                        && selectDevice.state != ClientState.UNAUTHORIZED
                        && (selectDevice.type == ClientType.ADB
                        || selectDevice.type == ClientType.ADB_AB
                        || selectDevice.type == ClientType.ADB_VAB)
            ) {
                InfoItem(
                    onClick = {
                        displayDialog = true
                        contentText = "ȷ��Ҫ���豸��${selectDevice?.id}���ػ���"
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore adb "reboot p"
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text("�豸�ػ�")
                }
            }

            AnimatedVisibility(
                selectDevice != null
                        && selectDevice.state != ClientState.UNAUTHORIZED
                        && (selectDevice.type == ClientType.ADB
                        || selectDevice.type == ClientType.ADB_AB
                        || selectDevice.type == ClientType.ADB_VAB)
            ) {
                InfoItem(
                    onClick = {
                        displayDialog = true
                        contentText = "ȷ��Ҫ����${selectDevice?.id}��������Bootloader��"
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore adb "reboot bootloader"
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text("������Bootloader")
                }
            }

            AnimatedVisibility(
                selectDevice != null
                        && selectDevice.state != ClientState.UNAUTHORIZED
                        && (selectDevice.type == ClientType.ADB
                        || selectDevice.type == ClientType.ADB_AB
                        || selectDevice.type == ClientType.ADB_VAB)
            ) {
                InfoItem(
                    onClick = {
                        displayDialog = true
                        contentText = "ȷ��Ҫ����${selectDevice?.id}��������Recovery��"
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore adb "reboot recovery"
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text("������Recovery")
                }
            }

            AnimatedVisibility(
                selectDevice != null
                        && selectDevice.state != ClientState.UNAUTHORIZED
                        && (selectDevice.type == ClientType.FASTBOOT)
            ) {
                InfoItem(
                    onClick = {
                        displayDialog = true
                        contentText = "ȷ��Ҫ������${selectDevice?.id}����"
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore fastboot "reboot"
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text("�����豸")
                }
            }

            AnimatedVisibility(
                selectDevice != null
                        && selectDevice.state != ClientState.UNAUTHORIZED
                        && (selectDevice.type == ClientType.FASTBOOT)
            ) {
                InfoItem(
                    onClick = {
                        displayDialog = true
                        contentText = "ȷ��Ҫ����${selectDevice?.id}��������Recovery��"
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore fastboot "reboot recovery"
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text("������Recovery")
                }
            }

            Dialog(
                title = "��ʾ",
                visible = displayDialog,
                cancelButtonText = "ȡ��",
                confirmButtonText = "ȷ��",
                onCancel = {
                    displayDialog = false
                },
                onConfirm = {
                    displayDialog = false
                    onConfirm()
                },
                content = {
                    Text(contentText)
                }
            )
        }
    }
}

@Composable
private fun InfoItem(
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.height(65.dp).padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(.5.dp, DividerDefaults.color.copy(alpha = .5f))
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) Box(Modifier.padding(start = 16.dp).size(28.dp), Alignment.Center) {
                icon()
            }
            Row(
                modifier = Modifier.padding(
                    start = 12.dp,
                    end = 16.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                content()
            }
        }
    }
}