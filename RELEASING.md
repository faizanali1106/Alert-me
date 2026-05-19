# How to publish a GitHub Release (APK)

Your repo can build the APK automatically on GitHub when you create a version tag.

## Option A — Automatic (recommended)

1. Push the latest code to GitHub (`main` branch).
2. On GitHub, open **Actions** and confirm the workflow **Build and release APK** exists.
3. Create a tag and push it:

   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

4. Wait 3–5 minutes. GitHub Actions will build the APK and attach it to a new **Release**.
5. Open **Releases** — you should see `v1.0.0` with `Alert-me-v1.0.0.apk` to download.

You can also run the workflow manually: **Actions → Build and release APK → Run workflow**.

> Note: Tag pushes (`v*`) create a Release with the APK. Manual runs only upload an artifact under Actions.

## Option B — Upload APK yourself (Android Studio)

1. In Android Studio: **Build → Build APK(s)** (or **Generate Signed Bundle / APK** for release).
2. Find the APK at `app/build/outputs/apk/debug/app-debug.apk`.
3. On GitHub: **Releases → Draft a new release**.
4. Tag: `v1.0.0`, Title: `v1.0.0`, upload the `.apk` file.
5. Click **Publish release**.

## Release notes template

```markdown
## Alert-me v1.0.0

- Relay incoming calls and SMS alerts via ntfy (iPhone, Android, or any ntfy client)
- Optional caller/sender names (no message body)
- Light/dark theme and setup checklist

### Install
1. Download `Alert-me-v1.0.0.apk` below
2. Allow install from browser/files if asked
3. Follow the README setup (Alert-me on call/SMS phone + ntfy on receiver device, same topic)
```
