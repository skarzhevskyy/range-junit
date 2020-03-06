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
package com.devpv.templates.context;

import com.devpv.DatabaseContext;

public class AllDatabases3ContextTest {

    private DatabaseContext db;

    public AllDatabases3ContextTest(DatabaseContext db) {
        this.db = db;
    }

    @TestWithDatabase
    void testWithDatabase() {
        System.out.println(this.getClass().getSimpleName() + " Run With:" + db.toString());
    }

}
