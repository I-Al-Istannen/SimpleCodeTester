package me.ialistannen.simplecodetester.test;

import java.util.Arrays;
import java.util.regex.Pattern;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.checks.defaults.ImportCheck;
import me.ialistannen.simplecodetester.submission.CompiledFile;

public class DefaultImportCheck extends ImportCheck {

  public DefaultImportCheck() {
    super(
        Arrays.asList(
            Pattern.compile("java.lang.*"),
            Pattern.compile("java.io.*"),
            Pattern.compile("edu.kit.informatik.*")
        ),
        Arrays.asList(Pattern.compile("java.lang.StringBuilders"))
    );
  }

  @Override
  public CheckResult check(CompiledFile file) {
    if (file.qualifiedName().equals("Terminal")) {
      return CheckResult.emptySuccess(this);
    }
    return super.check(file);
  }
}
