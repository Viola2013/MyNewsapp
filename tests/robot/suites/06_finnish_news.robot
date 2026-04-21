*** Settings ***
Documentation     Finnish News Integration tests for MyNewsapp.
Resource          ../resources/app_resources.resource
Test Setup        Open News App
Test Teardown     Close News App
Force Tags        finnish_news

*** Test Cases ***
Verify Finnish News (YLE) Loading
    [Documentation]    Verify that Finnish news from YLE can be loaded and displayed.
    # We use a mock URL that starts with mock://yle to trigger the new Finnish mock data
    Start News App Activity    use_mock=true    query=yle

    Wait Until Element Is Visible    id=${APP_PACKAGE}:id/article_title    timeout=${TIMEOUT_LONG}

    # Verify content from mock_yle.json
    Page Should Contain Text         Suomi voitti jääkiekon maailmanmestaruuden
    Page Should Contain Text         Yle Urheilu

    # Verify the second item by swiping if necessary
    Page Should Contain Text         Kesäkuu alkaa helteisessä säässä
