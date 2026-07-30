# Brew TV (Jet Stream Compose)

Android TV app based on Google’s [Jet Stream](https://developer.android.com/design/ui/tv/samples/jet-stream) Compose for TV sample, wired to Brew VOD APIs.

## APIs

| Screen | Endpoint |
|--------|----------|
| Home (carousel + rows) | `GET https://www.brew.tv/api/v1/vod/home-sections` |
| Movie details / player metadata | `GET https://www.brew.tv/api/v1/vod/get-campaign/{slug}` |

Example details slug: `227-dream-for-an-insomniac`

## Run

1. Open this folder in Android Studio (Ladybug / Iguana+).
2. Use an Android TV emulator or device (API 28+).
3. Run the `jetstream` configuration.

```bash
./gradlew :jetstream:installDebug
```

## What’s mapped

- **Home showcase** → Brew `movie_showcase` section (featured carousel)
- **Immersive “Most Watched”** → Brew tray named Most Watched
- **Content rows** → remaining `movie_tray` sections
- **Details** → campaign title, synopsis, cast/crew, genres, reviews, similar titles
- **Play** → uses trailer URL from the campaign when available (main feature may be DRM-protected)

Jet Stream UI patterns (TabRow, Carousel, ImmersiveList, TV Material cards) are preserved from the official sample.
