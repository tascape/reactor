/*
 * Copyright (c) 2015 - present Nebula Bay.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tascape.reactor.tools;

import java.io.File;
import java.io.IOException;
import javax.naming.OperationNotSupportedException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Tar;
import org.apache.tools.ant.taskdefs.Untar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class AntCompressor {
    private static final Logger LOG = LoggerFactory.getLogger(AntCompressor.class);

    public static void main(String[] args) throws IOException, OperationNotSupportedException {
        LOG.info("tgz/untgz, usages:");
        LOG.info("1. arguments 'tgz folder-to-be-tgzed'");
        LOG.info("2. arguments 'untgz file-to-be-untgzed'");

        switch (args[0]) {
            case "tgz":
                AntCompressor.tgz(args[1]);
                break;
            case "untgz":
                AntCompressor.untgz(args[1]);
                break;
            default:
                throw new UnsupportedOperationException("only supports tgz, untgz");
        }
    }

    public static File tgz(String folder) {
        Project p = new Project();
        p.init();

        Tar tar = new Tar();
        Tar.TarCompressionMethod method = new Tar.TarCompressionMethod();
        method.setValue("gzip");
        tar.setCompression(method);

        tar.setProject(p);
        File tgzFile = new File(folder + ".tgz");
        tar.setDestFile(tgzFile);
        tar.setBasedir(new File("."));
        tar.setIncludes(folder + "/**");
        tar.perform();
        LOG.info("output file is {}", tgzFile);
        return tgzFile;
    }

    public static void untgz(String file) {
        if (!file.endsWith("tgz")) {
            throw new UnsupportedOperationException("only supports .tgz file");
        }
        File tgzFile = new File(file);

        Project p = new Project();
        p.init();

        Untar untar = new Untar();
        Untar.UntarCompressionMethod method = new Untar.UntarCompressionMethod();
        method.setValue("gzip");
        untar.setCompression(method);

        untar.setProject(p);
        untar.setSrc(tgzFile);
        untar.setDest(new File("."));
        LOG.info("output folder is {}");
        untar.perform();
    }
}
