# Alert-me

**Get call and text alerts on another phone (or tablet) when they hit your Android.**

Free, open-source Android app that forwards **incoming call** and **new text** alerts to any device running [ntfy](https://ntfy.sh) — **iPhone**, a **second Android**, or the same phone for testing.

<p align="center">
  <a href="https://github.com/faizanali1106/Alert-me/releases/latest/download/alerter.apk">
    <img src="https://img.shields.io/badge/Download%20APK-0D9488?style=for-the-badge&logo=android&logoColor=white" alt="Download APK">
  </a>
</p>

<p align="center">
  <a href="https://github.com/faizanali1106/Alert-me/releases/latest/download/alerter.apk"><strong>⬇️ Direct download: alerter.apk</strong></a>
  &nbsp;·&nbsp;
  <a href="https://github.com/faizanali1106/Alert-me/releases/latest">All releases</a>
  &nbsp;·&nbsp;
  <a href="LICENSE">MIT License</a>
</p>

## Features

- **Call alerts** — know when someone is calling your Android
- **Text alerts** — new SMS notification only (message body is never sent)
- **Show who** (optional) — caller/sender name or number on the device running ntfy
- **Generic mode** — alert without names or numbers
- **Works with any ntfy client** — iPhone, second Android, or same phone (testing)
- **Private** — your own secret ntfy topic over HTTPS
- **Light & dark theme** — setup checklist and test buttons

## Download (Alert-me on the phone that receives calls)

1. Tap **[Download APK](https://github.com/faizanali1106/Alert-me/releases/latest/download/alerter.apk)** (starts download immediately)
2. Allow install from your browser/files app if prompted
3. Install on the **Android phone that gets the calls and texts**
4. Follow setup below

> **Latest:** [v1.0.0](https://github.com/faizanali1106/Alert-me/releases/tag/v1.0.0) · If the button does not work on mobile, open [Releases](https://github.com/faizanali1106/Alert-me/releases/latest) and tap `alerter.apk`.

## How it works

```
Android (calls/texts)  →  Alert-me  →  ntfy.sh (your topic)  →  ntfy app (any phone)
```

| Role | App | Where |
|------|-----|--------|
| **Sender** | **Alert-me** | Android phone that receives calls & SMS |
| **Receiver** | **ntfy** | iPhone, another Android, etc. (subscribed to same topic) |

Message **content** is never read or sent — only caller/sender from the system notification (or contacts lookup).

## Common setups

| Setup | Works? |
|-------|--------|
| Android (calls) → **iPhone** (ntfy) | ✅ Ideal for two phones |
| Android (calls) → **second Android** (ntfy) | ✅ Same as iPhone |
| Android (calls) → **same Android** (ntfy) | ✅ Good for testing; redundant for real calls |
| **Only ntfy**, no Alert-me | ❌ Nothing publishes call/SMS events to your topic |

## Alert types

| Mode | What ntfy shows |
|------|-----------------|
| **Show who** (default, on) | `Call from: Ali Khan` · `Text from: +92 300…` |
| **Generic** (toggle off) | Someone is calling / texting (no names) |

## Receiver setup — ntfy (one time)

Install **ntfy** on the device where you want alerts, then subscribe to the **same secret topic** you enter in Alert-me.

### iPhone

1. Install [ntfy from the App Store](https://apps.apple.com/app/ntfy/id1625396347)
2. **Subscribe to topic** → long random name (e.g. `relay-x8k2m9q4-private`)
3. Allow notifications

### Android (second phone, or same phone for tests)

1. Install [ntfy from Google Play](https://play.google.com/store/apps/details?id=io.ntfy.app) or [F-Droid](https://f-droid.org/packages/io.ntfy.app/)
2. **Subscribe to topic** → **same** topic name as in Alert-me
3. Allow notifications

## Alert-me setup (phone that gets calls & texts)

1. Install the APK from the [direct download](https://github.com/faizanali1106/Alert-me/releases/latest/download/alerter.apk) link above
2. Enter the **same ntfy topic** → **Save topic**
3. Turn **Show who is calling or texting** on or off
4. **Grant phone access** and **Enable notification access** (required for calls + texts)
5. **Grant contacts** (optional — saved names instead of numbers only)
6. **Battery optimization** → unrestricted for this app
7. **Enable relay** → run **Test** buttons and check the ntfy app on your receiver device

## Build from source

**Requirements:** Android Studio, JDK 17

1. Clone this repo
2. Open in Android Studio
3. **Build → Build APK(s)** or **Generate Signed Bundle / APK** for release

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## Privacy

- With details on: names/numbers go to **your** ntfy topic over HTTPS
- Use a **long random topic** — anyone who knows the topic can subscribe
- SMS/message body is **not** sent

## Troubleshooting

- **No alert on receiver:** Check topic matches on Alert-me and ntfy; relay enabled; internet works
- **No name on calls:** Grant **notification access** on the Alert-me phone
- **Number instead of name:** Grant **contacts** on the Alert-me phone
- **Missed texts:** Notification access on; SMS app must show notifications

## License

MIT License — see [LICENSE](LICENSE).
