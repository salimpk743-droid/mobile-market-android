# Mobile Market — publish guide

Package: `pk.mobilemarket.app`  
Website (TWA): `https://market-place-six-chi.vercel.app/`  
Architecture: Trusted Web Activity (Chrome Custom Tabs). **Not a WebView.**

Previous upload keys and password files are **retired**. Generate a new upload keystore on your machine. Do not reuse any password from an old zip.

---

## A. Open and build

1. Install the latest stable [Android Studio](https://developer.android.com/studio).
2. SDK Manager → install **Android 16 (API 36)** platform + Build-Tools 36.
3. **File → Open** the `android-app` folder (this folder).
4. Let Gradle sync (AGP 8.11.1, Gradle 8.13, Kotlin 2.1.10).

`compileSdk` and `targetSdk` are **36**. `minSdk` is **26**.

## B. Run a debug build

1. Plug in a phone or start an emulator.
2. Click **Run**. Debug uses application id `pk.mobilemarket.app.debug`, so Play Digital Asset Links will **not** apply (that is intentional).
3. Debug still opens the live website in a Custom Tab / TWA-like session.

Command (if you use the wrapper):

```
./gradlew assembleDebug
```

## C. Generate a new upload keystore (do this once, keep it private)

In a terminal **on your computer**:

```
keytool -genkeypair -v \
  -keystore upload-keystore.jks \
  -storetype PKCS12 \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias upload
```

Choose a strong password. Store the `.jks` and password in a password manager.  
Put the file at `keystore/upload-keystore.jks` (that folder is gitignored).

**Do not** email the keystore. **Do not** commit it. **Do not** put `password.txt` in this project.

## D. Configure signing locally (never commit)

Copy `keystore.example.properties` → `keystore/keystore.properties` and fill:

```
storeFile=../keystore/upload-keystore.jks
storePassword=YOUR_PASSWORD
keyAlias=upload
keyPassword=YOUR_PASSWORD
```

Or set environment variables instead:

- `MM_STORE_FILE`
- `MM_STORE_PASSWORD`
- `MM_KEY_ALIAS`
- `MM_KEY_PASSWORD`

`keystore.properties`, `*.jks`, `*.keystore`, `password.txt` are gitignored.

You can also skip the properties file and enter the keystore in Android Studio when generating the bundle.

## E. Generate the release Android App Bundle

Android Studio: **Build → Generate Signed App Bundle / APK → Android App Bundle**.

Or, with local secrets configured:

```
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

Play Console accepts **AAB**, not a raw APK, for new listings.

## F. Obtain the SHA-256 fingerprint

Upload key (the `.jks` you created):

```
keytool -list -v -keystore upload-keystore.jks -alias upload
```

Copy the **SHA256:** line (`AA:BB:CC:...`).

If **Google Play App Signing** is on (default): after the first AAB upload, open Play Console → **Setup → App signing** and copy **App signing key certificate** SHA-256. **That** is the fingerprint the live phone uses. The upload key is only for sending the bundle to Google.

## G. Configure `assetlinks.json`

File served at:

`https://market-place-six-chi.vercel.app/.well-known/assetlinks.json`

Template (`assetlinks.json.example` in this folder):

```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "pk.mobilemarket.app",
      "sha256_cert_fingerprints": [
        "PASTE_SHA256_HERE"
      ]
    }
  }
]
```

Replace `PASTE_SHA256_HERE` with the **production** SHA-256 (Play App Signing cert once enrolled). You may list both upload and Play certs as two strings in that array.

Must be:

- HTTPS
- HTTP 200 (no redirect)
- `Content-Type: application/json` (Vercel header is already set in `vercel.json`)

## H. Verify App Links / TWA

```
curl -sI https://market-place-six-chi.vercel.app/.well-known/assetlinks.json
```

Expect `200` and `content-type: application/json`. Then:

https://developers.google.com/digital-asset-links/tools/generator

Package: `pk.mobilemarket.app`  
Site: `https://market-place-six-chi.vercel.app`

On a real phone with the **release** build: open a listing URL in Chrome — it should offer Mobile Market, and the TWA should have **no browser URL bar**.

## I. Increment versions for each Play upload

In `app/build.gradle.kts`:

- `versionCode` — integer, **must increase every upload** (1 → 2 → 3 …)
- `versionName` — user-facing (`1.0.0` → `1.0.1` → `1.1.0`)

## J. Upload the AAB

Play Console → your app → **Release → Production** (or Internal testing first) → **Create release** → upload `app-release.aab`.

## K. Internal testing first

Use **Internal testing** (up to 100 testers by email) before Closed or Production. Install from the testing link on a real Android phone.

## L. What you still complete manually before Production

See the checklists below. Anything involving Play Console, your signing cert, screenshots, or a physical phone cannot be finished from this project alone.

---

## Google Play readiness

### Done in the project

- Unique application id `pk.mobilemarket.app`
- App name Mobile Market
- Adaptive launcher icons
- Splash (Android 12+ SplashScreen + TWA splash)
- `compileSdk` / `targetSdk` 36, `minSdk` 26
- TWA launcher, App Links intent filter, Custom Tabs fallback (not WebView)
- INTERNET + ACCESS_NETWORK_STATE only
- HTTPS-only network config
- Privacy policy URL on the website
- Feature graphic + 512 icon under `play/`
- Store listing draft: `play/STORE_LISTING.txt`
- Signing reads local/env secrets only
- Offline screen + retry
- `tel:`, WhatsApp, share, image picker declared in `<queries>`

### You must do

1. Generate a new upload keystore (section C).
2. Paste the real SHA-256 into `assetlinks.json` (section G).
3. Build a signed AAB on your machine (section E).
4. Create the Play app, fill Data safety, content rating, ads declaration, target audience.
5. Take phone screenshots (at least 2).
6. Internal testing on a real device.

### Website / backend

- Listings, search, filters, post/edit/delete/sold, photo upload, cities, PTA — all run on the website. The app displays that site.
- `assetlinks.json` is deployed with the website.
- AdSense is configured on the website (`js/ads-config.js`), not in the APK.
- No server user accounts today — ads you post stay on the device. Play **account deletion** does not apply unless you add accounts later.

### Play Console only

- Store listing (short/full description, graphics)
- Data safety form
- Content rating questionnaire
- Target audience / Families (choose 18+ unless you certify otherwise)
- Ads declaration (Yes after AdSense is live)
- App signing enrolment
- Testing track → Production rollout

---

## Marketplace behaviour (website vs Android)

| Action | Where it lives |
|---|---|
| Browse / search / filter | Website |
| Listing details and images | Website |
| Post ad + photo upload | Website (`<input type=file>` — TWA allows the system picker) |
| Edit / delete / mark sold | Website (My Ads) |
| Call seller | Website `tel:` — Android opens the dialer |
| WhatsApp | Website `https://wa.me/…` — Android opens WhatsApp if installed |
| Share listing | Website Web Share API |
| Back button | TWA / Chrome history |
| Offline launch | Android `OfflineActivity` |
| Maps | Only if a listing later uses `geo:` / Google Maps links |

Test those flows on a real phone in the release build.

---

## Testing checklist

**Basic:** fresh install, launch, close/reopen, back, deep link `https://market-place-six-chi.vercel.app/phones.html`, rotate if you allow it, small phone + tablet.

**Network:** fast, slow, airplane mode (offline screen), lose connection mid-session, restore and retry.

**Marketplace:** browse, search, brand/PTA/city filters, open listing, images, post ad, photo, edit, delete, mark sold, call, WhatsApp, share.

**Release:** signed AAB, Play Internal testing, TWA without URL bar, `assetlinks.json` 200 JSON, real device.

---

## Security

This repository and the downloadable zip must **not** contain:

- `password.txt`
- filled `keystore.properties`
- `.jks` / `.keystore` / `.p12`
- real SHA-256 certificates
- API tokens

If you ever find those files, treat the key as compromised and generate a new upload keystore.
