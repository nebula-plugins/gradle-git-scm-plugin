/*
 * Copyright 2014-2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nebula.plugin.scm.git

import groovy.transform.CompileDynamic
import nebula.plugin.scm.ScmPlugin
import nebula.plugin.scm.git.providers.GitProvider
import org.gradle.api.Plugin
import org.gradle.api.Project

class GitScmPlugin implements Plugin<Project> {
    private static final String EXTENSION_NAME = 'gitScm'
    @Override
    void apply(Project project) {
        project.plugins.apply(ScmPlugin)
        GitScmExtension extension = project.rootProject.extensions.findByName(EXTENSION_NAME) as GitScmExtension
        if (!extension) {
            extension = project.rootProject.extensions.create(EXTENSION_NAME, GitScmExtension, project.rootProject.projectDir.path)
            createScmFactoryMethod(project, extension)
        }
    }

    @CompileDynamic
    private void createScmFactoryMethod(Project project, GitScmExtension extension) {
        project.rootProject.scmFactory.createMethod = { new GitProvider(extension.getRootDirectory()) }
    }
}
