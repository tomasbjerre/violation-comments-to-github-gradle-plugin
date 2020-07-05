package se.bjurr.violations.comments.github.plugin.gradle;

import static se.bjurr.violations.comments.github.lib.ViolationCommentsToGitHubApi.violationCommentsToGitHubApi;
import static se.bjurr.violations.lib.ViolationsApi.violationsApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import se.bjurr.violations.lib.ViolationsLogger;
import se.bjurr.violations.lib.model.SEVERITY;
import se.bjurr.violations.lib.model.Violation;
import se.bjurr.violations.lib.reports.Parser;
import se.bjurr.violations.lib.util.Filtering;

public class ViolationCommentsToGitHubTask extends DefaultTask {

  private String repositoryOwner;
  private String repositoryName;
  /**
   * Travis will define TRAVIS_PULL_REQUEST as "false" if not a PR, and an integer if a PR. Having
   * this as String makes life easier =)
   */
  private String pullRequestId;

  private String oAuth2Token;
  private String username;
  private String password;
  private String gitHubUrl;
  private List<List<String>> violations = new ArrayList<>();
  private boolean createCommentWithAllSingleFileComments = false;
  private boolean createSingleFileComments = true;
  private boolean commentOnlyChangedContent = true;
  private boolean commentOnlyChangedFiles = true;
  private SEVERITY minSeverity;
  private boolean keepOldComments;
  private String commentTemplate;
  private Integer maxNumberOfViolations;

  public void setCommentOnlyChangedFiles(final boolean commentOnlyChangedFiles) {
    this.commentOnlyChangedFiles = commentOnlyChangedFiles;
  }

  public void setMaxNumberOfViolations(final Integer maxNumberOfViolations) {
    this.maxNumberOfViolations = maxNumberOfViolations;
  }

  public void setKeepOldComments(final boolean keepOldComments) {
    this.keepOldComments = keepOldComments;
  }

  public void setCreateCommentWithAllSingleFileComments(
      final boolean createCommentWithAllSingleFileComments) {
    this.createCommentWithAllSingleFileComments = createCommentWithAllSingleFileComments;
  }

  public void setCreateSingleFileComments(final boolean createSingleFileComments) {
    this.createSingleFileComments = createSingleFileComments;
  }

  public void setCommentOnlyChangedContent(final boolean commentOnlyChangedContent) {
    this.commentOnlyChangedContent = commentOnlyChangedContent;
  }

  public void setGitHubUrl(final String gitHubUrl) {
    this.gitHubUrl = gitHubUrl;
  }

  public void setRepositoryName(final String repositoryName) {
    this.repositoryName = repositoryName;
  }

  public void setRepositoryOwner(final String repositoryOwner) {
    this.repositoryOwner = repositoryOwner;
  }

  public void setPullRequestId(final String pullRequestId) {
    this.pullRequestId = pullRequestId;
  }

  public void setViolations(final List<List<String>> violations) {
    this.violations = violations;
  }

  public void setoAuth2Token(final String oAuth2Token) {
    this.oAuth2Token = oAuth2Token;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public void setMinSeverity(final SEVERITY minSeverity) {
    this.minSeverity = minSeverity;
  }

  public void setCommentTemplate(final String commentTemplate) {
    this.commentTemplate = commentTemplate;
  }

  @TaskAction
  public void gitChangelogPluginTasks() throws TaskExecutionException {
    this.getProject().getExtensions().findByType(ViolationCommentsToGitHubPluginExtension.class);
    if (this.pullRequestId == null || this.pullRequestId.equalsIgnoreCase("false")) {
      this.getLogger()
          .info("No pull request id defined, will not send violation comments to GitHub.");
      return;
    }
    final Integer pullRequestIdInt = Integer.valueOf(this.pullRequestId);
    if (this.oAuth2Token != null) {
      this.getLogger().info("Using OAuth2Token");
    } else if (this.username != null && this.password != null) {
      this.getLogger()
          .info("Using username/password: " + this.username.substring(0, 1) + ".../*********");
    } else {
      this.getLogger()
          .error(
              "No OAuth2 token and no username/email specified. Will not comment any pull request.");
      return;
    }

    this.getLogger()
        .info(
            "Will comment PR "
                + this.repositoryOwner
                + "/"
                + this.repositoryName
                + "/"
                + this.pullRequestId
                + " on "
                + this.gitHubUrl);

    final ViolationsLogger violationsLogger =
        new ViolationsLogger() {
          private LogLevel toGradleLogLevel(final Level level) {
            LogLevel gradleLevel = LogLevel.INFO;
            if (level == Level.FINE) {
              gradleLevel = LogLevel.DEBUG;
            } else if (level == Level.SEVERE) {
              gradleLevel = LogLevel.ERROR;
            } else if (level == Level.WARNING) {
              gradleLevel = LogLevel.WARN;
            }
            return gradleLevel;
          }

          @Override
          public void log(final Level level, final String string) {
            ViolationCommentsToGitHubTask.this
                .getLogger()
                .log(this.toGradleLogLevel(level), string);
          }

          @Override
          public void log(final Level level, final String string, final Throwable t) {
            ViolationCommentsToGitHubTask.this
                .getLogger()
                .log(this.toGradleLogLevel(level), string, t);
          }
        };

    Set<Violation> allParsedViolations = new TreeSet<>();
    for (final List<String> configuredViolation : this.violations) {
      final String reporter = configuredViolation.size() >= 4 ? configuredViolation.get(3) : null;
      final Set<Violation> parsedViolations =
          violationsApi() //
              .withViolationsLogger(violationsLogger) //
              .findAll(Parser.valueOf(configuredViolation.get(0))) //
              .inFolder(configuredViolation.get(1)) //
              .withPattern(configuredViolation.get(2)) //
              .withReporter(reporter) //
              .violations();
      if (this.minSeverity != null) {
        allParsedViolations = Filtering.withAtLEastSeverity(allParsedViolations, this.minSeverity);
      }
      allParsedViolations.addAll(parsedViolations);
    }

    try {
      violationCommentsToGitHubApi()
          .withViolationsLogger(violationsLogger)
          .withoAuth2Token(this.oAuth2Token)
          .withUsername(this.username)
          .withPassword(this.password)
          .withPullRequestId(pullRequestIdInt)
          .withRepositoryName(this.repositoryName)
          .withRepositoryOwner(this.repositoryOwner)
          .withGitHubUrl(this.gitHubUrl)
          .withViolations(allParsedViolations)
          .withCreateCommentWithAllSingleFileComments(
              this.createCommentWithAllSingleFileComments) //
          .withCreateSingleFileComments(this.createSingleFileComments) //
          .withCommentOnlyChangedContent(this.commentOnlyChangedContent) //
          .withCommentOnlyChangedFiles(this.commentOnlyChangedFiles) //
          .withKeepOldComments(this.keepOldComments) //
          .withCommentTemplate(this.commentTemplate) //
          .withMaxNumberOfViolations(this.maxNumberOfViolations) //
          .toPullRequest();
    } catch (final Exception e) {
      this.getLogger().error("", e);
    }
  }
}
