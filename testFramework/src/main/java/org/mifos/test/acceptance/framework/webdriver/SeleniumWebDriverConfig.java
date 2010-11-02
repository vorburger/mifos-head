/*
 * Copyright (c) 2005-2010 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package org.mifos.test.acceptance.framework.webdriver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thoughtworks.selenium.Selenium;

/**
 * Spring Configuration Class (like a <beans> *-context.xml) for WebDriver-based Selenium.
 *
 * @author Michael Vorburger
 */
@Configuration
public class SeleniumWebDriverConfig {

    @Bean
    public Selenium selenium() {
        int port = 8080; // TODO must get injected from ${servlet.port} - possible with Spring, I'll remember how later

        // TODO Make this configurable... Spring injected? Test Configuration System Property?
        // WebDriver baseDriver = new FirefoxDriver();
        WebDriver baseDriver = new HtmlUnitDriver(true);

        String baseUrl = "http://localhost:" + port + "/mifos/";
        WebDriverBackedSelenium selenium = new WebDriverBackedSelenium(baseDriver, baseUrl);

        return selenium;
    }
}
