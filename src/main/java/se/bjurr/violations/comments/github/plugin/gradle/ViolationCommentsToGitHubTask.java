package se.bjurr.violations.comments.github.plugin.gradle;

import static se.bjurr.violations.comments.github.lib.ViolationCommentsToGitHubApi.violationCommentsToGitHubApi;
import static se.bjurr.violations.lib.ViolationsApi.violationsApi;

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
  private String commentTemplate;

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
          violationsApi() //
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
          .withCommentTemplate(commentTemplate) //
          .toPullRequest();
    } catch (final Exception e) {
      getLogger().error("", e);
    }
  }
}
