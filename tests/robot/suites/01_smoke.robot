*** Settings ***
Documentation     Smoke tests for MyNewsapp.
...               These tests verify that the application launches correctly and
...               the primary UI components are present.
Resource          ../resources/app_resources.resource
Test Setup        Open News App
Test Teardown     Close News App
Force Tags        smoke

*** Test Cases ***
App Should Launch Successfully
    [Documentation]    Verify the app launches and displays the news list.
    Start News App Activity
    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/list    timeout=${TIMEOUT}
    Page Should Contain Element      id=${APP_PACKAGE}:id/list

Verify Essential UI Components
    [Documentation]    Verify that the news list is present in the layout.
    ...               The list visibility is the primary synchronization anchor.
    Start News App Activity
    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/list    timeout=${TIMEOUT_LONG}
    Page Should Contain Element      id=${APP_PACKAGE}:id/list
    # Progress bar should disappear after loading
    Wait Until Page Does Not Contain Element    id=${APP_PACKAGE}:id/progress_bar    timeout=${TIMEOUT}
