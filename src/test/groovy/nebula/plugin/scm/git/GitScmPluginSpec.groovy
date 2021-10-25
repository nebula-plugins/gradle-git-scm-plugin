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

import nebula.plugin.scm.git.providers.GitProvider
import nebula.plugin.scm.providers.ScmProvider
import nebula.test.PluginProjectSpec
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.operation.ResetOp

class GitScmPluginSpec extends PluginProjectSpec {
    String pluginName = 'nebula.gradle-git-scm'

    def 'scmFactory returns GitProvider'() {
        Grgit.init(dir: projectDir)
        project.apply plugin: pluginName

        when:
        def provider = project.scmFactory.create()

        then:
        noExceptionThrown()
        provider instanceof GitProvider
        provider instanceof ScmProvider
    }

    def 'non root directory for git action'() {
        def gitDir = new File(projectDir, 'project')
        gitDir.mkdirs()
        def git = Grgit.init(dir: gitDir)
        def nonroot = new File(gitDir, 'nonroot.txt')
        nonroot.text = 'test\n'
        git.add(patterns: ['nonroot.txt'])
        git.commit(message: 'Initial commit')

        def subDir = new File(projectDir, 'sub')
        Grgit.clone(dir: subDir, uri: "file://${gitDir.absolutePath}").close()

        project.apply plugin: pluginName
        project.gitScm {
            rootDirectory subDir.absolutePath
        }

        def provider = project.scmFactory.create()
        new File(subDir, 'nonroot.txt').text += 'addition\n'

        when:
        def success = provider.commit('test', ['nonroot.txt'])
        git.reset(commit: 'HEAD', mode: ResetOp.Mode.HARD)

        then:
        success
        new File(gitDir, 'nonroot.txt').text.contains('test\n')
        new File(gitDir, 'nonroot.txt').text.contains('addition\n')
    }
}
