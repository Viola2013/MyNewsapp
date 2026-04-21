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
- **Testing Frameworks:**
    - **Unit/Instrumentation:** JUnit 4 & AndroidX Test.
    - **Acceptance/E2E:** Robot Framework with AppiumLibrary.

## Project Structure
- `MainActivity`: Orchestrates UI, intent handling (Search/Mock/NewsData), and Loader initialization.
- `NewsAdapter`: Custom adapter for the `RecyclerView` with date normalization.
- `NewsLoader`: Implementation of `AsyncTaskLoader` for fetching news listings.
- `QueryUtils`: Central utility for URL generation, HTTP requests, and multi-format JSON parsing (Guardian, NewsData, etc.).
- `NewsListing`: Data model representing a news article.

## Running Automated Tests

The project uses **Robot Framework** for End-to-End and Regression testing.

### Prerequisites
1.  **Python & Robot Framework:** Ensure Python is installed, then install dependencies:
    ```bash
    pip install robotframework robotframework-appiumlibrary
    ```
2.  **Appium Server:** Install and start the Appium server (v2.x recommended):
    ```bash
    appium
    ```
3.  **Android Environment:** 
    - An Android Emulator or physical device must be connected via `adb`.
    - The `ANDROID_HOME` environment variable must be set.
4.  **Build the App:** Ensure the latest debug APK is built:
    ```bash
    ./gradlew assembleDebug
    ```

### Executing Tests
Run all regression tests using the following command from the project root:
```bash
robot tests/robot/suites/*.robot
```

### Viewing Results
After execution, Robot Framework generates detailed reports in the project root:
- `report.html`: A high-level summary of the test execution.
- `log.html`: Detailed execution logs with step-by-step breakdown.
- `output.xml`: Machine-readable test results.
