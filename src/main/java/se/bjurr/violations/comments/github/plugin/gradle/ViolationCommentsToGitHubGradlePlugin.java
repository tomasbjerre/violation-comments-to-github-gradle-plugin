package se.bjurr.violations.comments.github.plugin.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ViolationCommentsToGitHubGradlePlugin implements Plugin<Project> {
  @Override
  public void apply(Project target) {
    target
        .getExtensions()
        .create("violationCommentsToGitHubPlugin", ViolationCommentsToGitHubPluginExtension.class);
  }
}
