*** Settings ***
Documentation     Regression tests for MyNewsapp.
...               These tests ensure existing functionality remains intact.
Resource          ../resources/app_resources.resource
Test Setup        Open News App
Test Teardown     Close News App
Force Tags        regression

*** Keywords ***
Open News App With Mock
    Open News App
    Start News App Activity

*** Test Cases ***
Verify Data Consistency Across Orientations
    [Documentation]    Regression test to ensure data persists after rotation.
    [Setup]    Open News App With Mock
    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/article_title    timeout=${TIMEOUT_LONG}
    ${first_title} =    Get Text      id=${APP_PACKAGE}:id/article_title

    # Replacing adb shell rotation with Appium's Landscape mode
    Landscape
    Sleep    2s

    ${rotated_title} =  Get Text      id=${APP_PACKAGE}:id/article_title
    Should Be Equal     ${first_title}    ${rotated_title}

    # Reset rotation
    Portrait

Verify UI Scalability
    [Documentation]    Regression test for UI components placement.
    [Setup]    Open News App With Mock
    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/list    timeout=${TIMEOUT}
    # Ensure at least one item is loaded before checking its components
    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/article_title    timeout=${TIMEOUT}
    Wait Until Page Contains Element    id=${APP_PACKAGE}:id/article_title
    Wait Until Page Contains Element    id=${APP_PACKAGE}:id/section
    Wait Until Page Contains Element    id=${APP_PACKAGE}:id/date

Verify Finnish News Integration (Regression)
    [Documentation]    Regression test to ensure Finnish news source integration persists.
    [Tags]             finnish_news
    Start News App Activity    use_mock=true    query=yle
    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/article_title    timeout=${TIMEOUT_LONG}
    Page Should Contain Text         Suomi voitti jääkiekon maailmanmestaruuden
    Page Should Contain Text         Yle Urheilu

Verify NewsData.io Finnish News Integration
    [Documentation]    Integration test for NewsData.io API with Finnish news.
    [Tags]             newsdata    external
    # This test uses the real API as requested by the developer.
    # Note: In a CI environment, this might be fragile due to rate limits or connectivity.
    Start News App Activity    use_mock=false    query=newsdata
    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/article_title    timeout=${TIMEOUT_LONG}
    # Since we are using real data, we check for common Finnish words likely to appear in results for "Suomi".
    # We use a broad check to ensure something was loaded.
    ${title} =    Get Text    id=${APP_PACKAGE}:id/article_title
    Log    First article title: ${title}
    Should Not Be Empty    ${title}
    # Check for presence of the NewsData source ID or general Finnish context
    Wait Until Page Contains Element    id=${APP_PACKAGE}:id/section    timeout=${TIMEOUT}
