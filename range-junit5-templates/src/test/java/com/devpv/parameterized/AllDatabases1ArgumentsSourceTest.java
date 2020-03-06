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
package com.devpv.parameterized;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.devpv.DatabaseContext;

/**
 * The the same test Code with multiple databases
 *
 * @see https://www.baeldung.com/parameterized-tests-junit-5
 * @see https://blog.codefx.org/libraries/junit-5-parameterized-tests/
 * @see https://www.petrikainulainen.net/programming/testing/junit-5-tutorial-writing-parameterized-tests/
 */
public class AllDatabases1ArgumentsSourceTest {

    @ParameterizedTest(name = "on DB {0}")
    @ArgumentsSource(DatabaseArgumentProvider.class)
    void testWithDatabase(DatabaseContext db) {

        System.out.println(this.getClass().getSimpleName() + " Run With:" + db.toString());

    }

    static class DatabaseArgumentProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            // HEre is can use external config to decide what DB are installed and what to runs
            return Stream.of(
                    Arguments.of(new DatabaseContext("HSSQL")),
                    Arguments.of(new DatabaseContext("PostgreSQL")));
        }

    }

}
