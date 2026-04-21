# MyNewsapp

A simple Android news feed application that fetches and displays regularly-updated news from the internet.

## Features
- **Multi-Source News Feed:** Support for The Guardian, NewsApi.org, and NewsData.io.
- **Finnish News Integration:** Specialized support for Finnish news via NewsData.io (Query: "newsdata").
- **Robust UI:** Displays news in a list format using `RecyclerView` with optimized view binding.
- **Background Loading:** Efficient data fetching using Android `Loader` framework.
- **Automated Testing:** Comprehensive regression and integration tests using Robot Framework and Appium.

## Tech Stack
- **Language:** Java
- **UI Components:** `RecyclerView`, `ConstraintLayout`, `AppCompat`
- **Networking:** `HttpURLConnection` with custom JSON parsing in `QueryUtils`.
- **Dependency Management:** Gradle Version Catalog (`libs.versions.toml`).
- **Testing:**
    - **Unit/Instrumentation:** JUnit 4 & AndroidX Test.
    - **Integration/E2E:** Robot Framework with Appium Library.

## Project Structure
- `MainActivity`: Orchestrates UI, intent handling (Search/Mock/NewsData), and Loader initialization.
- `NewsAdapter`: Custom adapter for the `RecyclerView` with date normalization.
- `NewsLoader`: Implementation of `AsyncTaskLoader` for fetching news listings.
- `QueryUtils`: Central utility for URL generation, HTTP requests, and multi-format JSON parsing (Guardian, NewsData, etc.).
- `NewsListing`: Data model representing a news article.

## Integration Tests
To run the automated Robot Framework tests:
```bash
robot tests/robot/suites/04_regression.robot
```
Ensure Appium server is running and an Android device/emulator is connected.
