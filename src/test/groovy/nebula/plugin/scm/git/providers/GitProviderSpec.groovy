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
package nebula.plugin.scm.git.providers

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.operation.ResetOp
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class GitProviderSpec extends Specification {
    @Rule
    TemporaryFolder temporaryFolder
    File projectDir
    File gitDir
    Grgit git

    def setup() {
        def testDir = temporaryFolder.root
        projectDir = new File(testDir, 'project')
        projectDir.mkdirs()
        gitDir = new File(testDir, 'git')
        gitDir.mkdirs()
        git = Grgit.init(dir: gitDir)
        def aFile = new File(gitDir, 'a.txt')
        aFile.text = 'a'
        git.add(patterns: ['a.txt'])
        git.commit(message: 'Initial commit')

        Grgit.clone(dir: projectDir, uri: "file://${gitDir.absolutePath}").close()
    }

    def 'switch branches'() {
        def provider = new GitProvider(projectDir.absolutePath)

        when:
        Boolean success = provider.switchToBranch('test')

        then:
        success
    }

    def 'commit file'() {
        def provider = new GitProvider(projectDir.absolutePath)
        def bFile = new File(projectDir, 'b.txt')
        bFile.text = 'b'

        when:
        Boolean success = provider.commit('commit b.txt', 'b.txt')
        git.reset(commit: 'HEAD', mode: ResetOp.Mode.HARD)

        then:
        success
        new File(gitDir, 'b.txt').text == 'b'
    }

    def 'pull from repository'() {
        def provider = new GitProvider(projectDir.absolutePath)
        def pullFile = new File(gitDir, 'pull.txt')
        pullFile.text = 'pull'
        git.add(patterns: ['pull.txt'])
        git.commit(message: 'pull')

        when:
        Boolean success = provider.updateFromRepository()

        then:
        success
        new File(projectDir, 'pull.txt').text == 'pull'
    }

    def 'tag repository'() {
        def provider = new GitProvider(projectDir.absolutePath)

        when:
        Boolean success = provider.tag('testtag', 'Creating testtag')

        then:
        success
        git.tag.list()[0].name == 'testtag'
    }

    def 'undo changes'() {
        def provider = new GitProvider(projectDir.absolutePath)
        new File(projectDir, 'a.txt').text = 'test change'

        when:
        Boolean success = provider.undoChanges()

        then:
        success
        new File(projectDir, 'a.txt').text == 'a'
    }

    def 'preChange noop returns true'() {
        def provider = new GitProvider(projectDir.absolutePath)

        when:
        Boolean success = provider.preChanges('a.txt', 'b.txt')

        then:
        success
    }
}
