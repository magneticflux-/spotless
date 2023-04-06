package com.diffplug.spotless.maven.javascript;

import java.nio.file.Paths;

import org.apache.maven.plugins.annotations.Parameter;

import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.javascript.RomeStep;
import com.diffplug.spotless.maven.FormatterStepConfig;
import com.diffplug.spotless.maven.FormatterStepFactory;

/**
 * Factory for creating the Rome formatter step that formats JavaScript and
 * TypeScript code with Rome:
 * <a href= "https://github.com/rome/tools">https://github.com/rome/tools</a>.
 * It delegates to the Rome executable.
 */
public class RomeJs implements FormatterStepFactory {
	/**
	 * Optional directory where the downloaded Rome executable is placed. If this is
	 * a relative path, it is resolved against the project's base directory.
	 * Defaults to
	 * <code>~/.m2/repository/com/diffplug/spotless/spotless-data/rome</code>.
	 * <p>
	 * You can use an expression like <code>${user.home}/rome</code> if you want to
	 * use the home directory, or <code>${project.build.directory</code> if you want
	 * to use the target directory of the current project.
	 */
	@Parameter
	private String downloadDir;

	/**
	 * Optional path to the Rome executable. Either a <code>version</code> or a
	 * <code>pathToExe</code> should be specified. When not given, an attempt is
	 * made to download the executable for the given version from the network. When
	 * given, the executable is used and the <code>version</code> parameter is
	 * ignored.
	 * <p>
	 * When an absolute path is given, that path is used as-is. When a relative path
	 * is given, it is resolved against the project's base directory. When only a
	 * file name (i.e. without any slashes or back slash path separators such as
	 * {@code rome}) is given, this is interpreted as the name of a command with
	 * executable that is in your {@code path} environment variable. Use
	 * {@code ./executable-name} if you want to use an executable in the project's
	 * base directory.
	 */
	@Parameter
	private String pathToExe;

	/**
	 * Rome version to download, applies only when no <code>pathToExe</code> is
	 * specified explicitly. Either a <code>version</code> or a
	 * <code>pathToExe</code> should be specified. When not given, a default known
	 * version is used. For stable builds, it is recommended that you always set the
	 * version explicitly. This parameter is ignored when you specify a
	 * <code>pathToExe</code> explicitly.
	 */
	@Parameter
	private String version;

	@Override
	public FormatterStep newFormatterStep(FormatterStepConfig config) {
		RomeStep rome;
		if (pathToExe != null) {
			var resolvedExePath = resolveExePath(config);
			rome = RomeStep.withExePath(resolvedExePath);
		} else {
			var downloadDir = resolveDownloadDir(config);
			rome = RomeStep.withExeDownload(version, downloadDir);
		}
		return rome.create();
	}

	/**
	 * Resolves the path to the Rome executable. When the path is only a file name,
	 * do not perform any resolution and interpret it as a command that must be on
	 * the user's path. Otherwise resolve the executable path against the project's
	 * base directory.
	 * 
	 * @param config Configuration from the Maven Mojo execution with details about
	 *               the currently executed project.
	 * @return The resolved path to the Rome executable.
	 */
	private String resolveExePath(FormatterStepConfig config) {
		var path = Paths.get(pathToExe);
		if (path.getNameCount() == 1) {
			return path.toString();
		} else {
			return config.getFileLocator().getBaseDir().toPath().resolve(path).toAbsolutePath().toString();
		}
	}

	/**
	 * Resolves the directory to use for storing downloaded Rome executable. When a
	 * {@link #downloadDir} is given, use that directory, resolved against the
	 * current project's directory. Otherwise, use the {@code Rome} sub folder in
	 * the shared data directory.
	 * 
	 * @param config Configuration for this step.
	 * @return The download directory for the Rome executable.
	 */
	private String resolveDownloadDir(FormatterStepConfig config) {
		final var fileLocator = config.getFileLocator();
		if (downloadDir != null && !downloadDir.isBlank()) {
			return fileLocator.getBaseDir().toPath().resolve(downloadDir).toAbsolutePath().toString();
		} else {
			return fileLocator.getDataDir().toPath().resolve("rome").toString();
		}
	}
}
