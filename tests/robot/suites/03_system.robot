*** Settings ***
Documentation     System tests for MyNewsapp.
...               These tests verify high-level system behaviors and integration.
Resource          ../resources/app_resources.resource
Test Setup        Open News App
Test Teardown     Run Keywords    Reset Network Connection    AND    Close News App
Force Tags        system

*** Test Cases ***
Verify Search Result Display
    [Documentation]    Verifies that the system correctly processes a search query.
    # Start Activity with correct parameters
    Start News App Activity    action=android.intent.action.SEARCH    query=technology

    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/list    timeout=${TIMEOUT_LONG}
    ${count} =    Get Matching Xpath Count    //android.widget.TextView[@resource-id='${APP_PACKAGE}:id/article_title']
    Should Be True    ${count} > 0

Verify Network Error Handling
    [Documentation]    Verifies app behavior during network disconnection.
    # Instead of 'svc' which requires shell, we toggle connectivity via Appium's Airplane Mode or connection status
    # Connection 0=None, 1=Airplane, 2=Wifi, 4=Data, 6=All
    Set Network Connection Status    1
    Close News App
    Open News App
    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/empty_view    timeout=${TIMEOUT}
    Page Should Contain Element      id=${APP_PACKAGE}:id/empty_view
    # Cleanup: Reset connection
    Reset Network Connection
