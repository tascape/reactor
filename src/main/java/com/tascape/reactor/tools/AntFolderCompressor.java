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
public class AntFolderCompressor {
    private static final Logger LOG = LoggerFactory.getLogger(AntFolderCompressor.class);

    public static void main(String[] args) throws IOException, OperationNotSupportedException {
        LOG.info("tar/untar, usages:");
        LOG.info("1. arguments 'tar folder-to-be-tarred'");
        LOG.info("2. arguments 'untar file-to-be-untarred'");

        AntFolderCompressor compressor = new AntFolderCompressor();
        switch (args[0]) {
            case "tar":
                compressor.tar(args[1]);
                break;
            case "untar":
                compressor.untar(args[1]);
                break;
            default:
                throw new UnsupportedOperationException("only supports tar, untar");
        }
    }

    public File tar(String folder) {
        Project p = new Project();
        p.init();

        Tar tar = new Tar();
        tar.setProject(p);
        File tarFile = new File(folder + ".tar");
        tar.setDestFile(tarFile);
        tar.setBasedir(new File("."));
        tar.setIncludes(folder + "/**");
        tar.perform();
        LOG.info("output file is {}", tarFile);
        return tarFile;
    }

    public void untar(String file) {
        Project p = new Project();
        p.init();

        Untar tar = new Untar();
        tar.setProject(p);
        File tarFile = new File(file);
        tar.setSrc(tarFile);
        tar.setDest(new File("."));
        LOG.info("output folder is {}");
        tar.perform();
    }
}
