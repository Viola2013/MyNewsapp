*** Settings ***
Documentation     End-to-End tests for MyNewsapp.
...               These tests verify complete user journeys.
Resource          ../resources/app_resources.resource
Test Setup        Open News App
Test Teardown     Close News App
Force Tags        e2e

*** Test Cases ***
User Journey: Search And Read News
    [Documentation]    Full user journey: Open app, search for news, scroll and select an article.
    Start News App Activity
    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/list    timeout=${TIMEOUT}

    # Use Start News App Activity to trigger search via intent
    # Note: Search with mock=true will still show mock results, not filtered by query
    # because the mock implementation in NewsLoader.java simply returns the file content.
    Start News App Activity    action=android.intent.action.SEARCH    query=space    use_mock=true

    # Synchronize on the list showing results
    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/article_title    timeout=${TIMEOUT_LONG}
    Element Should Contain Text      id=${APP_PACKAGE}:id/article_title    Space

    # Scroll the RecyclerView - replaced Scroll Element Into View with Swipe By Percent
    # Swipe from 80% to 20% vertically in the middle of the screen
    Swipe By Percent    50    80    50    20    1000

    # Click on the first article title to open browser
    Click Element    id=${APP_PACKAGE}:id/article_title
    # Use explicit wait instead of sleep to be more robust
    Wait Until Page Does Not Contain Element    id=${APP_PACKAGE}:id/list    timeout=${TIMEOUT}

    # Return to app
    # If standard Go Back fails, we can try activating the app again
    Activate Application    ${APP_PACKAGE}
    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/list    timeout=${TIMEOUT_LONG}
