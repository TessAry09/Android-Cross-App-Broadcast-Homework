#!/usr/bin/env python3
"""Real-device UI regression and screenshots. Clears ONLY this project's A/B demo data.
Run after installing both debug APKs on one device. Python 3 standard library only.
"""
from pathlib import Path
import os, re, subprocess, time, xml.etree.ElementTree as ET
ROOT = Path(__file__).resolve().parents[1]
ADB = os.environ.get('ADB', str(Path.home() / 'Library/Android/sdk/platform-tools/adb'))
A = 'com.example.broadcast.receiver'
B = 'com.example.broadcast.sender'
REPORT = []

def adb(*args, binary=False):
    result = subprocess.run([ADB, *args], capture_output=True, timeout=35, check=True)
    return result.stdout if binary else result.stdout.decode('utf-8', errors='replace')

def shell(*args): return adb('shell', *args)
def note(text):
    REPORT.append(text)
    print(text, flush=True)
def launch(pkg, name):
    shell('am', 'start', '-W', '-n', pkg + '/.' + name)
    time.sleep(1)
    hide_keyboard()
def ui():
    shell('uiautomator', 'dump', '/sdcard/broadcast-homework-window.xml')
    return ET.fromstring(shell('cat', '/sdcard/broadcast-homework-window.xml'))
def find(ident, pkg=A):
    for node in ui().iter('node'):
        if node.get('resource-id') == pkg + ':id/' + ident: return node
    raise AssertionError('UI element missing: ' + ident)
def tap(ident, pkg=A):
    node = find(ident, pkg)
    x1,y1,x2,y2 = map(int, re.findall(r'\d+', node.get('bounds')))
    shell('input', 'tap', str((x1+x2)//2), str((y1+y2)//2))
    time.sleep(.4)
def enter(ident, text, pkg=A):
    tap(ident, pkg)
    shell('input', 'text', text)
def hide_keyboard():
    if 'mInputShown=true' in shell('dumpsys', 'input_method'):
        shell('input', 'keyevent', 'KEYCODE_BACK')
    time.sleep(.5)
def capture(name):
    hide_keyboard()
    (ROOT/'screenshots'/name).write_bytes(adb('exec-out', 'screencap', '-p', binary=True))
    note('SCREENSHOT screenshots/' + name)
def stack(label):
    result = shell('dumpsys', 'activity', 'activities')
    records = [line.strip() for line in result.splitlines() if re.search(r'Hist\s+#\d+:.*' + re.escape(A), line)]
    (ROOT/'evidence'/(label+'.txt')).write_text(('\n'.join(records) if records else 'No A Activity records.')+'\n')
    return records

def assert_reset(expected):
    assert find('account').get('text') == 'student2026'
    assert find('password').get('text') == 'Demo123456'
    assert find('message').get('text') == expected
    records = stack('reset-task')
    assert len(records) == 1 and '.LoginActivity' in records[0], records
    note('PASS reset: retained account/password, exact broadcast message, only Login in A task')

(ROOT/'screenshots').mkdir(exist_ok=True)
(ROOT/'evidence').mkdir(exist_ok=True)
try:
    note('Device: Android ' + shell('getprop','ro.build.version.release').strip() + ' / API ' + shell('getprop','ro.build.version.sdk').strip())
    for pkg in [A,B]: shell('pm','clear',pkg)
    shell('logcat','-c')
    launch(A,'LoginActivity')
    enter('account','student2026')
    enter('password','Demo123456')
    hide_keyboard()
    tap('show_password')
    capture('01-login.png')
    tap('login')
    capture('02-home.png')
    tap('open_detail')
    capture('03-detail.png')
    records = stack('before-reset-task')
    assert len(records) == 3, records
    assert all(any('.'+name in row for row in records) for name in ['LoginActivity','HomeActivity','DetailActivity'])
    note('PASS actual task stack: Login -> Home -> Detail (3 Activity records)')
    shell('input','keyevent','KEYCODE_HOME')
    launch(B,'SenderActivity')
    tap('send_message', B)
    shell('input','keyevent','KEYCODE_MOVE_END')
    shell('input','text','%sTEST-2026-10-24-01')
    hide_keyboard()
    expected = find('send_message',B).get('text')
    capture('04-sender.png')
    tap('send', B)
    time.sleep(1)
    assert_reset(expected)
    capture('05-reset.png')
    lifecycle = shell('logcat','-d','-s','BroadcastHomework:I','*:S')
    assert 'finishAll count=3' in lifecycle, lifecycle
    for name in ['LoginActivity','HomeActivity','DetailActivity']:
        assert 'destroy '+name in lifecycle, lifecycle
    note('PASS receiver log: finishAll count=3; all three old Activities destroyed')
    shell('input','keyevent','KEYCODE_BACK')
    time.sleep(1)
    records = stack('after-back-task')
    assert not any('.HomeActivity' in row or '.DetailActivity' in row for row in records), records
    focused = shell('dumpsys','activity','activities')
    resumed = '\n'.join(line for line in focused.splitlines() if 'ResumedActivity' in line)
    assert A not in resumed, resumed
    note('PASS Back: no old Home/Detail and A no longer resumed')
    launch(A,'LoginActivity')
    assert_reset(expected)
    note('PASS message remains visible when A is reopened')
    # Background process death (am kill, not force-stop): manifest receiver must restart it.
    shell('input','keyevent','KEYCODE_HOME')
    launch(B,'SenderActivity')
    assert '发送成功' in find('send_status',B).get('text')
    shell('am','kill',A)
    time.sleep(1)
    assert not subprocess.run([ADB, 'shell', 'pidof', A], capture_output=True).stdout.strip(), 'A process still running'
    tap('send_message',B)
    shell('input','keyevent','KEYCODE_MOVE_END')
    shell('input','text','%sSECOND')
    hide_keyboard()
    second = find('send_message',B).get('text')
    tap('send',B)
    time.sleep(1)
    assert_reset(second)
    note('PASS background process death: manifest receiver restarted A, new message replaced old')
    # Keep primary five screenshots from the first sequence; UI ends with a valid second reset.
    (ROOT/'evidence'/'lifecycle.log').write_text(shell('logcat','-d','-s','BroadcastHomework:I','*:S'))
    note('ALL CHECKS PASSED')
finally:
    (ROOT/'evidence'/'device-verification.txt').write_text('\n'.join(REPORT)+'\n')
