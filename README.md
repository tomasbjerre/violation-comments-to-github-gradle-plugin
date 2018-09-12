# Violation Comments to GitHub Gradle Plugin [![Build Status](https://travis-ci.org/tomasbjerre/violation-comments-to-github-gradle-plugin.svg?branch=master)](https://travis-ci.org/tomasbjerre/violation-comments-to-github-gradle-plugin) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.bjurr.violations/violation-comments-to-github-gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.bjurr.violations/violation-comments-to-github-gradle-plugin) [ ![Bintray](https://api.bintray.com/packages/tomasbjerre/tomasbjerre/se.bjurr.violations%3Aviolation-comments-to-github-gradle-plugin/images/download.svg) ](https://bintray.com/tomasbjerre/tomasbjerre/se.bjurr.violations%3Aviolation-comments-to-github-gradle-plugin/_latestVersion)

This is a Gradle plugin for [Violation Comments to GitHub Lib](https://github.com/tomasbjerre/violation-comments-to-github-lib).

It can be used in Travis, or any other build server, to read results from static code analysis and comment pull requests in GitHub with them.

You can have a look at [violations-test](https://github.com/tomasbjerre/violations-test/pull/2) to see what the result may look like.

The merge must be performed in order for the commented lines in the PR to match the lines reported by the analysis tools!

It supports:
 * [_AndroidLint_](http://developer.android.com/tools/help/lint.html)
 * [_Checkstyle_](http://checkstyle.sourceforge.net/)
   * [_Detekt_](https://github.com/arturbosch/detekt) with `--output-format xml`.
   * [_ESLint_](https://github.com/sindresorhus/grunt-eslint) with `format: 'checkstyle'`.
   * [_KTLint_](https://github.com/shyiko/ktlint)
   * [_SwiftLint_](https://github.com/realm/SwiftLint) with `--reporter checkstyle`.
   * [_TSLint_](https://palantir.github.io/tslint/usage/cli/) with `-t checkstyle`
   * [_PHPCS_](https://github.com/squizlabs/PHP_CodeSniffer) with `phpcs api.php --report=checkstyle`.
 * [_CLang_](https://clang-analyzer.llvm.org/)
   * [_RubyCop_](http://rubocop.readthedocs.io/en/latest/formatters/) with `rubycop -f clang file.rb`
   * [_GCC_](https://gcc.gnu.org/)
   * [_ARM-GCC_](https://developer.arm.com/open-source/gnu-toolchain/gnu-rm)
   * [_Doxygen_](https://www.stack.nl/~dimitri/doxygen/)
 * [_CodeNarc_](http://codenarc.sourceforge.net/)
 * [_CPD_](http://pmd.sourceforge.net/pmd-4.3.0/cpd.html)
 * [_CPPLint_](https://github.com/theandrewdavis/cpplint)
 * [_CPPCheck_](http://cppcheck.sourceforge.net/)
 * [_CSSLint_](https://github.com/CSSLint/csslint)
 * [_DocFX_](http://dotnet.github.io/docfx/)
 * [_Findbugs_](http://findbugs.sourceforge.net/)
   * [_Spotbugs_](https://spotbugs.github.io/)
 * [_Flake8_](http://flake8.readthedocs.org/en/latest/)
   * [_AnsibleLint_](https://github.com/willthames/ansible-lint) with `-p`
   * [_Mccabe_](https://pypi.python.org/pypi/mccabe)
   * [_Pep8_](https://github.com/PyCQA/pycodestyle)
   * [_PyFlakes_](https://pypi.python.org/pypi/pyflakes)
 * [_FxCop_](https://en.wikipedia.org/wiki/FxCop)
 * [_Gendarme_](http://www.mono-project.com/docs/tools+libraries/tools/gendarme/)
 * [_GoLint_](https://github.com/golang/lint)
   * [_GoVet_](https://golang.org/cmd/vet/) Same format as GoLint.
 * [_GoogleErrorProne_](https://github.com/google/error-prone)
   * [_NullAway_](https://github.com/uber/NullAway) Same format as Google Error Prone.
 * [_JSHint_](http://jshint.com/)
 * _Lint_ A common XML format, used by different linters.
 * [_JCReport_](https://github.com/jCoderZ/fawkez/wiki/JcReport)
 * [_Klocwork_](http://www.klocwork.com/products-services/klocwork/static-code-analysis)
 * [_MyPy_](https://pypi.python.org/pypi/mypy-lang)
 * [_PCLint_](http://www.gimpel.com/html/pcl.htm) PC-Lint using the same output format as the Jenkins warnings plugin, [_details here_](https://wiki.jenkins.io/display/JENKINS/PcLint+options)
 * [_PerlCritic_](https://github.com/Perl-Critic)
 * [_PiTest_](http://pitest.org/)
 * [_PyDocStyle_](https://pypi.python.org/pypi/pydocstyle)
 * [_PyLint_](https://www.pylint.org/) with `pylint --output-format=parseable`.
 * [_PMD_](https://pmd.github.io/)
   * [_Infer_](http://fbinfer.com/) Facebook Infer. With `--pmd-xml`.
   * [_PHPPMD_](https://phpmd.org/) with `phpmd api.php xml ruleset.xml`.
 * [_ReSharper_](https://www.jetbrains.com/resharper/)
 * [_SbtScalac_](http://www.scala-sbt.org/)
 * [_Simian_](http://www.harukizaemon.com/simian/)
 * [_StyleCop_](https://stylecop.codeplex.com/)
 * [_XMLLint_](http://xmlsoft.org/xmllint.html)
 * [_ZPTLint_](https://pypi.python.org/pypi/zptlint)

 
## Usage ##
There is a running example [here](https://github.com/tomasbjerre/violation-comments-to-github-gradle-plugin/tree/master/violation-comments-to-github-gradle-plugin-example).

Here is and example: 

```
  buildscript {
    repositories {
      maven {
        url "https://plugins.gradle.org/m2/"
      }
    }
    dependencies {
      classpath "se.bjurr.violations:violation-comments-to-github-gradle-plugin:X"
    }
  }

  apply plugin: "se.bjurr.violations.violation-comments-to-github-gradle-plugin"

  task violationCommentsToGitHub(type: se.bjurr.violations.comments.github.plugin.gradle.ViolationCommentsToGitHubTask) {
   repositoryOwner = "tomasbjerre";
   repositoryName = "violations-test"
   pullRequestId = System.properties['GITHUB_PULLREQUESTID']
   username = System.properties['GITHUB_USERNAME']
   password = System.properties['GITHUB_PASSWORD']
   oAuth2Token = System.properties['GITHUB_OAUTH2TOKEN']
   gitHubUrl = "https://api.github.com/"
   createCommentWithAllSingleFileComments = false
   createSingleFileComments = true
   commentOnlyChangedContent = true
   minSeverity = se.bjurr.violations.lib.model.SEVERITY.INFO //ERROR, INFO, WARN
   commentTemplate = """
**Reporter**: {{violation.reporter}}{{#violation.rule}}

**Rule**: {{violation.rule}}{{/violation.rule}}
**Severity**: {{violation.severity}}
**File**: {{violation.file}} L{{violation.startLine}}{{#violation.source}}

**Source**: {{violation.source}}{{/violation.source}}

{{violation.message}}
"""
   violations = [
    ["FINDBUGS",   ".", ".*/findbugs/.*\\.xml\$",   "Findbugs"],
    ["PMD",        ".", ".*/pmd/.*\\.xml\$",        "PMD"],
    ["CHECKSTYLE", ".", ".*/checkstyle/.*\\.xml\$", "Checkstyle"],
    ["JSHINT",     ".", ".*/jshint/.*\\.xml\$",     "JSHint"],
    ["CSSLINT",    ".", ".*/csslint/.*\\.xml\$",     "CssLint"]
   ]
  }
```

To send violations, just run:
```
./gradlew violationCommentsToGitHub -DGITHUB_PULLREQUESTID=$GITHUB_PULL_REQUEST -DGITHUB_USERNAME=... -DGITHUB_PASSWORD=...
```

Or if you want to use OAuth2:
```
./gradlew violationCommentsToGitHub -DGITHUB_PULLREQUESTID=$TRAVIS_PULL_REQUEST -DGITHUB_OAUTH2TOKEN=$GITHUB_OAUTH2TOKEN
```

You may also have a look at [Violations Lib](https://github.com/tomasbjerre/violations-lib).

## Developer instructions

To build the code you need to run `build.sh` in root of repo. You may also have a look at `.travis.yml`.

To do a release you need to do `./gradlew release -Dgradle.publish.key=... -Dgradle.publish.secret=...` and release the artifact from [staging](https://oss.sonatype.org/#stagingRepositories). More information [here](http://central.sonatype.org/pages/releasing-the-deployment.html).
