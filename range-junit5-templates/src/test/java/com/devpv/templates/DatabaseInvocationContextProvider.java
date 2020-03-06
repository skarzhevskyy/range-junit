/*
 * Pyx4j framework
 * Copyright (C) 2008-2019 pyx4j.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Created on Feb. 19, 2020
 * @author vlads
 */
package com.devpv.templates;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import com.devpv.DatabaseContext;

public class DatabaseInvocationContextProvider implements TestTemplateInvocationContextProvider {

    private final Map<String, DatabaseContext> databases;

    public DatabaseInvocationContextProvider() {
        databases = new HashMap<>();
        databases.put("postgresql", new DatabaseContext("PostgreSQL"));
        databases.put("hsql", new DatabaseContext("HSSQL"));
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        return databases.keySet().stream().map(this::invocationContext);
    }

    private TestTemplateInvocationContext invocationContext(final String databaseName) {

        return new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                return "on DB:" + databaseName;
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                final DatabaseContext databaseContainer = databases.get(databaseName);
                return Arrays.asList(
                        (BeforeEachCallback) context -> {
                            System.out.println("Start test with DB:" + databaseName);
                        },
                        new ParameterResolver() {

                            @Override
                            public boolean supportsParameter(ParameterContext parameterCtx, ExtensionContext extensionCtx) {
                                return parameterCtx.getParameter().getType().equals(DatabaseContext.class);
                            }

                            @Override
                            public Object resolveParameter(ParameterContext parameterCtx, ExtensionContext extensionCtx) {
                                return databaseContainer;
                            }
                        });
            }
        };
    }
}
