/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.jps.cabal;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor;
import org.jetbrains.jps.cabal.model.HaskellSdkType;
import org.jetbrains.jps.cabal.model.JpsHaskellSdkProperties;
import org.jetbrains.jps.incremental.*;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.incremental.messages.ProgressMessage;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.library.sdk.JpsSdk;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleType;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CabalBuilder extends ModuleLevelBuilder {

    public static String cabalPath = null;

    public CabalBuilder() {
        super(BuilderCategory.TRANSLATOR);
    }


    public ExitCode build(final CompileContext context,
                          final ModuleChunk chunk,
                          final DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> dirtyFilesHolder,
                          final OutputConsumer outputConsumer) throws ProjectBuildException {
        try {
            for (JpsModule module : chunk.getModules()) {
                JpsModuleType<?> moduleType = module.getModuleType();
                if (!(moduleType instanceof JpsHaskellModuleType)) {
                    continue;
                }
                File cabalFile = getCabalFile(module);
                if (cabalFile == null) {
                    context.processMessage(new CompilerMessage(
                            "cabal",
                            BuildMessage.Kind.ERROR,
                            "Can't find cabal file in " + getContentRootPath(module)));
                    continue;
                }

                if (getSdk(module) == null) {
                    context.processMessage(new CompilerMessage(
                            "cabal",
                            BuildMessage.Kind.ERROR,
                            "Can't find GHC SDK"));
                    continue;
                }

                CabalJspInterface cabal = new CabalJspInterface(cabalPath, cabalFile);

                if (!runConfigure(context, module, cabal)) {
                    return ExitCode.ABORT;
                }
                if (!runBuild(context, module, cabal)) {
                    return ExitCode.ABORT;
                }
            }
            return ExitCode.OK;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            context.processMessage(new CompilerMessage("cabal", BuildMessage.Kind.ERROR, e.getMessage()));
        }
        return ExitCode.ABORT;
    }

    private boolean runBuild(CompileContext context, JpsModule module, CabalJspInterface cabal) throws IOException, InterruptedException {
        context.processMessage(new ProgressMessage("cabal build"));
        context.processMessage(new CompilerMessage("cabal", BuildMessage.Kind.INFO, "Start build"));
        Process buildProcess = cabal.build();
        processOut(context, buildProcess, module);

        if (buildProcess.waitFor() != 0) {
            context.processMessage(new CompilerMessage("cabal", BuildMessage.Kind.ERROR, "build errors."));
            return false;
        }
        return true;
    }

    private JpsSdk<JpsSimpleElement<JpsHaskellSdkProperties>> getSdk(JpsModule module) {
        return module.getSdk(HaskellSdkType.INSTANCE);
    }

    private boolean runConfigure(CompileContext context, JpsModule module, CabalJspInterface cabal) throws IOException, InterruptedException {
        context.processMessage(new CompilerMessage("cabal", BuildMessage.Kind.INFO, "Start configure"));

        JpsSdk<JpsSimpleElement<JpsHaskellSdkProperties>> sdk = getSdk(module);
        String ghcPath = sdk.getSdkProperties().getData().getGhcPath();

        Process configureProcess = cabal.configure(ghcPath);

        processOut(context, configureProcess, module);

        if (configureProcess.waitFor() != 0) {
            context.processMessage(new CompilerMessage(
                    "cabal",
                    BuildMessage.Kind.ERROR,
                    "configure failed."));
            return false;
        }
        return true;
    }

    private void processOut(CompileContext context, Process process, JpsModule module) throws IOException {
        Iterator<String> processOut = collectOutput(process);

        while (processOut.hasNext()) {
            String line = processOut.next();
            String warningPrefix = "Warning: ";
            Matcher matcher = Pattern.compile("(.*):(\\d+):(\\d+):(.*)").matcher(line);
            if (line.startsWith(warningPrefix)) {
                String text = line.substring(warningPrefix.length()) + "\n" + processOut.next();
                context.processMessage(new CompilerMessage("cabal", BuildMessage.Kind.WARNING, text));
            } else if (matcher.find()) {
                String file = matcher.group(1);
                long lineNum = Long.parseLong(matcher.group(2));
                long colNum = Long.parseLong(matcher.group(3));
                String msg = matcher.group(4);
                while (processOut.hasNext()) {
                    String msgLine = processOut.next();

                    if (msgLine.endsWith("warning generated.")) {
                        break;
                    }
                    if (msgLine.trim().length() == 0) {
                        break;
                    }
                    msg += msgLine + "\n";
                }

                String sourcePath = getContentRootPath(module) + "/" + file.replace('\\', '/');
                BuildMessage.Kind kind;
                final String trimmedMessage = msg.trim();
                if (trimmedMessage.startsWith("warning") || trimmedMessage.startsWith("Warning")) {
                    kind = BuildMessage.Kind.WARNING;
                } else {
                    kind = BuildMessage.Kind.ERROR;
                }

                context.processMessage(new CompilerMessage(
                        "ghc",
                        kind,
                        trimmedMessage,
                        sourcePath,
                        -1L, -1L, -1L,
                        lineNum, colNum));
            } else {
                context.processMessage(new CompilerMessage("cabal", BuildMessage.Kind.INFO, line));
            }
        }
    }

    private Iterator<String> collectOutput(Process process) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return new Iterator<String>() {

            String line = null;

            @Override
            public boolean hasNext() {
                return fetch() != null;
            }

            private String fetch() {
                if (line == null) {
                    try {
                        line = reader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return line;
            }

            @Override
            public String next() {
                String result = fetch();
                line = null;
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private File getCabalFile(JpsModule module) {
        String pathname = getContentRootPath(module);
        File[] files = new File(pathname).listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file.getName().endsWith(".cabal")) {
                return file;
            }
        }

        return null;
    }

    private String getContentRootPath(JpsModule module) {
        final List<String> urls = module.getContentRootsList().getUrls();
        if (urls.size() == 0) {
            throw new RuntimeException("Can't find content root in module");
        }
        String url = urls.get(0);
        return url.substring("file://".length());
    }


    @Override
    public List<String> getCompilableFileExtensions() {
        return Collections.singletonList("hs");
    }


    @Override
    public String toString() {
        return getPresentableName();
    }

    @NotNull
    public String getPresentableName() {
        return "Cabal builder";
    }

}
