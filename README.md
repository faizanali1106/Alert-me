# Alert-me

**Get call and text alerts on your iPhone when they hit your Android phone.**

Free, open-source Android app that forwards **incoming call** and **new text** alerts to your iPhone using [ntfy](https://ntfy.sh).

[![Download latest APK](https://img.shields.io/github/v/release/faizanali1106/Alert-me?label=Download%20APK)](https://github.com/faizanali1106/Alert-me/releases/latest)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## Features

- **Call alerts** — know when someone is calling your Android
- **Text alerts** — new SMS notification only (message body is never sent)
- **Show who** (optional) — caller/sender name or number on iPhone
- **Generic mode** — alert without names or numbers
- **Private** — your own secret ntfy topic over HTTPS
- **Light & dark theme** — setup checklist and test buttons

## Download

1. Open **[Releases](https://github.com/faizanali1106/Alert-me/releases/latest)** and download the latest `.apk`
2. Allow install from your browser/files app if prompted
3. Install and follow setup below

> No release yet? Build from source (see below) or check back for an uploaded APK.

## How it works

```
Android (call/text) → Alert-me → ntfy.sh (your topic) → ntfy app on iPhone
```

## Alert types

| Mode | iPhone sees |
|------|-------------|
| **Show who** (default, on) | `Call from: Ali Khan` · `Text from: +92 300…` |
| **Generic** (toggle off) | Someone is calling / texting (no names) |

Message **content** is never read or sent — only sender/caller from the system notification (or contacts lookup).

## iPhone setup (one time)

1. Install **ntfy** from the App Store (free).
2. **Subscribe to topic** → enter a long secret name (e.g. `relay-x8k2m9q4-private`).
3. Allow notifications.

## Android setup

1. Install the APK from [Releases](https://github.com/faizanali1106/Alert-me/releases/latest).
2. Same **ntfy topic** → **Save topic**.
3. Turn **Show who is calling or texting** on or off.
4. **Grant phone access** and **Enable notification access** (needed for calls + texts).
5. **Grant contacts** (optional — shows saved names instead of only numbers).
6. **Battery optimization** → unrestricted.
7. **Enable relay** → run **Test** buttons.

## Build from source

**Requirements:** Android Studio, JDK 17

1. Clone this repo
2. Open in Android Studio
3. **Build → Build APK(s)** or **Generate Signed Bundle / APK** for release

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## Privacy

- With details on: names/numbers go to **your** ntfy topic over HTTPS.
- Use a **long random topic** — anyone with the topic can subscribe.
- SMS/message body is **not** sent.

## Troubleshooting

- **No name on calls:** Grant **notification access**; depends on Phone app notification.
- **Number instead of name:** Grant **contacts** permission.
- **Missed texts:** Enable notification access; SMS app must show notifications.

## License

MIT License — see [LICENSE](LICENSE).
