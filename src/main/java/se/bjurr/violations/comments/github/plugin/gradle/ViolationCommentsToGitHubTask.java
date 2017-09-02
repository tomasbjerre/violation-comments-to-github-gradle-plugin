package se.bjurr.violations.comments.github.plugin.gradle;

import static se.bjurr.violations.comments.github.lib.ViolationCommentsToGitHubApi.violationCommentsToGitHubApi;
import static se.bjurr.violations.lib.ViolationsReporterApi.violationsReporterApi;

import java.util.ArrayList;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
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
  private SEVERITY minSeverity;
  private boolean keepOldComments;

  public void setKeepOldComments(boolean keepOldComments) {
    this.keepOldComments = keepOldComments;
  }

  public void setCreateCommentWithAllSingleFileComments(
      boolean createCommentWithAllSingleFileComments) {
    this.createCommentWithAllSingleFileComments = createCommentWithAllSingleFileComments;
  }

  public void setCreateSingleFileComments(boolean createSingleFileComments) {
    this.createSingleFileComments = createSingleFileComments;
  }

  public void setCommentOnlyChangedContent(boolean commentOnlyChangedContent) {
    this.commentOnlyChangedContent = commentOnlyChangedContent;
  }

  public void setGitHubUrl(String gitHubUrl) {
    this.gitHubUrl = gitHubUrl;
  }

  public void setRepositoryName(String repositoryName) {
    this.repositoryName = repositoryName;
  }

  public void setRepositoryOwner(String repositoryOwner) {
    this.repositoryOwner = repositoryOwner;
  }

  public void setPullRequestId(String pullRequestId) {
    this.pullRequestId = pullRequestId;
  }

  public void setViolations(List<List<String>> violations) {
    this.violations = violations;
  }

  public void setoAuth2Token(String oAuth2Token) {
    this.oAuth2Token = oAuth2Token;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setMinSeverity(SEVERITY minSeverity) {
    this.minSeverity = minSeverity;
  }

  @TaskAction
  public void gitChangelogPluginTasks() throws TaskExecutionException {
    getProject().getExtensions().findByType(ViolationCommentsToGitHubPluginExtension.class);
    if (pullRequestId == null || pullRequestId.equalsIgnoreCase("false")) {
      getLogger().info("No pull request id defined, will not send violation comments to GitHub.");
      return;
    }
    final Integer pullRequestIdInt = Integer.valueOf(pullRequestId);
    if (oAuth2Token != null) {
      getLogger().info("Using OAuth2Token");
    } else if (username != null && password != null) {
      getLogger().info("Using username/password: " + username.substring(0, 1) + ".../*********");
    } else {
      getLogger()
          .error(
              "No OAuth2 token and no username/email specified. Will not comment any pull request.");
      return;
    }

    getLogger()
        .info(
            "Will comment PR "
                + repositoryOwner
                + "/"
                + repositoryName
                + "/"
                + pullRequestId
                + " on "
                + gitHubUrl);

    List<Violation> allParsedViolations = new ArrayList<>();
    for (final List<String> configuredViolation : violations) {
      final String reporter = configuredViolation.size() >= 4 ? configuredViolation.get(3) : null;
      final List<Violation> parsedViolations =
          violationsReporterApi() //
              .findAll(Parser.valueOf(configuredViolation.get(0))) //
              .inFolder(configuredViolation.get(1)) //
              .withPattern(configuredViolation.get(2)) //
              .withReporter(reporter) //
              .violations();
      if (minSeverity != null) {
        allParsedViolations = Filtering.withAtLEastSeverity(allParsedViolations, minSeverity);
      }
      allParsedViolations.addAll(parsedViolations);
    }

    try {
      violationCommentsToGitHubApi() //
          .withoAuth2Token(oAuth2Token) //
          .withUsername(username) //
          .withPassword(password) //
          .withPullRequestId(pullRequestIdInt) //
          .withRepositoryName(repositoryName) //
          .withRepositoryOwner(repositoryOwner) //
          .withGitHubUrl(gitHubUrl) //
          .withViolations(allParsedViolations) //
          .withCreateCommentWithAllSingleFileComments(createCommentWithAllSingleFileComments) //
          .withCreateSingleFileComments(createSingleFileComments) //
          .withCommentOnlyChangedContent(commentOnlyChangedContent) //
          .withKeepOldComments(keepOldComments) //
          .toPullRequest();
    } catch (final Exception e) {
      getLogger().error("", e);
    }
  }
}
