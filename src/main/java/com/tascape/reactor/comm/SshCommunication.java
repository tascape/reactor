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
package com.tascape.reactor.comm;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.tascape.reactor.SystemConfiguration;
import com.tascape.reactor.exception.EntityCommunicationException;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class SshCommunication extends EntityCommunication implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(SshCommunication.class);

    public static final String SYSPROP_HOST = "reactor.comm.ssh.HOST";

    public static final String SYSPROP_PORT = "reactor.comm.ssh.PORT";

    public static final String SYSPROP_KEY = "reactor.comm.ssh.KEY";

    public static final String SYSPROP_USER = "reactor.comm.ssh.USER";

    public static final String SYSPROP_PASS = "reactor.comm.ssh.PASS";

    private final JSch jSch;

    private final String host;

    private final int port;

    private Session session;

    private final Set<Channel> channels = new HashSet<>();

    public static SshCommunication newInstance() throws Exception {
        return newInstance("");
    }

    public static SshCommunication newInstance(String name) throws Exception {
        SystemConfiguration sysConfig = SystemConfiguration.getInstance();
        String h = sysConfig.getProperty(SshCommunication.SYSPROP_HOST + name);
        if (h == null) {
            h = sysConfig.getProperty(SshCommunication.SYSPROP_HOST, "localhost");
        }
        int p = sysConfig.getIntProperty(SshCommunication.SYSPROP_PORT + name);
        if (p == Integer.MIN_VALUE) {
            p = sysConfig.getIntProperty(SshCommunication.SYSPROP_PORT, 22);
        }
        SshCommunication ssh = new SshCommunication(h, p);

        String u = sysConfig.getProperty(SshCommunication.SYSPROP_USER + name);
        if (u == null) {
            u = sysConfig.getProperty(SshCommunication.SYSPROP_USER);
        }
        String k = sysConfig.getProperty(SshCommunication.SYSPROP_KEY + name);
        if (k == null) {
            k = sysConfig.getProperty(SshCommunication.SYSPROP_KEY);
        }
        String pw = sysConfig.getProperty(SshCommunication.SYSPROP_PASS + name);
        if (pw == null) {
            pw = sysConfig.getProperty(SshCommunication.SYSPROP_PASS);
        }
        ssh.setPrivateKey(k, pw);
        ssh.setUsernamePassword(u, pw);
        ssh.connect();
        return ssh;
    }

    /**
     *
     * @param host host DNS name or IP
     * @param port ssh port
     */
    public SshCommunication(String host, int port) {
        this.jSch = new JSch();
        this.host = host;
        this.port = port;
    }

    public void setPrivateKey(String key, String passphrase) throws JSchException {
        if (StringUtils.isNotBlank(key)) {
            this.jSch.addIdentity(key, passphrase);
        }
    }

    public void setUsernamePassword(String username, String password) throws JSchException {
        this.session = this.jSch.getSession(username, host, port);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        this.session.setPassword(password);
    }

    public List<String> shell(String command, long timeout) throws JSchException, IOException {
        Channel shell = this.session.openChannel("shell");
        shell.setInputStream(IOUtils.toInputStream(command + "\nexit\n", Charset.defaultCharset()));
        try (BufferedReader in = new BufferedReader(new InputStreamReader(shell.getInputStream()))) {
            shell.connect();
            channels.add(shell);
            Thread thread = new ChanneOperationTimer(shell, timeout);
            thread.start();
            List<String> lines = in.lines().collect(Collectors.toList());
            thread.interrupt();
            lines.forEach(l -> LOG.debug("{}", l));
            return lines;
        }
    }

    public Channel shell(String command, OutputStream out) throws JSchException {
        Channel shell = this.session.openChannel("shell");
        shell.setInputStream(IOUtils.toInputStream(command + "\nexit\n", Charset.defaultCharset()));
        shell.setOutputStream(out);
        shell.connect(2000);
        channels.add(shell);
        return shell;
    }

    public Channel shell(String command, File out) throws JSchException, IOException {
        Channel shell = this.session.openChannel("shell");
        shell.setInputStream(IOUtils.toInputStream(command + "\nexit\n", Charset.defaultCharset()));
        shell.setOutputStream(FileUtils.openOutputStream(out));
        shell.connect();
        channels.add(shell);
        return shell;
    }

    /**
     * Uploads a file to remote server, creates directory structure as needed.
     *
     * @param srcFile source file
     * @param destFile destination file full path from root, /home/user/dir1/dir2/file.txt
     * @param timeout in ms
     * @throws JSchException ssh error
     * @throws SftpException sftp error
     * @throws IOException io error
     */
    public void upload(File srcFile, String destFile, long timeout) throws JSchException, SftpException, IOException {
        Channel channel = this.session.openChannel("sftp");
        channel.setInputStream(System.in);
        channel.setOutputStream(System.out);
        channel.connect(5000);
        channels.add(channel);

        new ChanneOperationTimer(channel, timeout).start();
        ChannelSftp sftp = (ChannelSftp) channel;
        sftp.cd("/");
        String[] dirs = destFile.split("/");
        dirs[0] = "";
        dirs[dirs.length - 1] = "";
        for (String dir : dirs) {
            if (StringUtils.isBlank(dir)) {
                continue;
            }
            try {
                sftp.mkdir(dir);
            } catch (SftpException ex) {
                LOG.debug(ex.getLocalizedMessage());
            }
            sftp.cd(dir);
        }
        sftp.put(FileUtils.openInputStream(srcFile), destFile);
        sftp.exit();
    }

    public void download(String srcFile, File destFile, long timeout) throws JSchException, SftpException, IOException {
        Channel channel = this.session.openChannel("sftp");
        channel.setInputStream(System.in);
        channel.setOutputStream(System.out);
        channel.connect();
        channels.add(channel);

        new ChanneOperationTimer(channel, timeout).start();
        ChannelSftp sftp = (ChannelSftp) channel;
        try (FileOutputStream out = FileUtils.openOutputStream(destFile)) {
            sftp.get(srcFile, out);
        } finally {
            sftp.exit();
        }
    }

    @Override
    public void connect() throws Exception {
        session.connect();
        LOG.debug("connected to {}:{} ({})", session.getHost(), session.getPort(), session.getServerVersion());
    }

    @Override
    public void disconnect() throws Exception {
        try {
            channels.forEach(c -> {
                c.disconnect();
            });
        } finally {
            if (this.session != null) {
                this.session.disconnect();
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.disconnect();
        } catch (Exception ex) {
            throw new EntityCommunicationException(ex);
        }
    }

    private static class ChanneOperationTimer extends Thread {

        private final Channel channel;

        private final long timeout;

        public ChanneOperationTimer(Channel channel, long timeout) {
            this.channel = channel;
            this.timeout = timeout;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ex) {
                LOG.trace("interrupted - {}", ex.getMessage());
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        {
            SYS_CONFIG.getProperties().setProperty(SYSPROP_HOST + "VAGRANT", "localhost");
            SYS_CONFIG.getProperties().setProperty(SYSPROP_PORT + "VAGRANT", "2222");
            SYS_CONFIG.getProperties().setProperty(SYSPROP_USER + "VAGRANT", "vagrant");
            SYS_CONFIG.getProperties().setProperty(SYSPROP_PASS + "VAGRANT", "vagrant");
            SshCommunication ssh = SshCommunication.newInstance("VAGRANT");
            runSsh(ssh);
            ssh.disconnect();
        }
        {
            SshCommunication ssh = new SshCommunication("localhost", 2222);
            ssh.setUsernamePassword("vagrant", "vagrant");
            ssh.connect();
            runSsh(ssh);
            ssh.disconnect();
        }
        {
            SshCommunication ssh = new SshCommunication("localhost", 2222);
            ssh.setPrivateKey("~/.reactor/.vagrant/machines/default/virtualbox/private_key", "");
            ssh.setUsernamePassword("vagrant", "");
            ssh.connect();
            runSsh(ssh);
            ssh.disconnect();
        }
    }

    private static void runSsh(SshCommunication ssh) throws Exception {
        File out = File.createTempFile("ssh-", ".txt");
        FileUtils.write(out, "ssh test", Charset.defaultCharset());
        ssh.upload(out, "/home/vagrant/ssh.txt", 1000);

        ssh.shell("ls -al && sleep 5", 6000);
        ssh.shell("echo 'test data " + RandomUtils.nextLong() + "' >> /home/vagrant/ssh.txt", 2000);
        try (OutputStream os = FileUtils.openOutputStream(out)) {
            ssh.shell("cat /home/vagrant/ssh.txt", os);
        }
        ssh.download("/home/vagrant/ssh.txt", out, 2000);
        Thread.sleep(1000);
    }
}
