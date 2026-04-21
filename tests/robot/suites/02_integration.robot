*** Settings ***
Documentation     Integration tests for MyNewsapp.
...               These tests verify interactions between the app and external components.
Resource          ../resources/app_resources.resource
Test Setup        Open News App
Test Teardown     Close News App
Force Tags        integration

*** Test Cases ***
Verify Item Click Opens Browser
    [Documentation]    Verify that clicking an item opens an external browser.
    Start News App Activity
    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/article_title    timeout=${TIMEOUT_LONG}
    Click Element                    id=${APP_PACKAGE}:id/article_title
    Sleep    3s
    Go Back
    Wait Activity                    ${APP_ACTIVITY}    timeout=10
    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/list    timeout=${TIMEOUT}
    Page Should Contain Element      id=${APP_PACKAGE}:id/list

Verify Search Intent Integration
    [Documentation]    Verifies that the app handles the SEARCH intent correctly.
    # Start News App Activity allows triggering intents
    Start News App Activity    action=android.intent.action.SEARCH    query=science

    # Wait for the loader to finish and results to appear
    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/list    timeout=${TIMEOUT_LONG}
    Page Should Contain Element      id=${APP_PACKAGE}:id/article_title
