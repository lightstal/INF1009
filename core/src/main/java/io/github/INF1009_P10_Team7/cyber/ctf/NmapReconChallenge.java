package io.github.INF1009_P10_Team7.cyber.ctf;

/**
 * CTF Challenge 1 - Network Recon.
 *
 * Flow the player must discover:
 * nmap 192.168.10.0/24 → finds host .105
 * connect 192.168.10.105 → SSH shell on target
 * ls → see files
 * cat notes.txt → hint about hidden dir
 * ls -la → see .secret/
 * cd .secret → enter dir
 * ls → flag.txt
 * cat flag.txt → FLAG{r3c0n_m4st3r_1337}
 */
/**
 * NmapReconChallenge, CTF terminal challenge simulating network reconnaissance.
 *
 * <p>The player must discover a hidden SSH server on 192.168.10.105, connect to it,
 * locate a hidden {@code .secret/} directory, and read {@code flag.txt}.</p>
 *
 * <p>Implements {@link ICTFChallenge} so it can be wrapped in a
 * {@link io.github.INF1009_P10_Team7.cyber.minigame.TerminalMiniGame}
 * and treated as a standard {@link io.github.INF1009_P10_Team7.cyber.minigame.IMiniGame}
 * by {@code CyberGameScene} (OCP, LSP).</p>
 *
 * <p>State machine:</p>
 * <ul>
 * <li>Not connected → connected after {@code connect 192.168.10.105}</li>
 * <li>HOME dir → SECRET dir after {@code cd .secret}</li>
 * <li>Solved = {@code true} after {@code cat flag.txt} in the SECRET dir</li>
 * </ul>
 */
public class NmapReconChallenge implements ICTFChallenge {

    private boolean connected   = false;   // after "connect 192.168.10.105"
    private boolean solved      = false;

    // Simple state machine for where the shell is
    private enum Dir { HOME, SECRET }
    private Dir currentDir = Dir.HOME;

    @Override public String getTitle()      { return "TERMINAL-01 // NETWORK RECON"; }
    @Override public String getTargetInfo() { return "target: 192.168.10.0/24"; }
    @Override public String getPrompt() {
        if (!connected) return "attacker@kali:~$ ";
        return currentDir == Dir.SECRET
            ? "root@srv-105:~/.secret$ "
            : "root@srv-105:~$ ";
    }

    @Override
    public TerminalLine[] getWelcomeLines() {
        return new TerminalLine[]{
            TerminalLine.warn("OBJECTIVE: Locate the hidden flag on the target server."),
            TerminalLine.dim ("----------------------------------------"),
            TerminalLine.out ("Intel: An internal server is leaking credentials."),
            TerminalLine.out ("You have a foothold on the attacker machine."),
            TerminalLine.dim ("Hint: start with a network scan."),
        };
    }

    @Override
    public TerminalLine[] processInput(String raw) {
        String cmd = raw.trim();
        String lo  = cmd.toLowerCase();

        // Not yet connected
        if (!connected) {
            if (lo.startsWith("nmap")) {
                if (lo.contains("192.168.10")) return nmapScan();
                return err("No route to host. Try the /24 subnet: 192.168.10.0/24");
            }
            if (lo.startsWith("connect") || lo.startsWith("ssh")) {
                if (lo.contains("192.168.10.105")) {
                    connected = true;
                    return new TerminalLine[]{
                        TerminalLine.ok  ("Connecting to 192.168.10.105:22 ..."),
                        TerminalLine.ok  ("SSH-2.0-OpenSSH_8.9p1"),
                        TerminalLine.warn("Password: [credential cache hit]  -  root"),
                        TerminalLine.ok  ("Welcome to Ubuntu 22.04.3 LTS"),
                        TerminalLine.dim ("Last login: Mon Oct 14 03:17:42 2024 from 192.168.10.11"),
                    };
                }
                return err("ssh: connect to host " + extractArg(cmd) + " port 22: Connection refused");
            }
            if (lo.equals("help") || lo.equals("?")) return helpPre();
            return unknown(cmd);
        }

        // Connected - HOME dir
        if (currentDir == Dir.HOME) {
            if (lo.equals("ls")) return lsHome();
            if (lo.equals("ls -la") || lo.equals("ls -al") || lo.equals("ls -a")) return lsHomeHidden();
            if (lo.startsWith("cat notes.txt")) return catNotes();
            if (lo.startsWith("cat flag.txt"))
                return err("cat: flag.txt: Permission denied  (hint: look for hidden directories)");
            if (lo.startsWith("cat backup.tar.gz")) return err("cat: binary file  -  use: file backup.tar.gz");
            if (lo.startsWith("cat")) return err("cat: " + extractArg(cmd) + ": No such file or directory");
            if (lo.startsWith("cd .secret") || lo.equals("cd .secret")) {
                currentDir = Dir.SECRET;
                return new TerminalLine[]{ TerminalLine.dim("") };
            }
            if (lo.startsWith("cd logs"))  return err("bash: cd: logs: is a directory  -  try: ls logs/");
            if (lo.startsWith("cd")) return err("bash: cd: " + extractArg(cmd) + ": No such file or directory");
            if (lo.startsWith("whoami")) return new TerminalLine[]{ TerminalLine.out("root") };
            if (lo.startsWith("id"))     return new TerminalLine[]{ TerminalLine.out("uid=0(root) gid=0(root) groups=0(root)") };
            if (lo.startsWith("pwd"))    return new TerminalLine[]{ TerminalLine.out("/root") };
            if (lo.startsWith("uname"))  return uname();
            if (lo.startsWith("hostname")) return new TerminalLine[]{ TerminalLine.out("srv-105") };
            if (lo.startsWith("ifconfig") || lo.startsWith("ip addr") || lo.startsWith("ip a")) return ifconfig();
            if (lo.startsWith("netstat") || lo.startsWith("ss -")) return netstat();
            if (lo.startsWith("ps aux") || lo.startsWith("ps -ef")) return psAux();
            if (lo.startsWith("sudo -l")) return new TerminalLine[]{ TerminalLine.warn("User root may run ALL commands  -  you already are root.") };
            if (lo.startsWith("history")) return history();
            if (lo.startsWith("find")) return find(cmd);
            if (lo.startsWith("file backup")) return new TerminalLine[]{ TerminalLine.out("backup.tar.gz: gzip compressed data, last modified: Oct 14 02:01") };
            if (lo.startsWith("echo")) return new TerminalLine[]{ TerminalLine.out(extractArg(cmd).replaceAll("\"","").replaceAll("'","")) };
            if (lo.startsWith("env") || lo.startsWith("printenv")) return env();
            if (lo.equals("help") || lo.equals("?")) return helpPost();
            if (lo.equals("exit") || lo.equals("quit")) { connected = false; currentDir = Dir.HOME;
                return new TerminalLine[]{ TerminalLine.dim("Connection to 192.168.10.105 closed.") }; }
            return unknown(cmd);
        }

        // Connected - SECRET dir
        if (lo.equals("ls") || lo.equals("ls -la") || lo.equals("ls -al")) return lsSecret();
        if (lo.startsWith("cat flag.txt")) return catFlag();
        if (lo.startsWith("cat")) return err("cat: " + extractArg(cmd) + ": No such file or directory");
        if (lo.startsWith("cd ..") || lo.equals("cd ~")) { currentDir = Dir.HOME;
            return new TerminalLine[]{ TerminalLine.dim("") }; }
        if (lo.startsWith("whoami")) return new TerminalLine[]{ TerminalLine.out("root") };
        if (lo.startsWith("id"))     return new TerminalLine[]{ TerminalLine.out("uid=0(root) gid=0(root) groups=0(root)") };
        if (lo.startsWith("pwd"))    return new TerminalLine[]{ TerminalLine.out("/root/.secret") };
        if (lo.startsWith("echo")) return new TerminalLine[]{ TerminalLine.out(extractArg(cmd).replaceAll("\"","").replaceAll("'","")) };
        if (lo.equals("exit") || lo.equals("quit")) { connected = false; currentDir = Dir.HOME;
            return new TerminalLine[]{ TerminalLine.dim("Connection to 192.168.10.105 closed.") }; }
        return unknown(cmd);
    }

    @Override public boolean isSolved() { return solved; }
    @Override public void reset() { connected = false; currentDir = Dir.HOME; solved = false; }

    // Sub-responses

    private TerminalLine[] nmapScan() {
        return new TerminalLine[]{
            TerminalLine.out("Starting Nmap 7.94 ( https://nmap.org )"),
            TerminalLine.dim("Nmap scan report for 192.168.10.1"),
            TerminalLine.dim("Host is up (0.00030s latency)."),
            TerminalLine.dim("All 1000 scanned ports on 192.168.10.1 filtered"),
            TerminalLine.blank(),
            TerminalLine.out("Nmap scan report for 192.168.10.105"),
            TerminalLine.ok ("Host is up (0.00011s latency)."),
            TerminalLine.ok ("PORT   STATE SERVICE"),
            TerminalLine.ok ("22/tcp open  ssh      OpenSSH 8.9p1"),
            TerminalLine.ok ("80/tcp open  http     Apache httpd 2.4.54"),
            TerminalLine.dim("MAC Address: 08:00:27:AA:14:05"),
            TerminalLine.blank(),
            TerminalLine.warn("1 host found. SSH is open on 192.168.10.105."),
            TerminalLine.dim ("Try: ssh root@192.168.10.105  OR  connect 192.168.10.105"),
        };
    }

    private TerminalLine[] lsHome() {
        return new TerminalLine[]{
            TerminalLine.out("notes.txt  logs/  backup.tar.gz"),
        };
    }

    private TerminalLine[] lsHomeHidden() {
        return new TerminalLine[]{
            TerminalLine.dim("total 32"),
            TerminalLine.out("drwx------ 4 root root 4096 Oct 14 03:17 ."),
            TerminalLine.out("drwxr-xr-x 3 root root 4096 Oct 14 01:02 .."),
            TerminalLine.out("-rw-r--r-- 1 root root  220 Oct 14 01:02 .bash_logout"),
            TerminalLine.out("-rw-r--r-- 1 root root 3526 Oct 14 01:02 .bashrc"),
            TerminalLine.warn("drwx------ 2 root root 4096 Oct 14 03:15 .secret"),
            TerminalLine.out("-rw-r--r-- 1 root root  148 Oct 14 02:44 notes.txt"),
            TerminalLine.out("drwxr-xr-x 2 root root 4096 Oct 14 02:01 logs"),
            TerminalLine.out("-rw-r--r-- 1 root root 8192 Oct 14 02:01 backup.tar.gz"),
        };
    }

    private TerminalLine[] catNotes() {
        return new TerminalLine[]{
            TerminalLine.out("# Server admin notes  -  DO NOT SHARE"),
            TerminalLine.out("Moved sensitive data to hidden dir for 'safety'."),
            TerminalLine.out("Remember to delete this file before audit."),
            TerminalLine.warn("-- also the flag is in .secret/flag.txt but nobody"),
            TerminalLine.warn("   checks hidden directories anyway, right? :)"),
        };
    }

    private TerminalLine[] lsSecret() {
        return new TerminalLine[]{
            TerminalLine.out("total 8"),
            TerminalLine.out("-rw------- 1 root root 38 Oct 14 03:15 flag.txt"),
        };
    }

    private TerminalLine[] catFlag() {
        solved = true;
        return new TerminalLine[]{
            TerminalLine.blank(),
            TerminalLine.ok  ("||||||||||||||||||||||||||||||||||||||"),
            TerminalLine.ok  ("  FLAG{r3c0n_m4st3r_1337}"),
            TerminalLine.ok  ("||||||||||||||||||||||||||||||||||||||"),
            TerminalLine.blank(),
            TerminalLine.warn(">>> ACCESS KEY EXTRACTED  -  TERMINAL UNLOCKED <<<"),
        };
    }

    private TerminalLine[] helpPre() {
        return new TerminalLine[]{
            TerminalLine.info("Available commands:"),
            TerminalLine.out ("  nmap <subnet>        -  scan the network"),
            TerminalLine.out ("  connect <ip>         -  connect via SSH"),
            TerminalLine.out ("  ssh root@<ip>        -  same as connect"),
        };
    }

    private TerminalLine[] helpPost() {
        return new TerminalLine[]{
            TerminalLine.info("Shell commands:"),
            TerminalLine.out ("  ls / ls -la          -  list files (use -la for hidden)"),
            TerminalLine.out ("  cat <file>           -  read a file"),
            TerminalLine.out ("  cd <dir> / cd ..     -  change directory"),
            TerminalLine.out ("  whoami / id          -  current user"),
            TerminalLine.out ("  pwd                  -  current path"),
            TerminalLine.out ("  uname -a             -  kernel info"),
            TerminalLine.out ("  ifconfig / ip addr   -  network interfaces"),
            TerminalLine.out ("  netstat / ss         -  open ports"),
            TerminalLine.out ("  ps aux               -  running processes"),
            TerminalLine.out ("  history              -  command history"),
            TerminalLine.out ("  find / <name>        -  search files"),
            TerminalLine.out ("  echo <text>          -  print text"),
            TerminalLine.out ("  env                  -  environment variables"),
            TerminalLine.out ("  exit                 -  disconnect"),
        };
    }

    private TerminalLine[] uname() {
        return new TerminalLine[]{
            TerminalLine.out("Linux srv-105 5.15.0-91-generic #101-Ubuntu SMP x86_64 GNU/Linux"),
        };
    }

    private TerminalLine[] ifconfig() {
        return new TerminalLine[]{
            TerminalLine.out("eth0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500"),
            TerminalLine.out("        inet 192.168.10.105  netmask 255.255.255.0  broadcast 192.168.10.255"),
            TerminalLine.out("        ether 08:00:27:aa:14:05  txqueuelen 1000"),
            TerminalLine.blank(),
            TerminalLine.out("lo: flags=73<UP,LOOPBACK,RUNNING>  mtu 65536"),
            TerminalLine.out("        inet 127.0.0.1  netmask 255.0.0.0"),
        };
    }

    private TerminalLine[] netstat() {
        return new TerminalLine[]{
            TerminalLine.out("Active Internet connections (servers and established)"),
            TerminalLine.out("Proto  Local Address          Foreign Address        State"),
            TerminalLine.ok ("tcp    0.0.0.0:22             0.0.0.0:*              LISTEN"),
            TerminalLine.ok ("tcp    0.0.0.0:80             0.0.0.0:*              LISTEN"),
            TerminalLine.out("tcp    192.168.10.105:22      192.168.10.11:54321    ESTABLISHED"),
            TerminalLine.out("tcp    127.0.0.1:3306         0.0.0.0:*              LISTEN"),
        };
    }

    private TerminalLine[] psAux() {
        return new TerminalLine[]{
            TerminalLine.dim("USER         PID %CPU %MEM    VSZ   RSS TTY      STAT START   COMMAND"),
            TerminalLine.out("root           1  0.0  0.1  21292  7432 ?        Ss   Oct14   /sbin/init"),
            TerminalLine.out("root         412  0.0  0.1  15820  6120 ?        Ss   Oct14   /usr/sbin/sshd"),
            TerminalLine.out("www-data     502  0.0  0.3  82340 14220 ?        S    Oct14   apache2"),
            TerminalLine.warn("root         887  0.0  0.0   4628  1404 ?        S    Oct14   /bin/sh -c cron_backup.sh"),
            TerminalLine.out("mysql        921  0.0  2.1 1245392 86400 ?       Sl   Oct14   /usr/sbin/mysqld"),
            TerminalLine.out("root        1337  0.0  0.0   6548  1728 pts/0    Ss   03:17   bash"),
        };
    }

    private TerminalLine[] history() {
        return new TerminalLine[]{
            TerminalLine.dim("    1  apt update"),
            TerminalLine.dim("    2  apt install openssh-server"),
            TerminalLine.dim("    3  ls -la"),
            TerminalLine.warn("    4  mkdir .secret"),
            TerminalLine.warn("    5  cp /tmp/flag.txt .secret/"),
            TerminalLine.dim("    6  chmod 700 .secret"),
            TerminalLine.dim("    7  rm /tmp/flag.txt"),
            TerminalLine.dim("    8  echo 'done'"),
        };
    }

    private TerminalLine[] find(String cmd) {
        String arg = extractArg(cmd);
        if (arg.contains("flag") || arg.contains("secret") || arg.contains(".")) {
            return new TerminalLine[]{
                TerminalLine.warn("/root/.secret/flag.txt"),
                TerminalLine.out("/root/backup.tar.gz"),
                TerminalLine.out("/root/notes.txt"),
            };
        }
        return new TerminalLine[]{ TerminalLine.err("find: missing operand  -  try: find / -name flag*") };
    }

    private TerminalLine[] env() {
        return new TerminalLine[]{
            TerminalLine.out("SHELL=/bin/bash"),
            TerminalLine.out("USER=root"),
            TerminalLine.out("HOME=/root"),
            TerminalLine.out("PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"),
            TerminalLine.out("LANG=en_US.UTF-8"),
            TerminalLine.warn("SECRET_TOKEN=not_stored_here_check_the_filesystem"),
        };
    }

    private TerminalLine[] err(String msg) {
        return new TerminalLine[]{ TerminalLine.err(msg) };
    }
    private TerminalLine[] unknown(String cmd) {
        String c = cmd.split("\\s+")[0];
        return new TerminalLine[]{ TerminalLine.err("bash: " + c + ": command not found") };
    }
    private String extractArg(String cmd) {
        String[] p = cmd.split("\\s+", 2);
        return p.length > 1 ? p[1].trim() : "";
    }
}
