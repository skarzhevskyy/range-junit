/*
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
 * Created on Feb. 26, 2020
 * @author vlads
 */
package org.exmple;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestsLogExtension implements AfterTestExecutionCallback, AfterAllCallback {

    private static int jvmForkClassCount = 0;

    private static int jvmForkTestsCount = 0;

    static {
        registerShutdownHook();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        jvmForkTestsCount++;
    }

    private static boolean isUnderMaven() {
        return System.getProperty("surefire.forkNumber") != null;
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        jvmForkClassCount++;
        if (isUnderMaven()) {
            System.out.println(String.format("JVM Fork %s completed; tests: %s; classes %s", System.getProperty("surefire.forkNumber"), jvmForkTestsCount,
                    jvmForkClassCount));
        }
    }

    private static void registerShutdownHook() {
        if (isUnderMaven()) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    afterAllTests();
                }
            });
        }
    }

    @SuppressWarnings("java:S106")
    private static void afterAllTests() {
        String mavenProjectName = System.getProperty("project.artifactId", "");
        System.out.println(String.format("Tests completed %s JVM Fork %s; total tests: %s; classes: %s",
                mavenProjectName, System.getProperty("surefire.forkNumber"),
                jvmForkTestsCount, jvmForkClassCount));
    }

}
