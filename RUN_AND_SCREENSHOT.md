# macOS 运行、截图与提交手册

工程已经在 Android 14 模拟器成功运行并生成五张真实截图。以下步骤用于老师复查或在另一台 Mac 上重新操作；无需修改源码。

## 1. 打开与同步工程

1. 启动 Android Studio，选择 **Open**，选中 `Android-Cross-App-Broadcast-Homework` 文件夹（里面有 `settings.gradle`），不要只打开 `app-a` 子目录。
2. 选择信任工程。首次打开会下载 Gradle 和 Android 构建插件，等待右下角进度完成。
3. 在 **Settings → Languages & Frameworks → Android SDK** 中安装 Android 14 / API 34 平台、Android SDK Build-Tools 36.0.0（AGP 使用的工具版本）和 Android SDK Platform-Tools。SDK 路径通常为 `~/Library/Android/sdk`。
4. 在 **Settings → Build, Execution, Deployment → Build Tools → Gradle** 中，把 Gradle JDK 设置为 Android Studio 自带的 `jbr`（本机为 Java 25）。工程使用 Gradle 9.3.1 / Android Gradle Plugin 9.1.1，JDK 至少 17；建议直接用当前 Android Studio 自带 JDK。本工程 Java 源码兼容级别为 17。
5. 若提示找不到 SDK，在 SDK Location 中选择本机 SDK。`local.properties` 是本机文件，已被 Git 忽略，不需要上传。
6. 点击 **File → Sync Project with Gradle Files**。出现下载失败先检查当前网络/现有代理，再点击重试；不要改 GitHub 认证。

## 2. 创建并启动模拟器

1. 打开 **Tools → Device Manager**，点击 **Create Device**，选择 Pixel 3a 或其他手机。
2. 选择 Android 14 / API 34 的 Google APIs 系统镜像；Apple Silicon Mac 选择 ARM64。
3. 点击 Finish，然后点击设备右侧运行三角形。等待手机桌面出现。
4. 若已有设备，可直接启动已有模拟器；也可用开启 USB 调试的真机，并确认手机上的调试授权。

## 3. 分别安装 A、B

1. Android Studio 顶部运行配置中选 `app-a`，设备选择刚启动的模拟器，点击绿色运行按钮。桌面出现 **A · 活动重置**。
2. 切换运行配置为 `app-b`，使用同一设备点击运行。桌面出现 **B · 广播发送**。
3. 如果运行配置尚未生成，先完成 Gradle 同步，再打开 **Run → Edit Configurations → + → Android App**，分别选择两个 module。
4. 本仓库也提供 `apks/` 中已构建的两个 debug APK，可以拖到同一模拟器中安装。请成对安装，避免不同电脑独立构建导致签名权限不一致。遇到“签名不匹配”时，只卸载本作业这两个 App 后重装配套 APK；卸载会删除它们的演示资料。

## 4. 手动验证并拍摄五张截图

模拟器右侧工具栏的相机按钮 **Take Screenshot** 可以直接保存真实屏幕。保存时按下表命名，放到仓库根目录的 `screenshots/` 中。

| 步骤 | 操作 | 文件名 |
| --- | --- | --- |
| 1 | 打开 A，账号输入 `student2026`，密码输入 `Demo123456`，收起键盘，勾选“显示演示密码” | `01-login.png` |
| 2 | 点击登录，看到欢迎账号及功能首页 | `02-home.png` |
| 3 | 点击“打开详情页”，看到第三个 Activity | `03-detail.png` |
| 4 | 按系统主页键（不要退出 A），打开 B，输入如“课程广播测试：请重新登录 TEST-01”，收起键盘；发送前截图 | `04-sender.png` |
| 5 | 点击“发送广播并重置 A”，等待 A 自动出现，确认账号、密码和上述消息均显示 | `05-reset.png` |

最后按系统返回键：不能回到旧的首页/详情页。再次从桌面打开 A，消息应持续存在。重新打开 B，可以查看“发送成功”的回执反馈；再发送另一条消息，A 应显示新消息。

Android 12 以上根 Activity 按返回键可能退到桌面而不是销毁自身，这是系统行为；验证重点是旧 Home/Detail 不可恢复。不要把系统桌面返回理解为失败。

README 已用 `![说明](screenshots/文件名.png)` 直接嵌入五张图。保持同名覆盖即可更新展示，不用修改 README 路径；若改名，记得同步修改 README 的图片路径。上传前确保图片是模拟器或手机实际运行截取的 PNG。

## 5. 可复制的终端命令

打开终端后，先进入实际工程目录（若不是下面的位置，请替换 `cd` 路径）：

```bash
cd '/Users/tianxinrui/Desktop/大三上/Android/Android-Cross-App-Broadcast-Homework'
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$PATH"
chmod +x gradlew
./gradlew :app-a:assembleDebug :app-b:assembleDebug :app-a:lintDebug :app-b:lintDebug
adb devices -l
adb install -r app-a/build/outputs/apk/debug/app-a-debug.apk
adb install -r app-b/build/outputs/apk/debug/app-b-debug.apk
adb shell am start -n com.example.broadcast.receiver/.LoginActivity
```

先在 A 完成账号输入与三层页面操作，再打开 B：

```bash
adb shell input keyevent KEYCODE_HOME
adb shell am start -n com.example.broadcast.sender/.SenderActivity
```

在对应画面下分别执行以下截图命令（**每条命令应在其对应页面打开时单独运行，不要连续一次执行五条**）：

```bash
mkdir -p screenshots
adb exec-out screencap -p > screenshots/01-login.png
adb exec-out screencap -p > screenshots/02-home.png
adb exec-out screencap -p > screenshots/03-detail.png
adb exec-out screencap -p > screenshots/04-sender.png
adb exec-out screencap -p > screenshots/05-reset.png
```

查看 A 的 Activity 栈（广播前应有三项，重置后只剩 Login）：

```bash
adb shell dumpsys activity activities | rg 'Hist.*com.example.broadcast.receiver'
adb logcat -d -s BroadcastHomework:I '*:S'
```

没有安装 `rg` 时，可用 `grep 'Hist.*com.example.broadcast.receiver'` 替换。两个 APK 使用签名权限，不使用 `adb shell am broadcast` 冒充 B 发送，因为 shell 并不具备应用签名权限。

自动执行完整演示、额外验证并重新生成截图（需要 Python 3；会清空**本作业**两个 App 的演示数据，不影响其他 App）：

```bash
python3 tools/verify_device.py
```

同时连接多个设备时，请先只保留一个设备，或设置 `ANDROID_SERIAL=emulator-5554` 为希望使用的设备序列号。

## 6. GitHub 发布

本地完整 Git 工程已保留。目标是一个新的公开仓库 `Android-Cross-App-Broadcast-Homework`，不会覆盖原来的 Android 作业。

按照本次会话的 GitHub 操作规则，**创建远程仓库、修改 remote 和 push 之前，需要用户明确确认**。得到确认后，可在本工程目录执行：

```bash
gh api user --jq .login
gh repo create Android-Cross-App-Broadcast-Homework --public --source=. --remote=origin --description 'Android 课程作业：双 App 显式有序广播与 Activity 重置'
git push -u origin main
gh repo view --json url --jq .url
```

若远程仓库已存在，不重复创建、更不覆盖；先检查仓库所有权和本次工程来源。如果已经配置好本项目的 `origin`，只需在确认后执行 `git push -u origin main`。重新截图或改代码后，使用 `git add .` 和 `git commit -m "Update homework evidence"` 创建本地提交，再按规则确认推送。

沿用本机现有代理和 GitHub 凭据。连接失败时先看错误是代理/网络/DNS/TLS 还是认证问题；不运行 `gh auth login/logout/refresh`，不读取 Token，不修改系统或 Git 全局代理。Git 远程操作沿用原 remote；若 SSH 网络失败，可经诊断后仅对本条命令使用等价 HTTPS URL，并使用已有 credential helper，不持久化切换协议。
