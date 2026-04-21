*** Settings ***
Documentation     Shared resources for MyNewsapp Appium tests.
Library           Collections
Library           AppiumLibrary

*** Variables ***
${ANDROID_AUTOMATION_NAME}    UIAutomator2
${ANDROID_APP}                ${CURDIR}/../../../app/build/outputs/apk/debug/app-debug.apk
${ANDROID_PLATFORM_NAME}      Android
${ANDROID_PLATFORM_VERSION}   %{ANDROID_PLATFORM_VERSION=11}
${DEVICE_NAME}                %{DEVICE_NAME=emulator-5554}
${APP_PACKAGE}                com.example.android.mynewsapp
${APP_ACTIVITY}               .MainActivity
${TIMEOUT}                    15s
${TIMEOUT_LONG}               30s

*** Keywords ***
Open News App
    [Documentation]    Launches the application with the predefined capabilities.
    Open Application    http://localhost:4723
    ...                 automationName=${ANDROID_AUTOMATION_NAME}
    ...                 platformName=${ANDROID_PLATFORM_NAME}
    ...                 platformVersion=${ANDROID_PLATFORM_VERSION}
    ...                 deviceName=${DEVICE_NAME}
    ...                 appPackage=${APP_PACKAGE}
    ...                 appActivity=${APP_ACTIVITY}
    ...                 appWaitActivity=${APP_PACKAGE}${APP_ACTIVITY}
    ...                 noReset=false
    ...                 fullReset=false
    ...                 newCommandTimeout=300
    # Ensuring the app is ready. Open Application already waits for appWaitActivity,
    # but we'll ensure the list is present or the activity is focused.
    Ensure App Is In Foreground

Close News App
    [Documentation]    Closes the application.
    Close Application

Reset Network Connection
    [Documentation]    Resets network connection to All enabled (6).
    Set Network Connection Status    6

Start News App Activity
    [Arguments]    ${action}=android.intent.action.MAIN    ${query}=None    ${use_mock}=true
    [Documentation]    Starts the news app activity with optional search query and mock data flag using mobile:startActivity.
    # Terminating the app ensures the next activity start uses a fresh intent and handles extras correctly.
    Terminate Application    ${APP_PACKAGE}

    ${extras} =    Create List
    IF    '${query}' != 'None'
        ${extra_query} =    Create List    s    query    ${query}
        Append To List    ${extras}    ${extra_query}
    END
    IF    '${use_mock}' == 'true'
        ${extra_mock} =     Create List    z    use_mock    ${True}
        Append To List    ${extras}    ${extra_mock}
    END

    &{opts} =    Create Dictionary    component=${APP_PACKAGE}/${APP_ACTIVITY}    action=${action}
    IF    len($extras) > 0
        Set To Dictionary    ${opts}    extras=${extras}
    END

    Execute Script    mobile: startActivity    &{opts}

Ensure App Is In Foreground
    [Documentation]    Brings the application to the foreground.
    # Activate Application is a safe way to ensure the app is focused.
    # It doesn't require insecure adb_shell features.
    Activate Application    ${APP_PACKAGE}
