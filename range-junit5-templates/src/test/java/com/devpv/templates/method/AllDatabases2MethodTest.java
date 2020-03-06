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
package com.devpv.templates.method;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import com.devpv.DatabaseContext;
import com.devpv.templates.DatabaseInvocationContextProvider;

/**
 * The the same tests with multiple databases
 *
 * @see https://junit.org/junit5/docs/current/user-guide/#extensions-test-templates
 * @see https://dzone.com/articles/integration-test-with-multiple-databases-1
 */
public class AllDatabases2MethodTest {

    @TestTemplate
    @ExtendWith(DatabaseInvocationContextProvider.class)
    void testWithDatabase(DatabaseContext db) {
        System.out.println(this.getClass().getSimpleName() + " Run With:" + db.toString());
    }

}
